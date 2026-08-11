package org.tzi.use.plugins.bdi.evaluation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.tzi.use.plugins.bdi.validation.IssueCertainty;

/** Immutable, reviewed corpus and oracle definition for one evaluation run. */
public record EvaluationManifest(
        String schemaVersion,
        String caseStudy,
        String toolVersion,
        String useVersion,
        String configurationProfile,
        List<String> excludedLayers,
        List<EvaluationCase> cases) {
    public static final String CURRENT_SCHEMA_VERSION = "0.1.0";

    public EvaluationManifest {
        requireText(schemaVersion, "schemaVersion");
        if (!CURRENT_SCHEMA_VERSION.equals(schemaVersion)) {
            throw new IllegalArgumentException("Unsupported evaluation manifest schema version: " + schemaVersion);
        }
        requireText(caseStudy, "caseStudy");
        requireText(toolVersion, "toolVersion");
        requireText(useVersion, "useVersion");
        requireText(configurationProfile, "configurationProfile");
        excludedLayers = sortedUnique(excludedLayers, "excludedLayers");
        cases = new ArrayList<>(Objects.requireNonNull(cases, "cases"));
        if (cases.isEmpty()) {
            throw new IllegalArgumentException("Evaluation manifest must declare at least one case");
        }
        cases.sort(java.util.Comparator.comparing(EvaluationCase::id));
        Set<String> ids = new HashSet<>();
        for (EvaluationCase evaluationCase : cases) {
            if (!ids.add(evaluationCase.id())) {
                throw new IllegalArgumentException("Duplicate evaluation case id: " + evaluationCase.id());
            }
        }
        cases = List.copyOf(cases);
    }

    private static List<String> sortedUnique(List<String> values, String field) {
        Objects.requireNonNull(values, field);
        List<String> sorted = values.stream().map(value -> requireText(value, field + " item"))
                .distinct().sorted().toList();
        return List.copyOf(sorted);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    /** One isolated input set and its reviewed scoped oracle. */
    public record EvaluationCase(
            String id,
            String family,
            String layer,
            String useFile,
            List<String> aslFiles,
            Optional<String> jcmFile,
            Optional<String> mappingFile,
            List<String> requiredRuleIds,
            List<String> forbiddenRuleIds,
            Map<String, IssueCertainty> expectedCertainties,
            List<String> evidenceAnchors,
            List<String> allowedUnsupportedLayers,
            Duration timeout,
            Optional<String> stateFixture,
            Map<String, String> evidenceTokens) {
        public EvaluationCase(
                String id,
                String family,
                String layer,
                String useFile,
                List<String> aslFiles,
                Optional<String> jcmFile,
                Optional<String> mappingFile,
                List<String> requiredRuleIds,
                List<String> forbiddenRuleIds,
                Map<String, IssueCertainty> expectedCertainties,
                List<String> evidenceAnchors,
                List<String> allowedUnsupportedLayers,
                Duration timeout) {
            this(id, family, layer, useFile, aslFiles, jcmFile, mappingFile, requiredRuleIds,
                    forbiddenRuleIds, expectedCertainties, evidenceAnchors, allowedUnsupportedLayers,
                    timeout, Optional.empty(), Map.of());
        }

        public EvaluationCase {
            requireText(id, "case id");
            requireText(family, "family");
            requireText(layer, "layer");
            relativePath(useFile, "useFile");
            aslFiles = sortedUniquePaths(aslFiles, "aslFiles");
            jcmFile = optionalRelativePath(jcmFile, "jcmFile");
            mappingFile = optionalRelativePath(mappingFile, "mappingFile");
            if (aslFiles.isEmpty() == jcmFile.isEmpty()) {
                throw new IllegalArgumentException("Case " + id + " must declare exactly one of aslFiles or jcmFile");
            }
            requiredRuleIds = sortedUnique(requiredRuleIds, "requiredRuleIds");
            forbiddenRuleIds = sortedUnique(forbiddenRuleIds, "forbiddenRuleIds");
            Set<String> scoped = new HashSet<>(requiredRuleIds);
            Set<String> overlap = new HashSet<>(requiredRuleIds);
            overlap.retainAll(forbiddenRuleIds);
            if (!overlap.isEmpty()) {
                throw new IllegalArgumentException("Case " + id + " has overlapping required and forbidden rules");
            }
            scoped.addAll(forbiddenRuleIds);
            if (scoped.isEmpty()) {
                throw new IllegalArgumentException("Case " + id + " must declare an oracle rule scope");
            }
            Map<String, IssueCertainty> certaintyCopy = new LinkedHashMap<>();
            List<String> requiredForCertainty = requiredRuleIds;
            Objects.requireNonNull(expectedCertainties, "expectedCertainties").entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        if (!requiredForCertainty.contains(entry.getKey())) {
                            throw new IllegalArgumentException(
                                    "Case " + id + " certainty has no required rule: " + entry.getKey());
                        }
                        certaintyCopy.put(
                                requireText(entry.getKey(), "expected certainty rule"),
                                Objects.requireNonNull(entry.getValue(), "expected certainty"));
                    });
            for (String ruleId : requiredRuleIds) {
                if (!certaintyCopy.containsKey(ruleId)) {
                    throw new IllegalArgumentException("Case " + id + " has no expected certainty for " + ruleId);
                }
            }
            expectedCertainties = Map.copyOf(certaintyCopy);
            evidenceAnchors = sortedUnique(evidenceAnchors, "evidenceAnchors");
            allowedUnsupportedLayers = sortedUnique(allowedUnsupportedLayers, "allowedUnsupportedLayers");
            timeout = Objects.requireNonNull(timeout, "timeout");
            if (timeout.compareTo(Duration.ofSeconds(1)) < 0
                    || timeout.compareTo(Duration.ofMinutes(5)) > 0) {
                throw new IllegalArgumentException("Case " + id + " timeout must be between 1 second and 5 minutes");
            }
            stateFixture = Objects.requireNonNull(stateFixture, "stateFixture").map(value -> requireText(value, "stateFixture"));
            Map<String, String> tokenCopy = new LinkedHashMap<>();
            Objects.requireNonNull(evidenceTokens, "evidenceTokens").entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        if (!scoped.contains(entry.getKey())) {
                            throw new IllegalArgumentException(
                                    "Case " + id + " evidence token has no scoped rule: " + entry.getKey());
                        }
                        tokenCopy.put(
                                requireText(entry.getKey(), "evidence token rule"),
                                requireText(entry.getValue(), "evidence token"));
                    });
            evidenceTokens = Map.copyOf(tokenCopy);
        }

        private static List<String> sortedUniquePaths(List<String> values, String field) {
            Objects.requireNonNull(values, field);
            if (values.isEmpty()) {
                return List.of();
            }
            return values.stream().map(value -> relativePath(value, field + " item"))
                    .distinct().sorted().toList();
        }

        private static Optional<String> optionalRelativePath(Optional<String> value, String field) {
            Objects.requireNonNull(value, field);
            return value.map(path -> relativePath(path, field));
        }

        private static String relativePath(String value, String field) {
            requireText(value, field);
            if (value.startsWith("/") || value.startsWith("\\") || value.matches("^[A-Za-z]:.*")) {
                throw new IllegalArgumentException(field + " must be relative: " + value);
            }
            String normalized = value.replace('\\', '/');
            for (String segment : normalized.split("/")) {
                if (segment.equals("..") || segment.isBlank() || segment.equals(".")) {
                    throw new IllegalArgumentException(field + " contains unsafe path: " + value);
                }
            }
            return normalized;
        }
    }
}
