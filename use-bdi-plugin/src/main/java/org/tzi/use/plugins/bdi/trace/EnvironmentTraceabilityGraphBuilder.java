package org.tzi.use.plugins.bdi.trace;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.environment.ArtifactModel;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentOperationMapping;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentPropertyMapping;
import org.tzi.use.plugins.bdi.validation.environment.EnvironmentFinding;
import org.tzi.use.plugins.bdi.validation.environment.EnvironmentValidationContext;

/** Contributes static CArtAgO findings without exposing CArtAgO concrete types. */
public final class EnvironmentTraceabilityGraphBuilder {
    public TraceabilityGraph build(
            EnvironmentValidationContext context,
            List<EnvironmentFinding> findings) {
        List<TraceNode> nodes = new ArrayList<>();
        List<TraceEdge> edges = new ArrayList<>();
        for (EnvironmentFinding finding : findings) {
            addFinding(context, finding, nodes, edges);
        }
        return new TraceabilityGraph(nodes, edges);
    }

    private static void addFinding(
            EnvironmentValidationContext context,
            EnvironmentFinding finding,
            List<TraceNode> nodes,
            List<TraceEdge> edges) {
        String artifactRef = finding.mapping().workspace() + "/" + finding.mapping().artifact();
        String artifactId = "environment-artifact:" + artifactRef;
        nodes.add(node(artifactId, TraceNodeKind.ENVIRONMENT_ARTIFACT, artifactRef));
        String issueId = "environment-issue:" + finding.issue().ruleId() + ":" + digest(finding.mapping().key());
        nodes.add(new TraceNode(issueId, TraceNodeKind.ISSUE,
                finding.issue().ruleId() + ": " + finding.issue().message(), Optional.empty(),
                Optional.of(finding.issue().status()), Optional.of(finding.issue().certainty()),
                Optional.of(finding.issue().ruleId()), Optional.of(finding.issue().severity()),
                finding.issue().evidence()));

        Optional<ArtifactModel> artifact = context.environment()
                .artifact(finding.mapping().workspace(), finding.mapping().artifact());
        String elementLabel;
        TraceNodeKind elementKind;
        boolean exists;
        if (finding.mapping() instanceof EnvironmentOperationMapping operation) {
            elementLabel = artifactRef + "#" + operation.operation() + "/" + operation.actionArity();
            elementKind = TraceNodeKind.ARTIFACT_OPERATION;
            exists = artifact.stream().flatMap(value -> value.operations().stream())
                    .anyMatch(value -> value.name().equals(operation.operation())
                            && value.arity() == operation.actionArity());
        } else {
            EnvironmentPropertyMapping property = (EnvironmentPropertyMapping) finding.mapping();
            elementLabel = artifactRef + "#" + property.property() + "/" + property.propertyArity();
            elementKind = TraceNodeKind.OBSERVABLE_PROPERTY;
            exists = artifact.stream().flatMap(value -> value.observableProperties().stream())
                    .anyMatch(value -> value.name().equals(property.property())
                            && value.arity() == property.propertyArity());
        }
        String elementId = exists ? "environment-element:" + elementLabel
                : "environment-gap:" + digest(elementLabel);
        nodes.add(new TraceNode(elementId, exists ? elementKind : TraceNodeKind.GAP,
                exists ? elementLabel : "Missing environment target: " + elementLabel,
                Optional.empty(), Optional.empty(), Optional.of(finding.issue().certainty()),
                finding.issue().evidence()));
        edges.add(edge(artifactId, elementId,
                exists ? TraceRelationKind.DECLARES : TraceRelationKind.MISSING_TARGET, finding));

        if (!exists) {
            edges.add(edge(elementId, issueId, TraceRelationKind.PRODUCES, finding));
            return;
        }
        String umlId = "uml:" + finding.mapping().umlTarget();
        nodes.add(node(umlId, TraceNodeKind.UML_ELEMENT, finding.mapping().umlTarget()));
        edges.add(edge(elementId, umlId, TraceRelationKind.ENVIRONMENT_TARGET, finding));
        edges.add(edge(umlId, issueId, TraceRelationKind.PRODUCES, finding));
    }

    private static TraceNode node(String id, TraceNodeKind kind, String label) {
        return new TraceNode(id, kind, label, Optional.empty(), Optional.empty(), Optional.empty(), List.of());
    }

    private static TraceEdge edge(
            String from,
            String to,
            TraceRelationKind relation,
            EnvironmentFinding finding) {
        String id = "edge:" + relation + ":" + digest(from + "\n" + to);
        return new TraceEdge(id, from, to, relation, finding.issue().certainty(), finding.issue().evidence());
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
