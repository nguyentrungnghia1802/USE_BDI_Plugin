package org.tzi.use.plugins.bdi.application;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.tzi.use.plugins.bdi.importer.JaCaMoProjectParserAdapter;
import org.tzi.use.plugins.bdi.importer.MasProjectDiagnostic;
import org.tzi.use.plugins.bdi.importer.MasProjectDiagnosticSeverity;
import org.tzi.use.plugins.bdi.importer.MasProjectParseException;
import org.tzi.use.plugins.bdi.importer.ParsedMasAgent;
import org.tzi.use.plugins.bdi.importer.ParsedMasProject;
import org.tzi.use.plugins.bdi.importer.ParsedMasResource;
import org.tzi.use.plugins.bdi.model.mas.MasAgentImportStatus;
import org.tzi.use.plugins.bdi.model.mas.MasAgentInstanceModel;
import org.tzi.use.plugins.bdi.model.mas.MasProjectModel;
import org.tzi.use.plugins.bdi.model.mas.MasResourceReference;
import org.tzi.use.plugins.bdi.model.mas.MasResourceStatus;
import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;

/** Imports static `.jcm` declarations and delegates every agent source to the ASL pipeline. */
public final class MasProjectImportService {
    private final JaCaMoProjectParserAdapter parser;
    private final BdiImportService bdiImportService;

    public MasProjectImportService() {
        this(new JaCaMoProjectParserAdapter(), new BdiImportService());
    }

    MasProjectImportService(JaCaMoProjectParserAdapter parser, BdiImportService bdiImportService) {
        this.parser = Objects.requireNonNull(parser, "parser");
        this.bdiImportService = Objects.requireNonNull(bdiImportService, "bdiImportService");
    }

    public MasProjectImportResult importProject(Path projectFile) {
        Path file = Objects.requireNonNull(projectFile, "projectFile").toAbsolutePath().normalize();
        Path parent = file.getParent();
        if (parent == null) {
            return parseFailure(file, "JaCaMo project has no parent directory");
        }
        return importProject(parent, file);
    }

