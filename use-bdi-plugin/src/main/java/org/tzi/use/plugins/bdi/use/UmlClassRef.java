package org.tzi.use.plugins.bdi.use;

import java.util.List;
import java.util.Objects;

public record UmlClassRef(String name, boolean abstractType, List<String> parentNames) {
    public UmlClassRef {
        requireText(name, "name");
        parentNames = List.copyOf(Objects.requireNonNull(parentNames, "parentNames"));
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
