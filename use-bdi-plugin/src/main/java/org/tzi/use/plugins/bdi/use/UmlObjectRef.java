package org.tzi.use.plugins.bdi.use;

import java.util.Map;
import java.util.Objects;

public record UmlObjectRef(
        String name,
        String className,
        boolean exists,
        Map<String, String> attributeValues) {
    public UmlObjectRef {
        requireText(name, "name");
        requireText(className, "className");
        attributeValues = Map.copyOf(Objects.requireNonNull(attributeValues, "attributeValues"));
    }

    public String reference() {
        return name;
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
