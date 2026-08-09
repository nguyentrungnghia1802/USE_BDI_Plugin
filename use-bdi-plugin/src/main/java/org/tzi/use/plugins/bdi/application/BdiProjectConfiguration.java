package org.tzi.use.plugins.bdi.application;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.validation.RuleConfiguration;
import org.tzi.use.plugins.bdi.validation.Suppression;
import org.tzi.use.plugins.bdi.validation.ValidationOrchestrator;

/** Immutable project configuration selected for one BDI Explorer instance. */
public record BdiProjectConfiguration(
        Optional<Path> projectRoot,
        RuleConfiguration rules,
        List<Suppression> suppressions,
        boolean rulesLoaded,
        boolean suppressionsLoaded) {

    public BdiProjectConfiguration {
        Objects.requireNonNull(projectRoot, "projectRoot");
        projectRoot = projectRoot.map(path -> path.toAbsolutePath().normalize());
        rules = Objects.requireNonNull(rules, "rules");
        suppressions = List.copyOf(Objects.requireNonNull(suppressions, "suppressions"));
        if ((rulesLoaded || suppressionsLoaded) && projectRoot.isEmpty()) {
            throw new IllegalArgumentException("Loaded configuration requires a project root");
        }
    }

    public static BdiProjectConfiguration defaults() {
        return new BdiProjectConfiguration(
                Optional.empty(),
                RuleConfiguration.standard(),
                List.of(),
                false,
                false);
    }

    public ValidationOrchestrator newOrchestrator() {
        return new ValidationOrchestrator(rules, suppressions);
    }

    public String summary() {
        String ruleSource = rulesLoaded ? "project" : "default";
        String suppressionSource = suppressionsLoaded ? "project" : "default";
        return "Configuration: " + rules.enabledRuleIds().size() + " rule(s) [" + ruleSource + "], "
                + suppressions.size() + " suppression(s) [" + suppressionSource + "]";
    }
}
