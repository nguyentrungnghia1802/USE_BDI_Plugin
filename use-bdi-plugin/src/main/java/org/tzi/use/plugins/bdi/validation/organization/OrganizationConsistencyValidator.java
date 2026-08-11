package org.tzi.use.plugins.bdi.validation.organization;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.organization.OrganizationCardinalityMapping;
import org.tzi.use.plugins.bdi.model.organization.OrganizationMapping;
import org.tzi.use.plugins.bdi.model.organization.OrganizationMappingConfirmation;
import org.tzi.use.plugins.bdi.model.organization.OrganizationMissionMapping;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.Cardinality;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.SourceSpan;
import org.tzi.use.plugins.bdi.model.organization.OrganizationRoleMapping;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueSeverity;
import org.tzi.use.plugins.bdi.validation.IssueStatus;

/** Static organization pilot. It does not infer runtime enactment or norm fulfillment. */
public final class OrganizationConsistencyValidator {
    public List<OrganizationFinding> evaluate(OrganizationValidationContext context) {
        List<OrganizationFinding> findings = new ArrayList<>();
        for (OrganizationMapping mapping : context.mappings()) {
            if (mapping instanceof OrganizationRoleMapping role) {
                evaluateRole(context, role, findings);
            } else if (mapping instanceof OrganizationMissionMapping mission) {
                evaluateMission(context, mission, findings);
            } else if (mapping instanceof OrganizationCardinalityMapping cardinality) {
                evaluateCardinality(context, cardinality, findings);
            }
        }
        return findings.stream().sorted(Comparator
                .comparing((OrganizationFinding value) -> value.issue().ruleId())
                .thenComparing(value -> value.mapping().key())).toList();
    }

    private static void evaluateRole(
            OrganizationValidationContext context,
            OrganizationRoleMapping mapping,
            List<OrganizationFinding> findings) {
        Optional<SourceSpan> source = context.organization().roles().stream()
                .filter(role -> role.qualifiedId().equals(mapping.sourceQualifiedId()))
                .map(role -> role.span()).findFirst();
        if (candidate(mapping, OrganizationRuleCatalog.ROLE_CLASS, source, findings)) {
            return;
        }
        boolean targetExists = context.uml().classes().stream()
                .anyMatch(type -> type.reference().equals(mapping.umlClass()));
        if (source.isEmpty() || !targetExists) {
            findings.add(finding(mapping, source, OrganizationRuleCatalog.ROLE_CLASS,
                    IssueSeverity.ERROR, IssueCertainty.CONFIRMED,
                    source.isEmpty() ? "Mapped organization role does not exist"
                            : "Mapped UML class does not exist",
                    "Select an existing organization role and UML class"));
        }
    }

    private static void evaluateMission(
            OrganizationValidationContext context,
            OrganizationMissionMapping mapping,
            List<OrganizationFinding> findings) {
        Optional<SourceSpan> source = context.organization().schemes().stream()
                .flatMap(scheme -> scheme.missions().stream())
                .filter(mission -> mission.qualifiedId().equals(mapping.sourceQualifiedId()))
                .map(mission -> mission.span()).findFirst();
        if (candidate(mapping, OrganizationRuleCatalog.MISSION_OPERATION, source, findings)) {
            return;
        }
        boolean targetExists = context.uml().operations().stream()
                .anyMatch(operation -> operation.reference().equals(mapping.umlOperation()));
        if (source.isEmpty() || !targetExists) {
            findings.add(finding(mapping, source, OrganizationRuleCatalog.MISSION_OPERATION,
                    IssueSeverity.ERROR, IssueCertainty.CONFIRMED,
                    source.isEmpty() ? "Mapped organization mission does not exist"
                            : "Mapped UML operation does not exist",
                    "Select an existing organization mission and UML operation"));
        }
    }

