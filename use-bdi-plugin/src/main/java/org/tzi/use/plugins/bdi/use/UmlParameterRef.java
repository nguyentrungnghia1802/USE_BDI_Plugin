package org.tzi.use.plugins.bdi.use;

public record UmlParameterRef(String name, String type) {
    public UmlParameterRef {
        requireText(name, "name");
        requireText(type, "type");
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
