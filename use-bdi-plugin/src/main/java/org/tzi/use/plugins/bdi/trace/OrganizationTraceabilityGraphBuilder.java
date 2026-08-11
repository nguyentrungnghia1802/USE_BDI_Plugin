package org.tzi.use.plugins.bdi.trace;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.organization.OrganizationCardinalityMapping;
import org.tzi.use.plugins.bdi.model.organization.OrganizationMapping;
import org.tzi.use.plugins.bdi.model.organization.OrganizationMappingConfirmation;
import org.tzi.use.plugins.bdi.model.organization.OrganizationMissionMapping;
import org.tzi.use.plugins.bdi.model.organization.OrganizationRoleMapping;
import org.tzi.use.plugins.bdi.validation.organization.OrganizationFinding;
import org.tzi.use.plugins.bdi.validation.organization.OrganizationValidationContext;

/** Contributes static organization findings without exposing Moise concrete types. */
public final class OrganizationTraceabilityGraphBuilder {
    public TraceabilityGraph build(
            OrganizationValidationContext context,
            List<OrganizationFinding> findings) {
        List<TraceNode> nodes = new ArrayList<>();
        List<TraceEdge> edges = new ArrayList<>();
        for (OrganizationFinding finding : findings) {
            addFinding(context, finding, nodes, edges);
        }
        return new TraceabilityGraph(nodes, edges);
    }

    private static void addFinding(
            OrganizationValidationContext context,
            OrganizationFinding finding,
            List<TraceNode> nodes,
            List<TraceEdge> edges) {
        OrganizationMapping mapping = finding.mapping();
        String organizationId = "organization:" + context.organization().id();
        nodes.add(new TraceNode(organizationId, TraceNodeKind.ORGANIZATION,
                context.organization().id(), Optional.of(context.organization().source()),
                Optional.empty(), Optional.empty(), List.of()));

        boolean sourceExists = sourceExists(context, mapping);
        String elementId = sourceExists
                ? "organization-element:" + mapping.sourceQualifiedId()
                : "organization-gap:" + digest(mapping.sourceQualifiedId());
        nodes.add(new TraceNode(elementId, sourceExists ? elementKind(mapping) : TraceNodeKind.GAP,
                sourceExists ? mapping.sourceQualifiedId()
                        : "Missing organization source: " + mapping.sourceQualifiedId(),
                finding.organizationSource().map(value -> value.source()), Optional.empty(),
                Optional.of(finding.issue().certainty()), finding.issue().evidence()));
        edges.add(edge(organizationId, elementId,
                sourceExists ? TraceRelationKind.DECLARES : TraceRelationKind.MISSING_TARGET, finding));

        String issueId = "organization-issue:" + finding.issue().ruleId() + ":" + digest(mapping.key());
        nodes.add(new TraceNode(issueId, TraceNodeKind.ISSUE,
                finding.issue().ruleId() + ": " + finding.issue().message(),
                finding.organizationSource().map(value -> value.source()),
                Optional.of(finding.issue().status()), Optional.of(finding.issue().certainty()),
                finding.issue().evidence()));
        if (!sourceExists) {
            edges.add(edge(elementId, issueId, TraceRelationKind.PRODUCES, finding));
            return;
        }

        if (mapping.confirmation() != OrganizationMappingConfirmation.CONFIRMED) {
            String gapId = "organization-mapping-gap:" + digest(mapping.key());
            nodes.add(new TraceNode(gapId, TraceNodeKind.GAP, "Unconfirmed organization mapping",
                    finding.organizationSource().map(value -> value.source()), Optional.empty(),
                    Optional.of(finding.issue().certainty()), mapping.evidence()));
            edges.add(edge(elementId, gapId, TraceRelationKind.MISSING_MAPPING, finding));
            edges.add(edge(gapId, issueId, TraceRelationKind.PRODUCES, finding));
            return;
        }

        String mappingId = "organization-mapping:" + digest(mapping.key());
        nodes.add(new TraceNode(mappingId, TraceNodeKind.MAPPING,
                mapping.sourceQualifiedId() + " -> " + mapping.target(),
                finding.organizationSource().map(value -> value.source()), Optional.empty(),
                Optional.empty(), mapping.evidence()));
        edges.add(edge(elementId, mappingId, TraceRelationKind.MAPPED_BY, finding));

        boolean targetExists = targetExists(context, mapping);
        String targetId = targetExists ? targetId(mapping) : "organization-target-gap:" + digest(mapping.target());
        nodes.add(new TraceNode(targetId, targetExists ? targetKind(mapping) : TraceNodeKind.GAP,
                targetExists ? mapping.target() : "Missing UML/OCL target: " + mapping.target(),
                Optional.empty(), Optional.empty(), Optional.of(finding.issue().certainty()), mapping.evidence()));
        edges.add(edge(mappingId, targetId,
                targetExists ? TraceRelationKind.ORGANIZATION_TARGET : TraceRelationKind.MISSING_TARGET, finding));
        edges.add(edge(targetId, issueId, TraceRelationKind.PRODUCES, finding));
    }

