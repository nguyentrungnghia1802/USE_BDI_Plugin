package org.tzi.use.plugins.bdi.validation.environment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.environment.ArtifactModel;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMapping;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentOperation;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentOperationMapping;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentPropertyMapping;
import org.tzi.use.plugins.bdi.model.environment.ObservablePropertyModel;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueSeverity;
import org.tzi.use.plugins.bdi.validation.IssueStatus;

/** Static environment pilot. Dynamic property values remain UNKNOWN unless supplied as evidence. */
public final class EnvironmentConsistencyValidator {
    public List<EnvironmentFinding> evaluate(EnvironmentValidationContext context) {
        List<EnvironmentFinding> findings = new ArrayList<>();
        for (EnvironmentMapping mapping : context.mappings()) {
            if (mapping instanceof EnvironmentOperationMapping operation) {
                evaluateOperation(context, operation, findings);
            } else if (mapping instanceof EnvironmentPropertyMapping property) {
                evaluateProperty(context, property, findings);
            }
        }
        return findings.stream().sorted(Comparator
                .comparing((EnvironmentFinding value) -> value.issue().ruleId())
                .thenComparing(value -> value.mapping().key())).toList();
    }

    private static void evaluateOperation(
            EnvironmentValidationContext context,
            EnvironmentOperationMapping mapping,
            List<EnvironmentFinding> findings) {
        Optional<ArtifactModel> artifact = context.environment().artifact(mapping.workspace(), mapping.artifact());
        if (artifact.isEmpty()) {
            findings.add(finding(mapping, EnvironmentRuleCatalog.OPERATION_EXISTS,
                    IssueSeverity.ERROR, IssueCertainty.CONFIRMED,
                    "Mapped CArtAgO artifact does not exist", "Declare the artifact instance"));
            return;
        }
        List<EnvironmentOperation> named = artifact.orElseThrow().operations().stream()
                .filter(operation -> operation.name().equals(mapping.operation())).toList();
        boolean umlTargetExists = context.uml().operations().stream()
                .anyMatch(operation -> operation.reference().equals(mapping.umlTarget()));
        if (named.isEmpty() || !umlTargetExists) {
            findings.add(finding(mapping, EnvironmentRuleCatalog.OPERATION_EXISTS,
                    IssueSeverity.ERROR, IssueCertainty.CONFIRMED,
                    named.isEmpty() ? "Mapped CArtAgO operation does not exist"
                            : "Mapped UML operation does not exist",
                    "Select existing CArtAgO and UML operations"));
            return;
        }
        if (named.stream().noneMatch(operation -> operation.arity() == mapping.actionArity())) {
            findings.add(finding(mapping, EnvironmentRuleCatalog.OPERATION_ARITY,
                    IssueSeverity.ERROR, IssueCertainty.CONFIRMED,
                    "BDI action arity does not match the CArtAgO operation",
                    "Align the action and artifact operation arity"));
        }
    }

    private static void evaluateProperty(
            EnvironmentValidationContext context,
            EnvironmentPropertyMapping mapping,
            List<EnvironmentFinding> findings) {
        Optional<ObservablePropertyModel> property = context.environment()
                .artifact(mapping.workspace(), mapping.artifact()).stream()
                .flatMap(artifact -> artifact.observableProperties().stream())
                .filter(value -> value.name().equals(mapping.property()))
                .filter(value -> value.arity() == mapping.propertyArity())
                .findFirst();
        boolean attributeExists = context.uml().attributes().stream()
                .anyMatch(attribute -> attribute.reference().equals(mapping.umlTarget()));
        if (property.isEmpty() || !attributeExists) {
            findings.add(finding(mapping, EnvironmentRuleCatalog.PROPERTY_ATTRIBUTE,
                    IssueSeverity.ERROR, IssueCertainty.CONFIRMED,
                    property.isEmpty() ? "Mapped observable property does not exist"
                            : "Mapped UML attribute does not exist",
                    "Select an existing observable property and UML attribute"));
            return;
        }
        if (property.orElseThrow().runtimeValues().isEmpty()) {
            findings.add(finding(mapping, EnvironmentRuleCatalog.PROPERTY_ATTRIBUTE,
                    IssueSeverity.INFO, IssueCertainty.UNKNOWN,
                    "Static declarations match; dynamic observable-property value is unavailable",
                    "Capture runtime evidence before comparing values"));
        }
    }

    private static EnvironmentFinding finding(
            EnvironmentMapping mapping,
            String ruleId,
            IssueSeverity severity,
            IssueCertainty certainty,
            String message,
            String fix) {
        ConsistencyIssue issue = new ConsistencyIssue(
                ruleId, severity, IssueStatus.OPEN, message,
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(mapping.umlTarget()),
                List.of("environment=" + mapping.workspace() + "/" + mapping.artifact(),
                        "mapping=" + mapping.key()),
                Optional.of(fix), certainty);
        return new EnvironmentFinding(mapping, issue);
    }
}
