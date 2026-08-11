package org.tzi.use.plugins.bdi.evaluation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshot;
import org.tzi.use.plugins.bdi.cli.HeadlessAnalysisRequest;
import org.tzi.use.plugins.bdi.cli.HeadlessAnalysisResult;
import org.tzi.use.plugins.bdi.cli.HeadlessAnalysisService;
import org.tzi.use.plugins.bdi.cli.HeadlessInputException;
import org.tzi.use.plugins.bdi.importer.MasProjectDiagnostic;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;

/** Runs reviewed cases against the real headless analysis service in isolation. */
public final class EvaluationRunner {
    @FunctionalInterface
    public interface AnalysisExecutor {
        HeadlessAnalysisResult analyze(HeadlessAnalysisRequest request) throws Exception;
    }

    private final AnalysisExecutor executor;

    public EvaluationRunner() {
        this(new HeadlessAnalysisService()::analyze);
    }

    EvaluationRunner(AnalysisExecutor executor) {
        this.executor = java.util.Objects.requireNonNull(executor, "executor");
    }

    public EvaluationRunResult run(
            EvaluationManifest manifest,
            Path sourceRoot,
            Instant timestamp) throws IOException {
        java.util.Objects.requireNonNull(manifest, "manifest");
        Path root = requireDirectory(sourceRoot, "evaluation source root");
        java.util.Objects.requireNonNull(timestamp, "timestamp");

        List<String> corpusFiles = new ArrayList<>();
        for (EvaluationManifest.EvaluationCase evaluationCase : manifest.cases()) {
            validateCaseInputs(root, evaluationCase, corpusFiles);
        }
        String manifestHash = EvaluationHashing.sha256Text(EvaluationManifestCodec.encode(manifest));
        String corpusHash = EvaluationHashing.corpusHash(root, corpusFiles);
        String configurationHash = EvaluationHashing.sha256Text(
                manifest.toolVersion() + "\n" + manifest.useVersion() + "\n" + manifest.configurationProfile());

        List<EvaluationRunResult.EvaluationCaseResult> results = manifest.cases().stream()
                .map(evaluationCase -> runCase(root, evaluationCase, timestamp))
                .toList();
        return new EvaluationRunResult(
                EvaluationManifest.CURRENT_SCHEMA_VERSION,
                manifest.caseStudy(),
                manifest.toolVersion(),
                manifest.useVersion(),
                manifest.configurationProfile(),
                timestamp,
                manifestHash,
                corpusHash,
                configurationHash,
                results,
                EvaluationRunResult.EvaluationMetrics.from(results));
    }

