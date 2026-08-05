package org.tzi.use.plugins.bdi.use;

import java.util.Objects;
import java.util.Optional;

public record UmlAttributeRef(
        String ownerName,
        String name,
        String type,
        boolean derived,
        Optional<String> initExpression,
        Optional<String> deriveExpression) {
    public UmlAttributeRef {
        requireText(ownerName, "ownerName");
        requireText(name, "name");
        requireText(type, "type");
        initExpression = Objects.requireNonNull(initExpression, "initExpression");
        deriveExpression = Objects.requireNonNull(deriveExpression, "deriveExpression");
    }

    public String reference() {
        return ownerName + "::" + name;
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
