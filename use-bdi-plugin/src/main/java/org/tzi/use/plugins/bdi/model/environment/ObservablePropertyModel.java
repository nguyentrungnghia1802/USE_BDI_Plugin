package org.tzi.use.plugins.bdi.model.environment;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ObservablePropertyModel(
        String name,
        int arity,
        Optional<List<String>> runtimeValues,
        List<String> evidence) {
    public ObservablePropertyModel {
        EnvironmentOperation.requireText(name, "name");
        if (arity < 0) {
            throw new IllegalArgumentException("arity must not be negative");
        }
        runtimeValues = Objects.requireNonNull(runtimeValues, "runtimeValues")
                .map(List::copyOf);
        if (runtimeValues.isPresent() && runtimeValues.orElseThrow().size() != arity) {
            throw new IllegalArgumentException("runtimeValues must match arity");
        }
        evidence = List.copyOf(Objects.requireNonNull(evidence, "evidence"));
    }

    public String signature() {
        return name + "/" + arity;
    }
}
