package org.tzi.use.plugins.bdi.use;

import java.util.Objects;
import java.util.Optional;

public record UmlConstraintRef(
        String ownerName,
        Optional<String> operationName,
        String kind,
        String name,
        String expression) {
    public UmlConstraintRef {
        requireText(ownerName, "ownerName");
        operationName = Objects.requireNonNull(operationName, "operationName");
        requireText(kind, "kind");
        requireText(name, "name");
        requireText(expression, "expression");
    }

    public String reference() {
        return operationName.map(operation -> ownerName + "::" + operation + "::" + name)
                .orElse(ownerName + "::" + name);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
