package org.tzi.use.plugins.bdi.model.mas;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;

/** Immutable, parser-independent IR for one statically imported MAS project. */
public record MasProjectModel(
        String name,
        ProjectSourceId source,
        List<MasAgentInstanceModel> agents,
        List<MasResourceReference> resources) {
    public MasProjectModel {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        source = Objects.requireNonNull(source, "source");
        agents = List.copyOf(Objects.requireNonNull(agents, "agents"));
        resources = List.copyOf(Objects.requireNonNull(resources, "resources"));
        Set<String> names = new HashSet<>();
        for (MasAgentInstanceModel agent : agents) {
            if (!names.add(agent.name())) {
                throw new IllegalArgumentException("Duplicate agent instance in project IR: " + agent.name());
            }
        }
    }
}
