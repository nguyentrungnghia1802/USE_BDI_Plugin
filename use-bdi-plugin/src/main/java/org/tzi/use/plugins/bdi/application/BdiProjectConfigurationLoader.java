package org.tzi.use.plugins.bdi.application;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.tzi.use.plugins.bdi.persistence.RuleConfigurationRepository;
import org.tzi.use.plugins.bdi.persistence.SuppressionRepository;
import org.tzi.use.plugins.bdi.validation.RuleConfiguration;
import org.tzi.use.plugins.bdi.validation.Suppression;

/** Discovers versioned BDI configuration beside the currently loaded USE model. */
public final class BdiProjectConfigurationLoader {
    static final Path CONFIGURATION_DIRECTORY = Path.of(".bdi-plugin");
    static final String RULES_FILE = "rules.json";
    static final String SUPPRESSIONS_FILE = "suppressions.json";

    private final RuleConfigurationRepository ruleRepository;
    private final SuppressionRepository suppressionRepository;

    public BdiProjectConfigurationLoader() {
        this(new RuleConfigurationRepository(), new SuppressionRepository());
    }

    BdiProjectConfigurationLoader(
            RuleConfigurationRepository ruleRepository,
            SuppressionRepository suppressionRepository) {
        this.ruleRepository = ruleRepository;
        this.suppressionRepository = suppressionRepository;
    }

    public BdiProjectConfiguration loadModel(String modelFilename) throws IOException {
        if (modelFilename == null || modelFilename.isBlank()) {
            return BdiProjectConfiguration.defaults();
        }
        try {
            return load(Path.of(modelFilename));
        } catch (InvalidPathException error) {
            throw new IOException("Invalid USE model filename: " + modelFilename, error);
        }
    }

    public BdiProjectConfiguration load(Path modelFile) throws IOException {
        if (modelFile == null) {
            throw new NullPointerException("modelFile");
        }
        Path normalizedModel = modelFile.toAbsolutePath().normalize();
        Path projectRoot = normalizedModel.getParent();
        if (projectRoot == null) {
            return BdiProjectConfiguration.defaults();
        }

        Path configurationDirectory = projectRoot.resolve(CONFIGURATION_DIRECTORY);
        Path rulesFile = configurationDirectory.resolve(RULES_FILE);
        Path suppressionsFile = configurationDirectory.resolve(SUPPRESSIONS_FILE);
        boolean rulesLoaded = Files.exists(rulesFile);
        boolean suppressionsLoaded = Files.exists(suppressionsFile);

        try {
            RuleConfiguration rules = rulesLoaded
                    ? ruleRepository.load(rulesFile)
                    : RuleConfiguration.standard();
            List<Suppression> suppressions = suppressionsLoaded
                    ? suppressionRepository.load(suppressionsFile, projectRoot)
                    : List.of();
            BdiProjectConfiguration configuration = new BdiProjectConfiguration(
                    Optional.of(projectRoot),
                    rules,
                    suppressions,
                    rulesLoaded,
                    suppressionsLoaded);
            configuration.newOrchestrator();
            return configuration;
        } catch (IOException | IllegalArgumentException error) {
            throw new IOException(
                    "Could not load BDI configuration from " + configurationDirectory + ": " + error.getMessage(),
                    error);
        }
    }
}
