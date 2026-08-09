package org.tzi.use.plugins.bdi.validation.environment;

import java.util.Objects;

import org.tzi.use.plugins.bdi.model.environment.EnvironmentMapping;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;

public record EnvironmentFinding(EnvironmentMapping mapping, ConsistencyIssue issue) {
    public EnvironmentFinding {
        mapping = Objects.requireNonNull(mapping, "mapping");
        issue = Objects.requireNonNull(issue, "issue");
    }
}
