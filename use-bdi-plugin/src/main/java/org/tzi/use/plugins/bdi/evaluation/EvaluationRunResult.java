package org.tzi.use.plugins.bdi.evaluation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.tzi.use.plugins.bdi.validation.IssueCertainty;

/** Deterministic, corpus-scoped result of one evaluation manifest run. */
public record EvaluationRunResult(
        String schemaVersion,
        String caseStudy,
        String toolVersion,
        String useVersion,
        String configurationProfile,
        Instant timestamp,
        String manifestHash,
        String corpusHash,
        String configurationHash,
        List<EvaluationCaseResult> cases,
        EvaluationMetrics metrics) {
    public EvaluationRunResult {
        requireText(schemaVersion, "schemaVersion");
        requireText(caseStudy, "caseStudy");
        requireText(toolVersion, "toolVersion");
        requireText(useVersion, "useVersion");
        requireText(configurationProfile, "configurationProfile");
        Objects.requireNonNull(timestamp, "timestamp");
        requireHash(manifestHash, "manifestHash");
        requireHash(corpusHash, "corpusHash");
        requireHash(configurationHash, "configurationHash");
        cases = new ArrayList<>(Objects.requireNonNull(cases, "cases"));
        cases.sort(java.util.Comparator.comparing(EvaluationCaseResult::id));
        cases = List.copyOf(cases);
        Objects.requireNonNull(metrics, "metrics");
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    private static void requireHash(String value, String field) {
        requireText(value, field);
        if (!value.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException(field + " must be a lowercase SHA-256 digest");
        }
    }

    public record EvaluationCaseResult(
            String id,
            String family,
            String layer,
            EvaluationStatus status,
            int exitCode,
            List<String> requiredRuleIds,
            List<String> forbiddenRuleIds,
            List<String> observedRuleIds,
            Map<String, IssueCertainty> observedCertainties,
            List<String> missingRuleIds,
            List<String> violatedForbiddenRuleIds,
            List<String> unexpectedRuleIds,
            List<String> evidenceAnchors,
            List<String> traceLinks,
            List<String> observedEvidence,
            String inputHash,
            String diagnostic) {
        public EvaluationCaseResult {
            if (id == null || id.isBlank() || family == null || family.isBlank()
                    || layer == null || layer.isBlank()) {
                throw new IllegalArgumentException("Evaluation case identity must not be blank");
            }
            Objects.requireNonNull(status, "status");
            requiredRuleIds = sorted(requiredRuleIds, "requiredRuleIds");
            forbiddenRuleIds = sorted(forbiddenRuleIds, "forbiddenRuleIds");
            observedRuleIds = sorted(observedRuleIds, "observedRuleIds");
            missingRuleIds = sorted(missingRuleIds, "missingRuleIds");
            violatedForbiddenRuleIds = sorted(violatedForbiddenRuleIds, "violatedForbiddenRuleIds");
            unexpectedRuleIds = sorted(unexpectedRuleIds, "unexpectedRuleIds");
            evidenceAnchors = sorted(evidenceAnchors, "evidenceAnchors");
            traceLinks = sorted(traceLinks, "traceLinks");
            observedEvidence = sorted(observedEvidence, "observedEvidence");
            observedCertainties = Map.copyOf(Objects.requireNonNull(observedCertainties, "observedCertainties"));
            if (inputHash == null || !inputHash.matches("[0-9a-f]{64}")) {
                throw new IllegalArgumentException("inputHash must be a lowercase SHA-256 digest");
            }
            diagnostic = diagnostic == null ? "" : diagnostic;
        }

        private static List<String> sorted(List<String> values, String field) {
            Objects.requireNonNull(values, field);
            return values.stream().filter(Objects::nonNull).sorted().distinct().toList();
        }
    }

    public record EvaluationMetrics(
            int totalCases,
            int passed,
            int detected,
            int missed,
            int unexpected,
            int unknown,
            int unsupported,
            int invalidInput,
            int timeouts,
            int executionErrors) {
        public EvaluationMetrics {
            int[] values = {
                    totalCases, passed, detected, missed, unexpected, unknown,
                    unsupported, invalidInput, timeouts, executionErrors};
            for (int value : values) {
                if (value < 0) {
                    throw new IllegalArgumentException("Evaluation metric counts must not be negative");
                }
            }
            if (passed + detected + missed + unexpected + unknown + unsupported
                    + invalidInput + timeouts + executionErrors != totalCases) {
                throw new IllegalArgumentException("Evaluation metrics do not partition the case corpus");
            }
        }

        public static EvaluationMetrics from(List<EvaluationCaseResult> cases) {
            int passed = count(cases, EvaluationStatus.PASS);
            int detected = count(cases, EvaluationStatus.DETECTED);
            int missed = count(cases, EvaluationStatus.MISSED);
            int unexpected = count(cases, EvaluationStatus.UNEXPECTED);
            int unknown = count(cases, EvaluationStatus.UNKNOWN);
            int unsupported = count(cases, EvaluationStatus.UNSUPPORTED);
            int invalidInput = count(cases, EvaluationStatus.INVALID_INPUT);
            int timeouts = count(cases, EvaluationStatus.TIMEOUT);
            int executionErrors = count(cases, EvaluationStatus.EXECUTION_ERROR);
            return new EvaluationMetrics(
                    cases.size(), passed, detected, missed, unexpected, unknown,
                    unsupported, invalidInput, timeouts, executionErrors);
        }

        private static int count(List<EvaluationCaseResult> cases, EvaluationStatus status) {
            return (int) cases.stream().filter(result -> result.status() == status).count();
        }
    }
}
