package org.tzi.use.plugins.bdi.importer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

import jason.asSemantics.Agent;

public final class JasonAslParserAdapter {
    private static final String VERSION_RESOURCE = "jason-parser.properties";
    private static final String JASON_VERSION = loadJasonVersion();

    public AslParseSummary parse(Path source) throws AslParseException {
        Objects.requireNonNull(source, "source");
        Path normalizedSource = source.toAbsolutePath().normalize();
        if (!Files.isRegularFile(normalizedSource)) {
            throw new AslParseException("AgentSpeak source is not a regular file: " + normalizedSource);
        }

        Agent agent = new Agent();
        agent.setConsiderToAddMIForThisAgent(false);
        agent.initAg();
        try {
            agent.parseAS(normalizedSource.toFile());
        } catch (Exception error) {
            throw new AslParseException("Could not parse AgentSpeak source: " + normalizedSource, error);
        }

        return new AslParseSummary(
                normalizedSource,
                JASON_VERSION,
                agent.getInitialBels().size(),
                agent.getInitialGoals().size(),
                agent.getPL().size());
    }

    private static String loadJasonVersion() {
        Properties properties = new Properties();
        try (InputStream input = JasonAslParserAdapter.class.getResourceAsStream(VERSION_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Missing Jason parser version resource: " + VERSION_RESOURCE);
            }
            properties.load(input);
        } catch (IOException error) {
            throw new IllegalStateException("Could not read Jason parser version resource", error);
        }

        String version = properties.getProperty("version");
        if (version == null || version.isBlank()) {
            throw new IllegalStateException("Jason parser version is not configured");
        }
        return version;
    }
}