    private static boolean sourceExists(OrganizationValidationContext context, OrganizationMapping mapping) {
        if (mapping instanceof OrganizationRoleMapping) {
            return context.organization().roles().stream()
                    .anyMatch(role -> role.qualifiedId().equals(mapping.sourceQualifiedId()));
        }
        if (mapping instanceof OrganizationMissionMapping) {
            return context.organization().schemes().stream().flatMap(scheme -> scheme.missions().stream())
                    .anyMatch(mission -> mission.qualifiedId().equals(mapping.sourceQualifiedId()));
        }
        OrganizationCardinalityMapping cardinality = (OrganizationCardinalityMapping) mapping;
        return context.organization().groups().stream()
                .filter(group -> group.qualifiedId().equals(cardinality.groupQualifiedId()))
                .flatMap(group -> group.roles().stream())
                .anyMatch(role -> role.roleQualifiedId().equals(mapping.sourceQualifiedId()));
    }

    private static boolean targetExists(OrganizationValidationContext context, OrganizationMapping mapping) {
        if (mapping instanceof OrganizationRoleMapping) {
            return context.uml().classes().stream().anyMatch(type -> type.reference().equals(mapping.target()));
        }
        if (mapping instanceof OrganizationMissionMapping) {
            return context.uml().operations().stream()
                    .anyMatch(operation -> operation.reference().equals(mapping.target()));
        }
        return context.uml().classInvariants().stream()
                .anyMatch(constraint -> constraint.reference().equals(mapping.target()));
    }

    private static TraceNodeKind elementKind(OrganizationMapping mapping) {
        if (mapping instanceof OrganizationRoleMapping) {
            return TraceNodeKind.ORGANIZATION_ROLE;
        }
        if (mapping instanceof OrganizationMissionMapping) {
            return TraceNodeKind.ORGANIZATION_MISSION;
        }
        return TraceNodeKind.ORGANIZATION_CARDINALITY;
    }

    private static TraceNodeKind targetKind(OrganizationMapping mapping) {
        return mapping instanceof OrganizationCardinalityMapping
                ? TraceNodeKind.OCL_CONSTRAINT : TraceNodeKind.UML_ELEMENT;
    }

    private static String targetId(OrganizationMapping mapping) {
        return (mapping instanceof OrganizationCardinalityMapping ? "ocl:" : "uml:") + mapping.target();
    }

    private static TraceEdge edge(
            String from,
            String to,
            TraceRelationKind relation,
            OrganizationFinding finding) {
        String id = "edge:" + relation + ":" + digest(from + "\n" + to);
        return new TraceEdge(id, from, to, relation,
                finding.issue().certainty(), finding.issue().evidence());
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
