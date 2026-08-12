package org.tzi.use.plugins.bdi.diagram;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshot;
import org.tzi.use.plugins.bdi.model.environment.ArtifactModel;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMapping;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentModel;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentOperation;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentOperationMapping;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentPropertyMapping;
import org.tzi.use.plugins.bdi.model.mas.MasAgentInstanceModel;
import org.tzi.use.plugins.bdi.model.mas.MasProjectModel;
import org.tzi.use.plugins.bdi.model.mas.MasResourceReference;
import org.tzi.use.plugins.bdi.model.organization.OrganizationMapping;
import org.tzi.use.plugins.bdi.model.organization.OrganizationMappingConfirmation;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel;

/** Projects a JaCaMo project IR into a static, presentation-only MAS overview. */
public final class MasOverviewDiagramBuilder {
    private static final String STATIC_ONLY_NOTE =
            "Static JaCaMo project analysis | No JaCaMo runtime | No Moise enactment | No live CArtAgO state";

    public DiagramModel build(MasProjectModel project, CurrentAnalysisSnapshot snapshot, Path projectRoot) {
        return build(project, snapshot, projectRoot, Optional.empty(), List.of(), List.of());
    }

    public DiagramModel build(
            MasProjectModel project,
            CurrentAnalysisSnapshot snapshot,
            Path projectRoot,
            Optional<EnvironmentModel> environment,
            List<OrganizationMapping> organizationMappings,
            List<EnvironmentMapping> environmentMappings) {
        Objects.requireNonNull(project, "project");
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(projectRoot, "projectRoot").toAbsolutePath().normalize();
        return new Projection(
                project,
                snapshot,
                Objects.requireNonNull(environment, "environment"),
                List.copyOf(Objects.requireNonNull(organizationMappings, "organizationMappings")),
                List.copyOf(Objects.requireNonNull(environmentMappings, "environmentMappings")))
                .build();
    }

    private static final class Projection {
        private final MasProjectModel project;
        private final CurrentAnalysisSnapshot snapshot;
        private final Optional<EnvironmentModel> environment;
        private final List<OrganizationMapping> organizationMappings;
        private final List<EnvironmentMapping> environmentMappings;
        private final Map<String, DiagramNode> nodes = new LinkedHashMap<>();
        private final Map<String, DiagramEdge> edges = new LinkedHashMap<>();
        private final Set<String> groupMembers = new LinkedHashSet<>();
        private final Map<String, DiagramNode> organizationElements = new LinkedHashMap<>();
        private final Map<String, DiagramNode> environmentOperations = new LinkedHashMap<>();
        private final Map<String, DiagramNode> environmentArtifacts = new LinkedHashMap<>();

        private Projection(
                MasProjectModel project,
                CurrentAnalysisSnapshot snapshot,
                Optional<EnvironmentModel> environment,
                List<OrganizationMapping> organizationMappings,
                List<EnvironmentMapping> environmentMappings) {
            this.project = project;
            this.snapshot = snapshot;
            this.environment = environment;
            this.organizationMappings = organizationMappings;
            this.environmentMappings = environmentMappings;
        }

        private DiagramModel build() {
            DiagramNode projectNode = addProjectNode();
            addAgents(projectNode);
            addResources(projectNode);
            addOrganizations(projectNode);
            environment.ifPresent(value -> addEnvironment(projectNode, value));
            addOrganizationMappings();
            addEnvironmentMappings();
            return new DiagramModel(nodes.values().stream().toList(), edges.values().stream().toList(),
                    List.of(new DiagramGroup(
                            DiagramSelectionRef.of("mas-group", project.source().canonical()),
                            "MAS Overview: " + project.name(),
                            groupMembers.stream().toList(),
                            Map.of(
                                    "layer", "MAS",
                                    "staticOnly", "true",
                                    "legend", STATIC_ONLY_NOTE))));
        }

