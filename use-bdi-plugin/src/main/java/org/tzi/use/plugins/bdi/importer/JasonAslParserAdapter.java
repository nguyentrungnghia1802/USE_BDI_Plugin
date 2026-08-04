package org.tzi.use.plugins.bdi.importer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import jason.asSemantics.Agent;
import jason.asSyntax.Literal;
import jason.asSyntax.Plan;
import jason.asSyntax.SourceInfo;
import jason.asSyntax.Term;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.Token;

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
        } catch (ParseException error) {
            throw new AslParseException(toSyntaxDiagnostic(normalizedSource, error), error);
        } catch (Exception error) {
            throw new AslParseException("Could not parse AgentSpeak source: " + normalizedSource, error);
        }

        List<AslSourceLocation> sourceLocations = extractSourceLocations(agent, normalizedSource);
        return new AslParseSummary(
                normalizedSource,
                JASON_VERSION,
                agent.getInitialBels().size(),
                agent.getInitialGoals().size(),
                agent.getPL().size(),
                sourceLocations);
    }

    private static List<AslSourceLocation> extractSourceLocations(Agent agent, Path source) {
        List<AslSourceLocation> locations = new ArrayList<>(
                agent.getInitialBels().size() + agent.getInitialGoals().size() + agent.getPL().size());
        for (Literal belief : agent.getInitialBels()) {
            locations.add(toSourceLocation(source, AslSourceElement.INITIAL_BELIEF, belief, belief.toString()));
        }
        for (Literal goal : agent.getInitialGoals()) {
            locations.add(toSourceLocation(source, AslSourceElement.INITIAL_GOAL, goal, goal.toString()));
        }
        for (Plan plan : agent.getPL().getPlans()) {
            String subject = plan.getTrigger() == null ? plan.toString() : plan.getTrigger().toString();
            locations.add(toSourceLocation(source, AslSourceElement.PLAN, plan, subject));
        }
        locations.sort(Comparator
                .comparingInt(AslSourceLocation::beginLine)
                .thenComparingInt(AslSourceLocation::endLine)
                .thenComparing(AslSourceLocation::element));
        return locations;
    }

    private static AslSourceLocation toSourceLocation(
            Path source,
            AslSourceElement element,
            Term term,
            String subject) {
        SourceInfo sourceInfo = term.getSrcInfo();
        int beginLine = sourceInfo == null
                ? AslSourceLocation.UNKNOWN_POSITION
                : sourceInfo.getBeginSrcLine();
        int endLine = sourceInfo == null
                ? AslSourceLocation.UNKNOWN_POSITION
                : sourceInfo.getEndSrcLine();
        return new AslSourceLocation(source, element, subject, beginLine, endLine);
    }

    private static AslDiagnostic toSyntaxDiagnostic(Path source, ParseException error) {
        Token errorToken = error.currentToken == null ? null : error.currentToken.next;
        int line = errorToken == null ? AslDiagnostic.UNKNOWN_POSITION : errorToken.beginLine;
        int column = errorToken == null ? AslDiagnostic.UNKNOWN_POSITION : errorToken.beginColumn;
        String message = error.getMessage() == null ? "AgentSpeak syntax error" : error.getMessage();
        return new AslDiagnostic(
                AslDiagnostic.SYNTAX_ERROR_CODE,
                AslDiagnosticSeverity.ERROR,
                source,
                line,
                column,
                message);
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