    private static void evaluateCardinality(
            OrganizationValidationContext context,
            OrganizationCardinalityMapping mapping,
            List<OrganizationFinding> findings) {
        Optional<CardinalitySource> source = context.organization().groups().stream()
                .filter(group -> group.qualifiedId().equals(mapping.groupQualifiedId()))
                .flatMap(group -> group.roles().stream()
                        .filter(role -> role.roleQualifiedId().equals(mapping.sourceQualifiedId()))
                        .map(role -> new CardinalitySource(role.cardinality(), group.span())))
                .findFirst();
        Optional<SourceSpan> sourceSpan = source.map(CardinalitySource::span);
        if (candidate(mapping, OrganizationRuleCatalog.CARDINALITY_CONSTRAINT, sourceSpan, findings)) {
            return;
        }
        boolean targetExists = context.uml().classInvariants().stream()
                .anyMatch(constraint -> constraint.reference().equals(mapping.oclConstraint()));
        if (source.isEmpty() || !targetExists) {
            findings.add(finding(mapping, sourceSpan, OrganizationRuleCatalog.CARDINALITY_CONSTRAINT,
                    IssueSeverity.ERROR, IssueCertainty.CONFIRMED,
                    source.isEmpty() ? "Mapped organization role cardinality does not exist"
                            : "Mapped OCL invariant does not exist",
                    "Select an existing role cardinality and OCL invariant"));
            return;
        }
        if (mapping.normalizedOclBounds().isEmpty()) {
            findings.add(finding(mapping, sourceSpan, OrganizationRuleCatalog.CARDINALITY_CONSTRAINT,
                    IssueSeverity.WARNING, IssueCertainty.UNKNOWN,
                    "The OCL invariant exists but its cardinality bounds have not been reviewed",
                    "Confirm reviewer-normalized OCL minimum and maximum bounds"));
            return;
        }
        Cardinality expected = source.orElseThrow().cardinality();
        Cardinality actual = mapping.normalizedOclBounds().orElseThrow();
        if (!expected.equals(actual)) {
            findings.add(finding(mapping, sourceSpan, OrganizationRuleCatalog.CARDINALITY_CONSTRAINT,
                    IssueSeverity.ERROR, IssueCertainty.CONFIRMED,
                    "Organization and reviewed OCL cardinality bounds differ",
                    "Align the Moise cardinality and reviewed OCL bounds"));
            return;
        }
        findings.add(finding(mapping, sourceSpan, OrganizationRuleCatalog.CARDINALITY_CONSTRAINT,
                IssueSeverity.INFO, IssueCertainty.UNKNOWN,
                "Static cardinality bounds match; runtime organization enactment is unavailable",
                "Capture runtime membership evidence before checking enacted cardinality"));
    }

    private static boolean candidate(
            OrganizationMapping mapping,
            String ruleId,
            Optional<SourceSpan> source,
            List<OrganizationFinding> findings) {
        if (mapping.confirmation() == OrganizationMappingConfirmation.CONFIRMED) {
            return false;
        }
        findings.add(finding(mapping, source, ruleId, IssueSeverity.INFO, IssueCertainty.UNKNOWN,
                "Organization mapping is a candidate and has not been confirmed",
                "Review and explicitly confirm the mapping before validation"));
        return true;
    }

    private static OrganizationFinding finding(
            OrganizationMapping mapping,
            Optional<SourceSpan> source,
            String ruleId,
            IssueSeverity severity,
            IssueCertainty certainty,
            String message,
            String fix) {
        List<String> evidence = new ArrayList<>(mapping.evidence());
        evidence.add("organization=" + mapping.sourceQualifiedId());
        evidence.add("mapping=" + mapping.key());
        evidence.add("confirmation=" + mapping.confirmation());
        source.ifPresent(span -> evidence.add("source=" + span.source().canonical()));
        if (mapping instanceof OrganizationCardinalityMapping cardinality) {
            evidence.add("group=" + cardinality.groupQualifiedId());
            evidence.add("oclBounds=" + cardinality.normalizedOclBounds()
                    .map(OrganizationConsistencyValidator::cardinality).orElse("unavailable"));
        }
        ConsistencyIssue issue = new ConsistencyIssue(
                ruleId, severity, IssueStatus.OPEN, message,
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(mapping.target()),
                evidence, Optional.of(fix), certainty);
        return new OrganizationFinding(mapping, source, issue);
    }

    private static String cardinality(Cardinality value) {
        return value.minimum() + ".." + (value.maximum() == Cardinality.UNBOUNDED ? "*" : value.maximum());
    }

    private record CardinalitySource(Cardinality cardinality, SourceSpan span) {
    }
}