        private DiagramNode addProjectNode() {
            DiagramNode projectNode = add(new DiagramNode(
                    DiagramNodeType.MAS_PROJECT,
                    DiagramSelectionRef.of("mas-project", project.source().canonical()),
                    "MAS Project: " + project.name(),
                    Optional.of(project.source()),
                    Optional.empty(),
                    attributes("MAS",
                            Map.of(
                                    "staticOnly", "true",
                                    "agentCount", Integer.toString(project.agents().size()),
                                    "issueCount", Integer.toString(snapshot.issueCount()),
                                    "legend", STATIC_ONLY_NOTE))));
            groupMembers.add(projectNode.id());
            DiagramNode legend = add(new DiagramNode(
                    DiagramNodeType.MAS_PROJECT,
                    DiagramSelectionRef.of("mas-legend", project.source().canonical() + "#static-only"),
                    "Static analysis only: no runtime / no enactment / no live environment",
                    Optional.of(project.source()),
                    Optional.empty(),
                    attributes("MAS", Map.of("staticOnly", "true", "legend", "true"))));
            groupMembers.add(legend.id());
            addEdge(DiagramEdgeType.OWNS, projectNode, legend, "static-only", Map.of("staticOnly", "true"));
            return projectNode;
        }

        private void addAgents(DiagramNode projectNode) {
            for (MasAgentInstanceModel agent : project.agents()) {
                String status = agent.status().name();
                DiagramNode instance = add(new DiagramNode(
                        DiagramNodeType.AGENT,
                        DiagramSelectionRef.of("mas-agent", project.source().canonical() + "#" + agent.name()),
                        "Agent instance: " + agent.name() + statusSuffix(status),
                        Optional.of(agent.source()),
                        Optional.empty(),
                        attributes("BDI", Map.of(
                                "masLayer", "BDI",
                                "agentStatus", status,
                                "staticOnly", "true"))));
                groupMembers.add(instance.id());
                addEdge(DiagramEdgeType.OWNS, projectNode, instance, "agent-" + agent.name(),
                        Map.of("layer", "BDI"));

                DiagramNode source = add(new DiagramNode(
                        DiagramNodeType.TRACE_SOURCE,
                        DiagramSelectionRef.of("mas-agent-source", agent.source().canonical()),
                        "Agent source: " + agent.source().projectPath(),
                        Optional.of(agent.source()),
                        Optional.empty(),
                        attributes("BDI", Map.of("staticOnly", "true"))));
                groupMembers.add(source.id());
                addEdge(DiagramEdgeType.OWNS, instance, source, "agent-source", Map.of("layer", "BDI"));
            }
        }

        private void addResources(DiagramNode projectNode) {
            for (MasResourceReference resource : project.resources()) {
                String status = resource.status().name();
                DiagramNodeType type = resource.kind().name().equals("ORGANIZATION")
                        || resource.kind().name().equals("INSTITUTION")
                                ? DiagramNodeType.ORGANIZATION
                                : DiagramNodeType.ARTIFACT;
                String layer = type == DiagramNodeType.ARTIFACT ? "ENVIRONMENT" : "ORGANIZATION";
                DiagramNode node = add(new DiagramNode(
                        type,
                        DiagramSelectionRef.of("mas-resource", resource.kind().name() + ":" + resource.name()),
                        resource.kind() + ": " + resource.name() + statusSuffix(status),
                        resource.source(),
                        Optional.empty(),
                        attributes(layer, Map.of(
                                "resourceKind", resource.kind().name(),
                                "resourceStatus", status,
                                "staticOnly", "true"))));
                groupMembers.add(node.id());
                addEdge(DiagramEdgeType.OWNS, projectNode, node, "resource-" + resource.kind() + "-" + resource.name(),
                        Map.of("layer", layer));
            }
        }

