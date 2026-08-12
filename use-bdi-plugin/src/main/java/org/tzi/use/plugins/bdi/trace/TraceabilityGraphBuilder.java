package org.tzi.use.plugins.bdi.trace;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshot;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.model.mapping.MappingBinding;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;
import org.tzi.use.plugins.bdi.model.mapping.MappingSourceId;
import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;

/** Derives portable evidence chains from an immutable current-analysis snapshot. */
public final class TraceabilityGraphBuilder {
    public TraceabilityGraph build(CurrentAnalysisSnapshot snapshot, Path projectRoot) {
        Objects.requireNonNull(snapshot, "snapshot");
        Path root = Objects.requireNonNull(projectRoot, "projectRoot").toAbsolutePath().normalize();
        Map<String, TraceNode> nodes = new LinkedHashMap<>();
        Map<String, TraceEdge> edges = new LinkedHashMap<>();
        snapshot.issues().stream().sorted(issueOrder(root)).forEach(issue ->
                addIssue(snapshot, root, issue, nodes, edges));
        return new TraceabilityGraph(List.copyOf(nodes.values()), List.copyOf(edges.values()));
    }

    private static void addIssue(
            CurrentAnalysisSnapshot snapshot,
            Path root,
            ConsistencyIssue issue,
            Map<String, TraceNode> nodes,
            Map<String, TraceEdge> edges) {
        Optional<ProjectSourceId> source = issue.sourceSpan().map(span -> ProjectSourceId.from(root, span));
        String issueId = issueId(issue, source);
        put(nodes, new TraceNode(
                issueId, TraceNodeKind.ISSUE, issue.ruleId() + ": " + issue.message(), source,
                Optional.of(issue.status()), Optional.of(issue.certainty()), Optional.of(issue.ruleId()),
                Optional.of(issue.severity()), issue.evidence()));

        String sourceNodeId = source.map(value -> "source:" + value.canonical()).orElse("source:unknown");
        put(nodes, new TraceNode(sourceNodeId, TraceNodeKind.SOURCE,
                source.map(ProjectSourceId::projectPath).orElse("Unknown source"), source,
                Optional.empty(), Optional.empty(), List.of()));
        String portableElement = source.map(ProjectSourceId::canonical).orElse("unknown");
        String bdiId = "bdi:" + portableElement;
        put(nodes, new TraceNode(bdiId, TraceNodeKind.BDI_ELEMENT,
                source.map(value -> "BDI element at " + value.projectPath() + ":" + value.beginLine())
                        .orElse("Unknown BDI source element"),
                source,
                Optional.empty(), Optional.empty(), issue.agentId().stream().toList()));
        edge(edges, sourceNodeId, bdiId, TraceRelationKind.DECLARES, issue.certainty(), List.of());

        List<MappingBinding> mappings = matchingMappings(snapshot.mapping().bindings(), issue, source, root);
        if (mappings.isEmpty()) {
            String gapId = "gap:" + digest(issueId + "\nmissing-mapping");
            put(nodes, new TraceNode(gapId, TraceNodeKind.GAP, "Missing confirmed mapping", source,
                    Optional.empty(), Optional.of(issue.certainty()),
                    List.of("No confirmed mapping supports this issue chain")));
            edge(edges, bdiId, gapId, TraceRelationKind.MISSING_MAPPING, issue.certainty(), issue.evidence());
            edge(edges, gapId, issueId, TraceRelationKind.PRODUCES, issue.certainty(), issue.evidence());
            return;
        }

        for (MappingBinding mapping : mappings) {
            String portableSource = portableMappingSource(mapping, root);
            String mappingId = "mapping:" + mapping.kind() + ":" + digest(portableSource + "\n" + mapping.target());
            put(nodes, new TraceNode(mappingId, TraceNodeKind.MAPPING,
                    mapping.kind() + " -> " + mapping.target(), source,
                    Optional.empty(), Optional.empty(), mapping.evidence()));
            edge(edges, bdiId, mappingId, TraceRelationKind.MAPPED_BY, issue.certainty(), mapping.evidence());

            String umlId = "uml:" + mapping.target();
            put(nodes, new TraceNode(umlId, TraceNodeKind.UML_ELEMENT, mapping.target(), Optional.empty(),
                    Optional.empty(), Optional.empty(), List.of("Confirmed mapping target")));
            edge(edges, mappingId, umlId, TraceRelationKind.TARGETS, issue.certainty(), mapping.evidence());
            if (issue.ruleId().startsWith("OCL-")) {
                String oclId = "ocl:" + issue.ruleId() + ":" + mapping.target();
                put(nodes, new TraceNode(oclId, TraceNodeKind.OCL_CONSTRAINT,
                        issue.ruleId() + " on " + mapping.target(), source,
                        Optional.of(issue.status()), Optional.of(issue.certainty()), Optional.of(issue.ruleId()),
                        Optional.of(issue.severity()), issue.evidence()));
                edge(edges, umlId, oclId, TraceRelationKind.EVALUATED_BY, issue.certainty(), issue.evidence());
                edge(edges, oclId, issueId, TraceRelationKind.PRODUCES, issue.certainty(), issue.evidence());
            } else {
                edge(edges, umlId, issueId, TraceRelationKind.PRODUCES, issue.certainty(), issue.evidence());
            }
        }
    }

