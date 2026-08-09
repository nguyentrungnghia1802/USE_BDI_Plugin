package org.tzi.use.plugins.bdi.validation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** Runs rule phases in a stable order without mutating the BDI or USE inputs. */
public final class ValidationOrchestrator {
    private final List<ConsistencyRule> rules;
    private final RuleConfiguration configuration;
    private final List<Suppression> suppressions;
    private final Optional<Path> projectRoot;

    public ValidationOrchestrator() {
        this(StandardConsistencyRules.create(), RuleConfiguration.standard(), List.of());
    }

    public ValidationOrchestrator(RuleConfiguration configuration) {
        this(StandardConsistencyRules.create(), configuration, List.of());
    }

    public ValidationOrchestrator(RuleConfiguration configuration, List<Suppression> suppressions) {
        this(StandardConsistencyRules.create(), configuration, suppressions);
    }

    public ValidationOrchestrator(
            RuleConfiguration configuration,
            List<Suppression> suppressions,
            Optional<Path> projectRoot) {
        this(StandardConsistencyRules.create(), configuration, suppressions, projectRoot);
    }

    public ValidationOrchestrator(List<ConsistencyRule> rules) {
        this(rules, allRules(rules), List.of());
    }

    public ValidationOrchestrator(List<ConsistencyRule> rules, RuleConfiguration configuration) {
        this(rules, configuration, List.of());
    }

    public ValidationOrchestrator(
            List<ConsistencyRule> rules,
            RuleConfiguration configuration,
            List<Suppression> suppressions) {
        this(rules, configuration, suppressions, Optional.empty());
    }

    public ValidationOrchestrator(
            List<ConsistencyRule> rules,
            RuleConfiguration configuration,
            List<Suppression> suppressions,
            Optional<Path> projectRoot) {
        List<ConsistencyRule> candidates = List.copyOf(Objects.requireNonNull(rules, "rules"));
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.suppressions = List.copyOf(Objects.requireNonNull(suppressions, "suppressions"));
        this.projectRoot = Objects.requireNonNull(projectRoot, "projectRoot")
                .map(path -> path.toAbsolutePath().normalize());
        if (this.projectRoot.isEmpty()
                && this.suppressions.stream().anyMatch(suppression -> suppression.projectSourceId().isPresent())) {
            throw new IllegalArgumentException("Project-relative suppressions require a project root");
        }
        Set<String> available = new HashSet<>();
        for (ConsistencyRule rule : candidates) {
            Objects.requireNonNull(rule, "rule");
            if (!available.add(rule.id())) {
                throw new IllegalArgumentException("Duplicate consistency rule ID: " + rule.id());
            }
        }
        Set<String> unknown = configuration.enabledRuleIds().stream()
                .filter(ruleId -> !available.contains(ruleId))
                .collect(Collectors.toCollection(TreeSet::new));
        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException("Rule configuration references unknown rule IDs: " + unknown);
        }
        this.rules = candidates.stream()
                .filter(rule -> configuration.isEnabled(rule.id()))
                .toList();
    }

    public List<ConsistencyRule> rules() {
        return rules;
    }

    public RuleConfiguration configuration() {
        return configuration;
    }

    public List<Suppression> suppressions() {
        return suppressions;
    }

    public List<ConsistencyIssue> evaluate(ValidationContext context) {
        Objects.requireNonNull(context, "context");
        List<ConsistencyIssue> issues = new ArrayList<>();
        rules.stream()
                .sorted(Comparator.comparing(ConsistencyRule::phase).thenComparing(ConsistencyRule::id))
                .forEach(rule -> issues.addAll(rule.evaluate(context)));
        List<ConsistencyIssue> ordered = issues.stream()
                .sorted(Comparator
                        .comparing((ConsistencyIssue issue) -> issue.sourceSpan()
                                .map(span -> span.source().toString())
                                .orElse(""))
                        .thenComparing(issue -> issue.sourceSpan().map(span -> span.beginLine()).orElse(0))
                        .thenComparing(ConsistencyIssue::ruleId)
                        .thenComparing(ConsistencyIssue::message))
                .toList();
        return projectRoot
                .map(root -> SuppressionService.apply(ordered, suppressions, root))
                .orElseGet(() -> SuppressionService.apply(ordered, suppressions));
    }

    private static RuleConfiguration allRules(List<ConsistencyRule> rules) {
        Objects.requireNonNull(rules, "rules");
        return RuleConfiguration.of(rules.stream().map(ConsistencyRule::id).toList());
    }
}