        private void addOrganizations(DiagramNode projectNode) {
            for (OrganizationModel organization : project.organizations()) {
                DiagramNode organizationNode = add(new DiagramNode(
                        DiagramNodeType.ORGANIZATION,
                        DiagramSelectionRef.of("mas-organization", organization.id()),
                        "Organization: " + organization.id(),
                        Optional.of(organization.source()),
                        Optional.empty(),
                        attributes("ORGANIZATION", Map.of("staticOnly", "true", "status", "NORMALIZED"))));
                groupMembers.add(organizationNode.id());
                addEdge(DiagramEdgeType.OWNS, projectNode, organizationNode, "organization-" + organization.id(),
                        Map.of("layer", "ORGANIZATION"));

                for (OrganizationModel.Role role : organization.roles()) {
                    DiagramNode roleNode = add(new DiagramNode(
                            DiagramNodeType.ROLE,
                            DiagramSelectionRef.of("mas-role", role.qualifiedId()),
                            "Role: " + role.qualifiedId(),
                            Optional.of(role.span().source()),
                            Optional.empty(),
                            attributes("ORGANIZATION", Map.of("staticOnly", "true"))));
                    organizationElements.put(role.qualifiedId(), roleNode);
                    groupMembers.add(roleNode.id());
                    addEdge(DiagramEdgeType.OWNS, organizationNode, roleNode, "role-" + role.qualifiedId(),
                            Map.of("layer", "ORGANIZATION"));
                }
                for (OrganizationModel.Scheme scheme : organization.schemes()) {
                    DiagramNode schemeNode = add(new DiagramNode(
                            DiagramNodeType.ORGANIZATION,
                            DiagramSelectionRef.of("mas-scheme", scheme.qualifiedId()),
                            "Scheme: " + scheme.qualifiedId(),
                            Optional.of(scheme.span().source()),
                            Optional.empty(),
                            attributes("ORGANIZATION", Map.of("staticOnly", "true"))));
                    groupMembers.add(schemeNode.id());
                    addEdge(DiagramEdgeType.OWNS, organizationNode, schemeNode, "scheme-" + scheme.qualifiedId(),
                            Map.of("layer", "ORGANIZATION"));
                    for (OrganizationModel.Mission mission : scheme.missions()) {
                        DiagramNode missionNode = add(new DiagramNode(
                                DiagramNodeType.MISSION,
                                DiagramSelectionRef.of("mas-mission", mission.qualifiedId()),
                                "Mission: " + mission.qualifiedId(),
                                Optional.of(mission.span().source()),
                                Optional.empty(),
                                attributes("ORGANIZATION", Map.of("staticOnly", "true"))));
                        organizationElements.put(mission.qualifiedId(), missionNode);
                        groupMembers.add(missionNode.id());
                        addEdge(DiagramEdgeType.OWNS, schemeNode, missionNode,
                                "mission-" + mission.qualifiedId(), Map.of("layer", "ORGANIZATION"));
                    }
                }
                for (OrganizationModel.UnsupportedFeature unsupported : organization.unsupportedFeatures()) {
                    DiagramNode unsupportedNode = add(new DiagramNode(
                            DiagramNodeType.ORGANIZATION,
                            DiagramSelectionRef.of("mas-unsupported", organization.id() + "#" + unsupported.code()),
                            "Unsupported organization feature: " + unsupported.code(),
                            Optional.of(unsupported.span().source()),
                            Optional.empty(),
                            attributes("ORGANIZATION", Map.of(
                                    "staticOnly", "true",
                                    "status", "UNSUPPORTED",
                                    "detail", unsupported.detail()))));
                    groupMembers.add(unsupportedNode.id());
                    addEdge(DiagramEdgeType.OWNS, organizationNode, unsupportedNode,
                            "unsupported-" + unsupported.code(), Map.of("layer", "ORGANIZATION"));
                }
            }
        }

