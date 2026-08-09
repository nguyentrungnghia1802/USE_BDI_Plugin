package org.tzi.use.plugins.bdi.importer;

import java.util.List;
import java.util.Objects;

public record ParsedMasProject(
        String name,
        List<ParsedMasAgent> agents,
        List<ParsedMasResource> resources) {
    public ParsedMasProject {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        agents = List.copyOf(Objects.requireNonNull(agents, "agents"));
        resources = List.copyOf(Objects.requireNonNull(resources, "resources"));
    }
}
