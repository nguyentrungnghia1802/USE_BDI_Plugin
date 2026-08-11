package org.tzi.use.plugins.bdi.cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.tzi.use.parser.use.USECompiler;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.application.BdiProjectConfiguration;
import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshot;
import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshotService;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisRequest;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisResult;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisService;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.persistence.MappingFileRepository;
import org.tzi.use.plugins.bdi.persistence.RuleConfigurationRepository;
import org.tzi.use.plugins.bdi.persistence.SuppressionRepository;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseSnapshotOclEvaluator;
import org.tzi.use.plugins.bdi.use.UseUmlModelFacade;
import org.tzi.use.plugins.bdi.validation.RuleConfiguration;
import org.tzi.use.plugins.bdi.validation.Suppression;
import org.tzi.use.plugins.bdi.validation.ValidationOrchestrator;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.sys.MSystem;

/** Builds the same immutable analysis aggregate as the GUI from explicit files. */
public final class HeadlessAnalysisService {

    public HeadlessAnalysisResult analyze(HeadlessAnalysisRequest request) throws HeadlessInputException {
        Path useFile = requireFile(request.useFile(), "USE model");
        requireExtension(useFile, ".use", "USE model");
        List<Path> aslFiles = new ArrayList<>();
        for (Path aslFile : request.aslFiles()) {
            Path normalized = requireFile(aslFile, "AgentSpeak source");
            requireExtension(normalized, ".asl", "AgentSpeak source");
            aslFiles.add(normalized);
        }
        Path projectRoot = useFile.getParent();
        if (projectRoot == null) {
            throw new HeadlessInputException("USE model has no project directory: " + useFile);
        }
        Path projectFile;
        if (request.projectFile().isPresent()) {
            projectFile = requireFile(request.projectFile().orElseThrow(), "JaCaMo project");
            requireExtension(projectFile, ".jcm", "JaCaMo project");
        } else {
            projectFile = null;
        }

        MSystem system = compileSystem(useFile);
        UseUmlModelFacade facade = new UseUmlModelFacade();
        UseModelSnapshot useModel = facade.snapshot(system);
        BdiImportSnapshot imported = new BdiImportService().importFiles(aslFiles);
        RuleConfiguration rules = loadRules(request.rulesFile());
        List<Suppression> suppressions = loadSuppressions(request.suppressionsFile(), projectRoot);
        MappingDocument mapping = loadMapping(request.mappingFile(), projectRoot, useModel);
        String before = facade.snapshot(system).fingerprint();
        Optional<MasProjectAnalysisResult> projectResult = Optional.ofNullable(projectFile).map(normalized -> {
            BdiProjectConfiguration configuration = new BdiProjectConfiguration(
                    Optional.of(projectRoot),
                    rules,
                    suppressions,
                    request.rulesFile().isPresent(),
                    request.suppressionsFile().isPresent());
            return new MasProjectAnalysisService().analyze(MasProjectAnalysisRequest.of(
                    normalized,
                    request.timestamp(),
                    Optional.of(useModel),
                    Optional.of(new UseSnapshotOclEvaluator(system)),
                    mapping,
                    configuration));
        });
        CurrentAnalysisSnapshot snapshot;
        Optional<org.tzi.use.plugins.bdi.model.mas.MasProjectModel> project = Optional.empty();
        List<org.tzi.use.plugins.bdi.importer.MasProjectDiagnostic> diagnostics = List.of();
        if (projectResult.isPresent()) {
            MasProjectAnalysisResult result = projectResult.orElseThrow();
            snapshot = result.snapshot();
            project = result.project();
            diagnostics = result.projectDiagnostics();
        } else {
            String configurationOrigin = configurationOrigin(request, rules, suppressions);
            CurrentAnalysisSnapshotService service = new CurrentAnalysisSnapshotService(
                    new ValidationOrchestrator(rules, suppressions, Optional.of(projectRoot)),
                    configurationOrigin,
                    "0.1.0",
                    "USE-7.1.1");
            snapshot = service.create(
                    request.timestamp(),
                    imported,
                    Optional.of(useModel),
                    Optional.of(new UseSnapshotOclEvaluator(system)),
                    mapping);
        }
        String after = facade.snapshot(system).fingerprint();
        if (!before.equals(after)) {
            throw new IllegalStateException("Headless analysis changed its private USE state");
        }
        return new HeadlessAnalysisResult(
                request.projectName().orElseGet(() -> projectName(
                        projectFile == null ? useFile : projectFile)),
                snapshot,
                project,
                diagnostics);
    }

