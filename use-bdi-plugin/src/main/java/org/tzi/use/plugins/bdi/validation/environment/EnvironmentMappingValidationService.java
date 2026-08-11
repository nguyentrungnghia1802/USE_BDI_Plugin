package org.tzi.use.plugins.bdi.validation.environment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingDocument;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingStalenessStatus;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentModel;
import org.tzi.use.plugins.bdi.model.environment.PersistedEnvironmentMapping;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueSeverity;
import org.tzi.use.plugins.bdi.validation.IssueStatus;

/** Bridges persisted environment mappings to the existing plugin-owned rule engine. */
public final class EnvironmentMappingValidationService {
    private final EnvironmentMappingStalenessDetector stalenessDetector;
    private final EnvironmentConsistencyValidator consistencyValidator;

    public EnvironmentMappingValidationService() {
        this(new EnvironmentMappingStalenessDetector(), new EnvironmentConsistencyValidator());
    }

    EnvironmentMappingValidationService(
            EnvironmentMappingStalenessDetector stalenessDetector,
            EnvironmentConsistencyValidator consistencyValidator) {
        this.stalenessDetector = Objects.requireNonNull(stalenessDetector, "stalenessDetector");
        this.consistencyValidator = Objects.requireNonNull(consistencyValidator, "consistencyValidator");
    }

    public EnvironmentValidationResult evaluate(
            EnvironmentMappingDocument document,
            EnvironmentModel environment,
            UseModelSnapshot uml) {
        EnvironmentMappingDocument revalidated = stalenessDetector.detect(document, environment, uml);
        List<EnvironmentFinding> findings = new ArrayList<>();
        for (PersistedEnvironmentMapping mapping : revalidated.mappings()) {
            if (mapping.confirmation()
                    != org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingConfirmation.CONFIRMED) {
                continue;
            }
            if (mapping.staleness().status() != EnvironmentMappingStalenessStatus.CURRENT) {
                findings.add(stalenessFinding(mapping));
            }
        }
        findings.addAll(consistencyValidator.evaluate(new EnvironmentValidationContext(
                environment, uml, revalidated.confirmedCurrentMappings())));
        findings.sort(Comparator.comparing((EnvironmentFinding value) -> value.issue().ruleId())
                .thenComparing(value -> value.mapping().key()));
        return new EnvironmentValidationResult(revalidated, findings);
    }

    private static EnvironmentFinding stalenessFinding(PersistedEnvironmentMapping mapping) {
        boolean stale = mapping.staleness().status() == EnvironmentMappingStalenessStatus.STALE;
        IssueSeverity severity = stale ? IssueSeverity.ERROR : IssueSeverity.WARNING;
        IssueCertainty certainty = stale ? IssueCertainty.CONFIRMED : IssueCertainty.UNKNOWN;
        String message = stale ? "Confirmed environment mapping is stale"
                : "Confirmed environment mapping staleness is unknown";
        List<String> evidence = new ArrayList<>();
        evidence.add("mapping=" + mapping.key());
        evidence.add("source=" + mapping.provenance().source().canonical());
        evidence.addAll(mapping.staleness().reasons());
        ConsistencyIssue issue = new ConsistencyIssue(
                EnvironmentRuleCatalog.STALE_BINDING,
                severity,
                IssueStatus.OPEN,
                message,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(mapping.umlTarget()),
                evidence,
                Optional.of("Reconfirm the mapping after resolving the reported source or target change"),
                certainty);
        return new EnvironmentFinding(mapping.toRuntimeMapping(), issue);
    }
}
