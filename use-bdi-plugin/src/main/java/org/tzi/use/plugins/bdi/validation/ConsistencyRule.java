package org.tzi.use.plugins.bdi.validation;

import java.util.List;

/** One deterministic, side-effect-free consistency check. */
public interface ConsistencyRule {
    String id();

    RulePhase phase();

    List<ConsistencyIssue> evaluate(ValidationContext context);
}