    private static MSystem compileSystem(Path useFile) throws HeadlessInputException {
        StringWriter errors = new StringWriter();
        try (var input = Files.newInputStream(useFile)) {
            MModel model = USECompiler.compileSpecification(
                    input, useFile.toString(), new PrintWriter(errors), new ModelFactory());
            if (model == null) {
                throw new HeadlessInputException(
                        "Invalid USE model " + useFile + ": " + errors.toString().trim());
            }
            model.setFilename(useFile.toString());
            return new MSystem(model);
        } catch (IOException error) {
            throw new HeadlessInputException("Could not read USE model " + useFile + ": " + error.getMessage(), error);
        }
    }

    private static RuleConfiguration loadRules(Optional<Path> file) throws HeadlessInputException {
        if (file.isEmpty()) {
            return RuleConfiguration.standard();
        }
        Path path = requireFile(file.orElseThrow(), "rule configuration");
        try {
            return new RuleConfigurationRepository().load(path);
        } catch (IOException | IllegalArgumentException error) {
            throw new HeadlessInputException("Invalid rule configuration " + path + ": " + error.getMessage(), error);
        }
    }

    private static List<Suppression> loadSuppressions(Optional<Path> file, Path projectRoot)
            throws HeadlessInputException {
        if (file.isEmpty()) {
            return List.of();
        }
        Path path = requireFile(file.orElseThrow(), "suppression configuration");
        try {
            return new SuppressionRepository().load(path, projectRoot);
        } catch (IOException | IllegalArgumentException error) {
            throw new HeadlessInputException("Invalid suppression configuration " + path + ": " + error.getMessage(), error);
        }
    }

    private static MappingDocument loadMapping(
            Optional<Path> file,
            Path projectRoot,
            UseModelSnapshot useModel) throws HeadlessInputException {
        if (file.isEmpty()) {
            return MappingDocument.empty(useModel.fingerprint());
        }
        Path path = requireFile(file.orElseThrow(), "mapping");
        try {
            return new MappingFileRepository().load(path, projectRoot);
        } catch (IOException | IllegalArgumentException error) {
            throw new HeadlessInputException("Invalid mapping " + path + ": " + error.getMessage(), error);
        }
    }

    private static String configurationOrigin(
            HeadlessAnalysisRequest request,
            RuleConfiguration rules,
            List<Suppression> suppressions) {
        return "Headless configuration: " + rules.enabledRuleIds().size() + " rule(s) ["
                + request.rulesFile().map(Path::toString).orElse("standard") + "], "
                + suppressions.size() + " suppression(s) ["
                + request.suppressionsFile().map(Path::toString).orElse("none") + "]";
    }

    private static Path requireFile(Path file, String role) throws HeadlessInputException {
        Path normalized = file.toAbsolutePath().normalize();
        if (!Files.isRegularFile(normalized)) {
            throw new HeadlessInputException("Missing " + role + " file: " + normalized);
        }
        return normalized;
    }

    private static void requireExtension(Path file, String extension, String role) throws HeadlessInputException {
        if (!file.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(extension)) {
            throw new HeadlessInputException(
                    role + " must use " + extension + "; unsupported input: " + file);
        }
    }

    private static String projectName(Path useFile) {
        String filename = useFile.getFileName().toString();
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }
}