        private void addEnvironment(DiagramNode projectNode, EnvironmentModel model) {
            for (ArtifactModel artifact : model.artifacts()) {
                DiagramNode artifactNode = add(new DiagramNode(
                        DiagramNodeType.ARTIFACT,
                        DiagramSelectionRef.of("mas-artifact", artifact.reference()),
                        "Artifact: " + artifact.reference(),
                        Optional.empty(),
                        Optional.empty(),
                        attributes("ENVIRONMENT", Map.of(
                                "artifactType", artifact.typeName(),
                                "staticOnly", "true",
                                "status", "NORMALIZED"))));
                environmentArtifacts.put(artifact.reference(), artifactNode);
                groupMembers.add(artifactNode.id());
                addEdge(DiagramEdgeType.USES_ARTIFACT, projectNode, artifactNode,
                        "artifact-" + artifact.reference(), Map.of("layer", "ENVIRONMENT"));
                for (EnvironmentOperation operation : artifact.operations()) {
                    DiagramNode operationNode = add(new DiagramNode(
                            DiagramNodeType.ARTIFACT_OPERATION,
                            DiagramSelectionRef.of("mas-artifact-operation",
                                    artifact.reference() + "#" + operation.signature()),
                            "Artifact operation: " + artifact.reference() + "#" + operation.signature(),
                            Optional.empty(),
                            Optional.empty(),
                            attributes("ENVIRONMENT", Map.of(
                                    "operation", operation.signature(),
                                    "staticOnly", "true"))));
                    environmentOperations.put(artifact.reference() + "#" + operation.signature(), operationNode);
                    groupMembers.add(operationNode.id());
                    addEdge(DiagramEdgeType.HAS_OPERATION, artifactNode, operationNode,
                            "operation-" + operation.signature(), Map.of("layer", "ENVIRONMENT"));
                }
            }
        }

        private void addOrganizationMappings() {
            for (OrganizationMapping mapping : organizationMappings) {
                DiagramNode source = organizationElements.get(mapping.sourceQualifiedId());
                if (mapping.confirmation() != OrganizationMappingConfirmation.CONFIRMED) {
                    DiagramNode gap = add(new DiagramNode(
                            DiagramNodeType.GAP,
                            DiagramSelectionRef.of("mas-organization-gap", mapping.key()),
                            "Unconfirmed organization mapping: " + mapping.sourceQualifiedId(),
                            Optional.empty(),
                            Optional.empty(),
                            attributes("ORGANIZATION", Map.of(
                                    "mappingStatus", "MISSING",
                                    "mappingKind", mapping.getClass().getSimpleName()))));
                    groupMembers.add(gap.id());
                    if (source != null) {
                        addEdge(DiagramEdgeType.MISSING_MAPPING, source, gap, "candidate-" + mapping.key(),
                                Map.of("mappingStatus", "MISSING", "layer", "ORGANIZATION"));
                    }
                    continue;
                }
                if (source == null) {
                    addMissingOrganizationSource(mapping);
                    continue;
                }
                DiagramNodeType targetType = mapping instanceof org.tzi.use.plugins.bdi.model.organization.OrganizationRoleMapping
                        ? DiagramNodeType.UML_CLASS
                        : mapping instanceof org.tzi.use.plugins.bdi.model.organization.OrganizationMissionMapping
                                ? DiagramNodeType.UML_OPERATION
                                : DiagramNodeType.OCL_CONSTRAINT;
                DiagramNode target = add(new DiagramNode(
                        targetType,
                        DiagramSelectionRef.of("mas-uml", mapping.key()),
                        mapping.target(),
                        Optional.empty(),
                        Optional.empty(),
                        attributes("UML", Map.of(
                                "mappingStatus", "CONFIRMED",
                                "mappingKind", mapping.getClass().getSimpleName(),
                                "staticOnly", "true"))));
                groupMembers.add(target.id());
                addEdge(DiagramEdgeType.MAPS_TO, source, target, "organization-map-" + mapping.key(),
                        Map.of("mappingStatus", "CONFIRMED", "layer", "UML"));
            }
        }

        private void addMissingOrganizationSource(OrganizationMapping mapping) {
            DiagramNode gap = add(new DiagramNode(
                    DiagramNodeType.GAP,
                    DiagramSelectionRef.of("mas-organization-gap", "missing-" + mapping.key()),
                    "Missing organization element: " + mapping.sourceQualifiedId(),
                    Optional.empty(),
                    Optional.empty(),
                    attributes("ORGANIZATION", Map.of(
                            "mappingStatus", "MISSING",
                            "mappingKind", mapping.getClass().getSimpleName()))));
            groupMembers.add(gap.id());
        }

