package org.tzi.use.plugins.bdi.validation.environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.environment.ArtifactModel;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingDocument;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingStaleness;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingStalenessStatus;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentOperation;
import org.tzi.use.plugins.bdi.model.environment.PersistedEnvironmentMapping;
import org.tzi.use.plugins.bdi.model.environment.PersistedEnvironmentOperationMapping;
import org.tzi.use.plugins.bdi.model.environment.PersistedEnvironmentPropertyMapping;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;

/** Rechecks persisted environment targets against the current static snapshots. */
public final class EnvironmentMappingStalenessDetector {
    public EnvironmentMappingDocument detect(
            EnvironmentMappingDocument document,
            org.tzi.use.plugins.bdi.model.environment.EnvironmentModel environment,
            UseModelSnapshot uml) {
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(uml, "uml");

        List<PersistedEnvironmentMapping> updated = document.mappings().stream()
                .map(mapping -> detectMapping(mapping, environment, uml))
                .toList();
        return document.withMappings(updated);
    }

    private static PersistedEnvironmentMapping detectMapping(
            PersistedEnvironmentMapping mapping,
            org.tzi.use.plugins.bdi.model.environment.EnvironmentModel environment,
            UseModelSnapshot uml) {
        if (mapping.confirmation() != org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingConfirmation.CONFIRMED
                || mapping.staleness().status() == EnvironmentMappingStalenessStatus.UNKNOWN) {
            return mapping;
        }
        List<String> reasons = new ArrayList<>();
        Optional<ArtifactModel> artifact = environment.artifact(mapping.workspace(), mapping.artifact());
        if (artifact.isEmpty()) {
            reasons.add("CArtAgO artifact is missing: " + mapping.workspace() + "/" + mapping.artifact());
        } else {
            ArtifactModel value = artifact.orElseThrow();
            if (!value.typeName().equals(mapping.artifactType())) {
                reasons.add("CArtAgO artifact type changed from " + mapping.artifactType()
                        + " to " + value.typeName());
            }
            if (mapping instanceof PersistedEnvironmentOperationMapping operation) {
                detectOperation(operation, value, uml, reasons);
            } else if (mapping instanceof PersistedEnvironmentPropertyMapping property) {
                detectProperty(property, value, uml, reasons);
            }
        }
        EnvironmentMappingStaleness status = reasons.isEmpty()
                ? EnvironmentMappingStaleness.current()
                : new EnvironmentMappingStaleness(EnvironmentMappingStalenessStatus.STALE, reasons);
        return mapping.withStaleness(status);
    }

    private static void detectOperation(
            PersistedEnvironmentOperationMapping mapping,
            ArtifactModel artifact,
            UseModelSnapshot uml,
            List<String> reasons) {
        Optional<EnvironmentOperation> operation = artifact.operations().stream()
                .filter(value -> value.name().equals(mapping.operation()))
                .filter(value -> value.arity() == mapping.operationArity())
                .findFirst();
        if (operation.isEmpty()) {
            reasons.add("CArtAgO operation is missing: " + mapping.operation()
                    + "/" + mapping.operationArity());
        } else {
            EnvironmentOperation value = operation.orElseThrow();
            if (value.arity() != mapping.operationArity()) {
                reasons.add("CArtAgO operation arity changed from " + mapping.operationArity()
                        + " to " + value.arity());
            }
            if (!value.parameterTypes().equals(mapping.parameterTypes())) {
                reasons.add("CArtAgO operation parameter types changed");
            }
        }
        if (uml.operations().stream().noneMatch(value -> value.reference().equals(mapping.umlTarget()))) {
            reasons.add("UML operation is missing: " + mapping.umlTarget());
        }
    }

    private static void detectProperty(
            PersistedEnvironmentPropertyMapping mapping,
            ArtifactModel artifact,
            UseModelSnapshot uml,
            List<String> reasons) {
        boolean propertyExists = artifact.observableProperties().stream()
                .anyMatch(value -> value.name().equals(mapping.property())
                        && value.arity() == mapping.propertyArity());
        if (!propertyExists) {
            reasons.add("CArtAgO observable property is missing: " + mapping.property()
                    + "/" + mapping.propertyArity());
        }
        if (uml.attributes().stream().noneMatch(value -> value.reference().equals(mapping.umlTarget()))) {
            reasons.add("UML attribute is missing: " + mapping.umlTarget());
        }
    }
}
