package org.tzi.use.plugins.bdi.model.environment;

import java.util.List;
import java.util.Objects;

public record EnvironmentOperation(String name, int arity, List<String> parameterTypes, String guard) {
    public EnvironmentOperation {
        requireText(name, "name");
        if (arity < 0) {
            throw new IllegalArgumentException("arity must not be negative");
        }
        parameterTypes = List.copyOf(Objects.requireNonNull(parameterTypes, "parameterTypes"));
        if (parameterTypes.size() != arity) {
            throw new IllegalArgumentException("parameterTypes must match arity");
        }
        guard = Objects.requireNonNull(guard, "guard");
    }

    public String signature() {
        return name + "/" + arity;
    }

    static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