    public MasProjectImportResult importProject(Path projectRoot, Path projectFile) {
        Path root = Objects.requireNonNull(projectRoot, "projectRoot").toAbsolutePath().normalize();
        Path file = Objects.requireNonNull(projectFile, "projectFile").toAbsolutePath().normalize();
        if (!file.startsWith(root) || file.equals(root)) {
            return outsideRoot(file, root);
        }

        ParsedMasProject parsed;
        try {
            parsed = parser.parse(file);
        } catch (MasProjectParseException error) {
            return new MasProjectImportResult(
                    Optional.empty(), bdiImportService.importFiles(List.of()), List.of(error.diagnostic()));
        }

        List<MasProjectDiagnostic> diagnostics = new ArrayList<>();
        Map<String, ParsedMasAgent> uniqueAgents = new LinkedHashMap<>();
        for (ParsedMasAgent agent : parsed.agents()) {
            if (!agent.source().startsWith(root) || agent.source().equals(root)) {
                diagnostics.add(diagnostic(
                        MasProjectDiagnostic.SOURCE_OUTSIDE_ROOT,
                        MasProjectDiagnosticSeverity.ERROR,
                        agent.source(),
                        "Agent source resolves outside project root: " + root));
                continue;
            }
            if (uniqueAgents.putIfAbsent(agent.name(), agent) != null) {
                diagnostics.add(diagnostic(
                        MasProjectDiagnostic.DUPLICATE_AGENT,
                        MasProjectDiagnosticSeverity.ERROR,
                        file,
                        "Duplicate JaCaMo agent instance: " + agent.name()));
            }
        }

        Set<Path> importableSources = new LinkedHashSet<>();
        for (ParsedMasAgent agent : uniqueAgents.values()) {
            if (Files.isRegularFile(agent.source())) {
                importableSources.add(agent.source());
            } else {
                diagnostics.add(diagnostic(
                        MasProjectDiagnostic.MISSING_AGENT_SOURCE,
                        MasProjectDiagnosticSeverity.ERROR,
                        agent.source(),
                        "AgentSpeak source does not exist: " + agent.source()));
            }
        }
        BdiImportSnapshot snapshot = bdiImportService.importFiles(List.copyOf(importableSources));
        Set<Path> invalidSources = new LinkedHashSet<>();
        snapshot.diagnostics().forEach(diagnostic -> invalidSources.add(diagnostic.source()));
        for (Path source : invalidSources) {
            diagnostics.add(diagnostic(
                    MasProjectDiagnostic.INVALID_AGENT_SOURCE,
                    MasProjectDiagnosticSeverity.ERROR,
                    source,
                    "AgentSpeak import failed; see "
                            + snapshot.diagnostics().stream()
                                    .filter(item -> item.source().equals(source))
                                    .findFirst().orElseThrow().code()));
        }

        List<MasAgentInstanceModel> agents = uniqueAgents.values().stream()
                .map(agent -> new MasAgentInstanceModel(
                        agent.name(),
                        ProjectSourceId.fromPath(root, agent.source()),
                        status(agent.source(), invalidSources)))
                .toList();
        List<MasResourceReference> resources = new ArrayList<>();
        for (ParsedMasResource resource : parsed.resources()) {
            Optional<ProjectSourceId> sourceId = resource.source().flatMap(source -> {
                if (!source.startsWith(root) || source.equals(root)) {
                    diagnostics.add(diagnostic(
                            MasProjectDiagnostic.SOURCE_OUTSIDE_ROOT,
                            MasProjectDiagnosticSeverity.ERROR,
                            source,
                            "Resource source resolves outside project root: " + root));
                    return Optional.empty();
                }
                return Optional.of(ProjectSourceId.fromPath(root, source));
            });
            resources.add(new MasResourceReference(
                    resource.kind(), resource.name(), sourceId, MasResourceStatus.UNSUPPORTED));
            diagnostics.add(diagnostic(
                    MasProjectDiagnostic.UNSUPPORTED_RESOURCE,
                    MasProjectDiagnosticSeverity.WARNING,
                    resource.source().orElse(file),
                    resource.kind() + " resource is retained but not semantically imported: "
                            + resource.name()));
        }

        MasProjectModel project = new MasProjectModel(
                parsed.name(), ProjectSourceId.fromPath(root, file), agents, resources);
        return new MasProjectImportResult(Optional.of(project), snapshot, diagnostics);
    }

    private static MasAgentImportStatus status(Path source, Set<Path> invalidSources) {
        if (!Files.isRegularFile(source)) {
            return MasAgentImportStatus.MISSING;
        }
        return invalidSources.contains(source)
                ? MasAgentImportStatus.INVALID
                : MasAgentImportStatus.IMPORTED;
    }

    private MasProjectImportResult outsideRoot(Path file, Path root) {
        return new MasProjectImportResult(
                Optional.empty(),
                bdiImportService.importFiles(List.of()),
                List.of(diagnostic(
                        MasProjectDiagnostic.SOURCE_OUTSIDE_ROOT,
                        MasProjectDiagnosticSeverity.ERROR,
                        file,
                        "JaCaMo project is outside project root: " + root)));
    }

    private MasProjectImportResult parseFailure(Path file, String message) {
        return new MasProjectImportResult(
                Optional.empty(),
                bdiImportService.importFiles(List.of()),
                List.of(diagnostic(
                        MasProjectDiagnostic.PARSE_ERROR,
                        MasProjectDiagnosticSeverity.ERROR,
                        file,
                        message)));
    }

    private static MasProjectDiagnostic diagnostic(
            String code, MasProjectDiagnosticSeverity severity, Path source, String message) {
        return new MasProjectDiagnostic(code, severity, source, 0, 0, message);
    }
}