    private EvaluationRunResult.EvaluationCaseResult runCase(
            Path sourceRoot,
            EvaluationManifest.EvaluationCase evaluationCase,
            Instant timestamp) {
        Path workspace = null;
        String inputHash;
        try {
            inputHash = caseInputHash(sourceRoot, evaluationCase);
            workspace = Files.createTempDirectory("use-bdi-evaluation-");
            CaseInputs inputs = copyInputs(sourceRoot, evaluationCase, workspace);
            HeadlessAnalysisRequest request = new HeadlessAnalysisRequest(
                    inputs.useFile(),
                    inputs.aslFiles(),
                    inputs.jcmFile(),
                    inputs.mappingFile(),
                    Optional.empty(),
                    Optional.empty(),
                    timestamp,
                    Optional.of(evaluationCase.id()),
                    evaluationCase.stateFixture().map(org.tzi.use.plugins.bdi.cli.HeadlessStateFixture::new));
            AnalysisOutcome outcome = execute(request, evaluationCase.timeout());
            if (outcome.status() != null) {
                return result(evaluationCase, outcome.status(), inputHash, outcome.message(),
                        List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), outcome.exitCode());
            }
            return classify(evaluationCase, inputHash, outcome.analysis(), workspace);
        } catch (IllegalArgumentException error) {
            return result(evaluationCase, EvaluationStatus.INVALID_INPUT, safeHash(sourceRoot, evaluationCase),
                    error.getMessage(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), 3);
        } catch (java.io.UncheckedIOException error) {
            return result(evaluationCase, EvaluationStatus.EXECUTION_ERROR, safeHash(sourceRoot, evaluationCase),
                    error.getMessage(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), 4);
        } catch (IOException error) {
            return result(evaluationCase, EvaluationStatus.EXECUTION_ERROR, safeHash(sourceRoot, evaluationCase),
                    error.getMessage(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), 4);
        } finally {
            if (workspace != null) {
                try {
                    deleteTree(workspace);
                } catch (IOException ignored) {
                    // The case result is already captured; the workspace is outside the repository.
                }
            }
        }
    }

    private AnalysisOutcome execute(HeadlessAnalysisRequest request, java.time.Duration timeout) {
        ExecutorService executorService = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "use-bdi-evaluation-case");
            thread.setDaemon(true);
            return thread;
        });
        Future<HeadlessAnalysisResult> future = executorService.submit(() -> executor.analyze(request));
        try {
            return new AnalysisOutcome(null, future.get(timeout.toMillis(), TimeUnit.MILLISECONDS), 0, "");
        } catch (TimeoutException error) {
            future.cancel(true);
            return new AnalysisOutcome(EvaluationStatus.TIMEOUT, null, 4,
                    "Analysis exceeded timeout " + timeout.toSeconds() + "s");
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            return new AnalysisOutcome(EvaluationStatus.EXECUTION_ERROR, null, 4, "Evaluation runner interrupted");
        } catch (ExecutionException error) {
            Throwable cause = error.getCause();
            if (cause instanceof HeadlessInputException) {
                return new AnalysisOutcome(EvaluationStatus.INVALID_INPUT, null, 3, cause.getMessage());
            }
            String message = cause == null ? error.getMessage() : cause.getClass().getSimpleName() + ": " + cause.getMessage();
            return new AnalysisOutcome(EvaluationStatus.EXECUTION_ERROR, null, 4, message);
        } finally {
            executorService.shutdownNow();
        }
    }

    private EvaluationRunResult.EvaluationCaseResult classify(
            EvaluationManifest.EvaluationCase evaluationCase,
            String inputHash,
            HeadlessAnalysisResult analysis,
            Path workspace) {
        CurrentAnalysisSnapshot snapshot = analysis.snapshot();
        Map<String, IssueCertainty> observedCertainties = new HashMap<>();
        Set<String> observedRules = new HashSet<>();
        List<String> observedEvidence = new ArrayList<>();
        List<String> traceLinks = new ArrayList<>();
        Set<String> scope = new HashSet<>(evaluationCase.requiredRuleIds());
        scope.addAll(evaluationCase.forbiddenRuleIds());
        for (ConsistencyIssue issue : snapshot.issues()) {
            if (!scope.contains(issue.ruleId())) {
                continue;
            }
            String evidenceToken = evaluationCase.evidenceTokens().get(issue.ruleId());
            String searchableEvidence = issue.message() + " "
                    + issue.umlElementRef().orElse("") + " "
                    + String.join("; ", issue.evidence());
            if (evidenceToken != null && !searchableEvidence.contains(evidenceToken)) {
                continue;
            }
            observedRules.add(issue.ruleId());
            observedCertainties.merge(issue.ruleId(), issue.certainty(), EvaluationRunner::worstCertainty);
            observedEvidence.addAll(issue.evidence().stream()
                    .map(evidence -> sanitizeEvidence(evidence, workspace))
                    .toList());
            traceLinks.add(issue.ruleId() + "|" + issue.message() + "@" + sourcePosition(issue.sourceSpan()));
        }
        List<String> observedScope = observedRules.stream().filter(scope::contains).sorted().toList();
        List<String> missing = evaluationCase.requiredRuleIds().stream()
                .filter(ruleId -> !observedRules.contains(ruleId)).toList();
        List<String> violated = evaluationCase.forbiddenRuleIds().stream()
                .filter(observedRules::contains).toList();
        List<String> unexpected = observedScope.stream()
                .filter(ruleId -> !evaluationCase.requiredRuleIds().contains(ruleId)
                        && !evaluationCase.forbiddenRuleIds().contains(ruleId))
                .toList();
        List<String> uncertain = evaluationCase.requiredRuleIds().stream()
                .filter(observedRules::contains)
                .filter(ruleId -> !certaintyMatches(
                        evaluationCase.expectedCertainties().get(ruleId), observedCertainties.get(ruleId)))
                .toList();
        EvaluationStatus status;
        String diagnostic = "";
        if (isAllowedUnsupported(evaluationCase, analysis.projectDiagnostics())) {
            status = EvaluationStatus.UNSUPPORTED;
            diagnostic = analysis.projectDiagnostics().stream().map(MasProjectDiagnostic::code).sorted().distinct()
                    .reduce((left, right) -> left + "," + right).orElse("unsupported project layer");
        } else if (!violated.isEmpty() || !unexpected.isEmpty()) {
            status = EvaluationStatus.UNEXPECTED;
        } else if (!missing.isEmpty()) {
            status = EvaluationStatus.MISSED;
        } else if (!uncertain.isEmpty()) {
            status = EvaluationStatus.UNKNOWN;
            diagnostic = "Required rule certainty did not meet the reviewed oracle: " + uncertain;
        } else if (evaluationCase.requiredRuleIds().isEmpty()) {
            status = EvaluationStatus.PASS;
        } else {
            status = EvaluationStatus.DETECTED;
        }
        return result(evaluationCase, status, inputHash, diagnostic, observedScope,
                observedCertainties, missing, violated, unexpected, traceLinks, analysis.exitCode().code(), observedEvidence);
    }

    private static boolean isAllowedUnsupported(
            EvaluationManifest.EvaluationCase evaluationCase,
            List<MasProjectDiagnostic> diagnostics) {
        return !evaluationCase.allowedUnsupportedLayers().isEmpty()
                && evaluationCase.allowedUnsupportedLayers().contains(evaluationCase.layer())
                && diagnostics.stream().anyMatch(diagnostic -> diagnostic.code().equals(MasProjectDiagnostic.UNSUPPORTED_RESOURCE));
    }

    private static IssueCertainty worstCertainty(IssueCertainty left, IssueCertainty right) {
        if (left == IssueCertainty.UNKNOWN || right == IssueCertainty.UNKNOWN) {
            return IssueCertainty.UNKNOWN;
        }
        if (left == IssueCertainty.POTENTIAL || right == IssueCertainty.POTENTIAL) {
            return IssueCertainty.POTENTIAL;
        }
        return IssueCertainty.CONFIRMED;
    }

    private static boolean certaintyMatches(IssueCertainty expected, IssueCertainty observed) {
        return expected == observed;
    }

    private static EvaluationRunResult.EvaluationCaseResult result(
            EvaluationManifest.EvaluationCase evaluationCase,
            EvaluationStatus status,
            String inputHash,
            String diagnostic,
            List<String> observedRules,
            List<String> observedCertainties,
            List<String> missing,
            List<String> violated,
            List<String> unexpected,
            List<String> traceLinks,
            int exitCode) {
        Map<String, IssueCertainty> certaintyMap = new HashMap<>();
        return result(evaluationCase, status, inputHash, diagnostic, observedRules, certaintyMap,
                missing, violated, unexpected, traceLinks, exitCode, List.of());
    }

    private static EvaluationRunResult.EvaluationCaseResult result(
            EvaluationManifest.EvaluationCase evaluationCase,
            EvaluationStatus status,
            String inputHash,
            String diagnostic,
            List<String> observedRules,
            Map<String, IssueCertainty> observedCertainties,
            List<String> missing,
            List<String> violated,
            List<String> unexpected,
            List<String> traceLinks,
            int exitCode,
            List<String> observedEvidence) {
        return new EvaluationRunResult.EvaluationCaseResult(
                evaluationCase.id(),
                evaluationCase.family(),
                evaluationCase.layer(),
                status,
                exitCode,
                evaluationCase.requiredRuleIds(),
                evaluationCase.forbiddenRuleIds(),
                observedRules,
                observedCertainties,
                missing,
                violated,
                unexpected,
                evaluationCase.evidenceAnchors(),
                traceLinks,
                observedEvidence,
                inputHash,
                diagnostic);
    }

    private static String sourcePosition(Optional<SourceSpan> sourceSpan) {
        return sourceSpan.map(span -> span.beginLine() + ":" + span.beginColumn()).orElse("unknown");
    }

    private static String sanitizeEvidence(String evidence, Path workspace) {
        String absolute = workspace.toAbsolutePath().normalize().toString();
        return evidence.replace(absolute, "$CASE_WORKSPACE")
                .replace(absolute.replace('\\', '/'), "$CASE_WORKSPACE");
    }

    private static void validateCaseInputs(
            Path root,
            EvaluationManifest.EvaluationCase evaluationCase,
            List<String> corpusFiles) {
        List<String> paths = new ArrayList<>();
        paths.add(evaluationCase.useFile());
        paths.addAll(evaluationCase.aslFiles());
        evaluationCase.jcmFile().ifPresent(paths::add);
        evaluationCase.mappingFile().ifPresent(paths::add);
        for (String relative : paths) {
            Path file = EvaluationHashing.resolve(root, relative);
            if (!Files.isRegularFile(file)) {
                throw new IllegalArgumentException(
                        "Manifest input does not exist for " + evaluationCase.id() + ": " + relative);
            }
            corpusFiles.add(relative);
        }
        for (String anchor : evaluationCase.evidenceAnchors()) {
            String path = anchor.split("#", 2)[0];
            if (path.contains("/") || path.contains("\\")) {
                Path file = EvaluationHashing.resolve(root, path);
                if (!Files.isRegularFile(file)) {
                    throw new IllegalArgumentException(
                            "Evidence anchor does not exist for " + evaluationCase.id() + ": " + anchor);
                }
            }
        }
    }

    private static String caseInputHash(Path root, EvaluationManifest.EvaluationCase evaluationCase) throws IOException {
        List<String> paths = new ArrayList<>();
        paths.add(evaluationCase.useFile());
        paths.addAll(evaluationCase.aslFiles());
        evaluationCase.jcmFile().ifPresent(paths::add);
        evaluationCase.mappingFile().ifPresent(paths::add);
        return EvaluationHashing.corpusHash(root, paths);
    }

    private static String safeHash(Path root, EvaluationManifest.EvaluationCase evaluationCase) {
        try {
            return caseInputHash(root, evaluationCase);
        } catch (Exception error) {
            return EvaluationHashing.sha256Text(evaluationCase.id());
        }
    }

    private static CaseInputs copyInputs(
            Path sourceRoot,
            EvaluationManifest.EvaluationCase evaluationCase,
            Path workspace) throws IOException {
        Map<String, Path> copied = new HashMap<>();
        Path use = copy(sourceRoot, evaluationCase.useFile(), workspace, copied);
        List<Path> asl = evaluationCase.aslFiles().stream()
                .map(path -> copyUnchecked(sourceRoot, path, workspace, copied))
                .toList();
        Optional<Path> jcm = evaluationCase.jcmFile()
                .map(path -> copyUnchecked(sourceRoot, path, workspace, copied));
        Optional<Path> mapping = evaluationCase.mappingFile()
                .map(path -> copyUnchecked(sourceRoot, path, workspace, copied));
        return new CaseInputs(use, asl, jcm, mapping);
    }

    private static Path copyUnchecked(Path root, String relative, Path workspace, Map<String, Path> copied) {
        try {
            return copy(root, relative, workspace, copied);
        } catch (IOException error) {
            throw new java.io.UncheckedIOException(error);
        }
    }

    private static Path copy(Path root, String relative, Path workspace, Map<String, Path> copied) throws IOException {
        String filename = Path.of(relative).getFileName().toString();
        Path target = workspace.resolve(filename);
        Path source = EvaluationHashing.resolve(root, relative);
        Path previous = copied.putIfAbsent(filename, source);
        if (previous != null && !previous.equals(source)) {
            throw new IllegalArgumentException("Case input filename collision in isolated workspace: " + filename);
        }
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    private static Path requireDirectory(Path path, String role) {
        Path normalized = java.util.Objects.requireNonNull(path, role).toAbsolutePath().normalize();
        if (!Files.isDirectory(normalized)) {
            throw new IllegalArgumentException(role + " does not exist: " + normalized);
        }
        return normalized;
    }

    private static void deleteTree(Path directory) throws IOException {
        try (var paths = Files.walk(directory)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException error) {
                    throw new java.io.UncheckedIOException(error);
                }
            });
        } catch (java.io.UncheckedIOException error) {
            throw error.getCause();
        }
    }

    private record CaseInputs(Path useFile, List<Path> aslFiles, Optional<Path> jcmFile, Optional<Path> mappingFile) {
    }

    private record AnalysisOutcome(EvaluationStatus status, HeadlessAnalysisResult analysis, int exitCode, String message) {
    }
}
