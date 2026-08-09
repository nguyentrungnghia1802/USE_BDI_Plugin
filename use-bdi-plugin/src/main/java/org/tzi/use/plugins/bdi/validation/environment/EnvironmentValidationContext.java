package org.tzi.use.plugins.bdi.validation.environment;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.tzi.use.plugins.bdi.model.environment.EnvironmentMapping;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentModel;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;

public record EnvironmentValidationContext(
        EnvironmentModel environment,
        UseModelSnapshot uml,
        List<EnvironmentMapping> mappings) {
    public EnvironmentValidationContext {
        environment = Objects.requireNonNull(environment, "environment");
        uml = Objects.requireNonNull(uml, "uml");
        mappings = List.copyOf(Objects.requireNonNull(mappings, "mappings")).stream()
                .sorted(Comparator.comparing(EnvironmentMapping::key)).toList();
        if (mappings.stream().map(EnvironmentMapping::key).distinct().count() != mappings.size()) {
            throw new IllegalArgumentException("Duplicate environment mapping");
        }
    }
}