    private static List<MappingBinding> matchingMappings(
            List<MappingBinding> bindings,
            ConsistencyIssue issue,
            Optional<ProjectSourceId> source,
            Path root) {
        return bindings.stream()
                .filter(binding -> issue.umlElementRef().map(binding.target()::equals).orElse(false)
                        || source.map(value -> portableMappingSource(binding, root).startsWith(value.canonical()))
                                .orElse(false))
                .filter(binding -> issue.planId().map(plan ->
                        !portableMappingSource(binding, root).contains("#plan:")
                                || portableMappingSource(binding, root).contains("#plan:"
                                        + MappingSourceId.stablePlanLabel(plan) + "#"))
                        .orElse(true))
                .sorted(Comparator.comparing(MappingBinding::key))
                .toList();
    }

    private static String portableMappingSource(MappingBinding binding, Path root) {
        if (binding.kind() == MappingKind.BELIEF_ATTRIBUTE) {
            return binding.source();
        }
        int marker = binding.source().indexOf('#');
        String pathPart = marker < 0 ? binding.source() : binding.source().substring(0, marker);
        String suffix = marker < 0 ? "" : binding.source().substring(marker);
        try {
            Path path = Path.of(pathPart);
            return path.isAbsolute()
                    ? ProjectSourceId.fromPath(root, path).canonical() + suffix
                    : binding.source();
        } catch (RuntimeException error) {
            return binding.source();
        }
    }

    private static void put(Map<String, TraceNode> nodes, TraceNode node) {
        nodes.putIfAbsent(node.id(), node);
    }

    private static void edge(
            Map<String, TraceEdge> edges,
            String from,
            String to,
            TraceRelationKind relation,
            IssueCertainty certainty,
            List<String> evidence) {
        String id = "edge:" + relation + ":" + digest(from + "\n" + to);
        edges.putIfAbsent(id, new TraceEdge(id, from, to, relation, certainty, evidence));
    }

    private static String issueId(ConsistencyIssue issue, Optional<ProjectSourceId> source) {
        return "issue:" + issue.ruleId() + ":" + digest(String.join("\n",
                source.map(ProjectSourceId::canonical).orElse("unknown"),
                issue.planId().orElse(""), issue.umlElementRef().orElse(""),
                issue.status().name(), issue.certainty().name(), issue.message()));
    }

    private static Comparator<ConsistencyIssue> issueOrder(Path root) {
        return Comparator.comparing(ConsistencyIssue::ruleId)
                .thenComparing(issue -> issue.sourceSpan()
                        .map(span -> portableSource(root, span))
                        .orElse(""))
                .thenComparing(ConsistencyIssue::message);
    }

    private static String portableSource(Path root, SourceSpan span) {
        return ProjectSourceId.from(root, span).canonical();
    }

    private static String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException error) {
            throw new AssertionError("JVM must provide SHA-256", error);
        }
    }
}
