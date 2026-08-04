package org.tzi.use.plugins.bdi.validation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Runs rule phases in a stable order without mutating the BDI or USE inputs. */
public final class ValidationOrchestrator {
    private final List<ConsistencyRule> rules;

    public ValidationOrchestrator() {
        this(StandardConsistencyRules.create());
    }

    public ValidationOrchestrator(List<ConsistencyRule> rules) {
        this.rules = List.copyOf(Objects.requireNonNull(rules, "rules"));
    }

    public List<ConsistencyRule> rules() {
        return rules;
    }

    public List<ConsistencyIssue> evaluate(ValidationContext context) {
        Objects.requireNonNull(context, "context");
        List<ConsistencyIssue> issues = new ArrayList<>();
        rules.stream()
                .sorted(Comparator.comparing(ConsistencyRule::phase).thenComparing(ConsistencyRule::id))
                .forEach(rule -> issues.addAll(rule.evaluate(context)));
        return issues.stream()
                .sorted(Comparator
                        .comparing((ConsistencyIssue issue) -> issue.sourceSpan()
                                .map(span -> span.source().toString())
                                .orElse(""))
                        .thenComparing(issue -> issue.sourceSpan().map(span -> span.beginLine()).orElse(0))
                        .thenComparing(ConsistencyIssue::ruleId)
                        .thenComparing(ConsistencyIssue::message))
                .toList();
    }
}