        private void addEnvironmentMappings() {
            for (EnvironmentMapping mapping : environmentMappings) {
                if (mapping instanceof EnvironmentOperationMapping operation) {
                    DiagramNode source = findEnvironmentOperation(operation);
                    DiagramNode artifact = environmentArtifacts.get(operation.workspace() + "/" + operation.artifact());
                    addEnvironmentTarget(source, artifact, operation.umlTarget(), DiagramNodeType.UML_OPERATION,
                            mapping.key(), "Missing environment operation: " + operation.workspace() + "/"
                                    + operation.artifact() + "#" + operation.operation());
                } else if (mapping instanceof EnvironmentPropertyMapping property) {
                    DiagramNode artifact = environmentArtifacts.get(property.workspace() + "/" + property.artifact());
                    addEnvironmentTarget(artifact, artifact, property.umlTarget(), DiagramNodeType.UML_ATTRIBUTE,
                            mapping.key(), "Missing environment artifact: " + property.workspace() + "/"
                                    + property.artifact());
                }
            }
        }

        private DiagramNode findEnvironmentOperation(EnvironmentOperationMapping mapping) {
            String prefix = mapping.workspace() + "/" + mapping.artifact() + "#" + mapping.operation() + "/";
            return environmentOperations.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(prefix))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }

        private void addEnvironmentTarget(
                DiagramNode source,
                DiagramNode fallbackSource,
                String targetReference,
                DiagramNodeType targetType,
                String key,
                String missingDescription) {
            if (source == null) {
                DiagramNode gap = add(new DiagramNode(
                        DiagramNodeType.GAP,
                        DiagramSelectionRef.of("mas-environment-gap", key),
                        missingDescription,
                        Optional.empty(),
                        Optional.empty(),
                        attributes("ENVIRONMENT", Map.of(
                                "mappingStatus", "MISSING",
                                "mappingKind", targetType.name(),
                                "staticOnly", "true"))));
                groupMembers.add(gap.id());
                if (fallbackSource != null) {
                    addEdge(DiagramEdgeType.MISSING_MAPPING, fallbackSource, gap, "missing-environment-" + key,
                            Map.of("mappingStatus", "MISSING", "layer", "ENVIRONMENT"));
                }
                return;
            }
            DiagramNode target = add(new DiagramNode(
                    targetType,
                    DiagramSelectionRef.of("mas-uml", key),
                    targetReference,
                    Optional.empty(),
                    Optional.empty(),
                    attributes("UML", Map.of("mappingStatus", "CONFIRMED", "staticOnly", "true"))));
            groupMembers.add(target.id());
            addEdge(DiagramEdgeType.MAPS_TO, source, target, "environment-map-" + key,
                    Map.of("mappingStatus", "CONFIRMED", "layer", "UML"));
        }

        private DiagramNode add(DiagramNode node) {
            DiagramNode previous = nodes.putIfAbsent(node.id(), node);
            if (previous != null && !previous.equals(node)) {
                throw new IllegalArgumentException("Conflicting MAS overview node identity: " + node.id());
            }
            return previous == null ? node : previous;
        }

        private void addEdge(DiagramEdgeType type, DiagramNode source, DiagramNode target, String key,
                Map<String, String> attributes) {
            DiagramEdge edge = new DiagramEdge(
                    type,
                    source.id(),
                    target.id(),
                    DiagramSelectionRef.of("mas-edge", source.id() + "#" + key + "#" + target.id()),
                    Optional.empty(),
                    attributes);
            DiagramEdge previous = edges.putIfAbsent(edge.id(), edge);
            if (previous != null && !previous.equals(edge)) {
                throw new IllegalArgumentException("Conflicting MAS overview edge identity: " + edge.id());
            }
        }
    }

    private static Map<String, String> attributes(String layer, Map<String, String> values) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("layer", layer);
        result.putAll(values);
        return result;
    }

    private static String statusSuffix(String status) {
        return "IMPORTED".equals(status) || "NORMALIZED".equals(status) ? "" : " [" + status + "]";
    }
}
