package org.tzi.use.plugins.bdi.importer;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.mas.MasResourceKind;

import jacamo.project.JaCaMoInstParameters;
import jacamo.project.JaCaMoOrgParameters;
import jacamo.project.JaCaMoProject;
import jacamo.project.JaCaMoWorkspaceParameters;
import jacamo.project.parser.JaCaMoProjectParser;
import jacamo.project.parser.ParseException;
import jacamo.project.parser.Token;
import jason.mas2j.AgentParameters;

/** Boundary adapter; JaCaMo types never leave this class. */
public final class JaCaMoProjectParserAdapter {
    public ParsedMasProject parse(Path projectFile) throws MasProjectParseException {
        Path source = projectFile.toAbsolutePath().normalize();
        Path directory = source.getParent();
        if (directory == null) {
            throw failure(source, "JaCaMo project has no parent directory", null, null);
        }
        try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
            JaCaMoProject project = new JaCaMoProjectParser(reader).parse(directory.toString());
            List<ParsedMasAgent> agents = new ArrayList<>();
            for (AgentParameters agent : project.getAgents()) {
                Path agentSource = agent.getSourceAsFile().toPath();
                agents.add(new ParsedMasAgent(agent.getAgName(), resolve(directory, agentSource)));
            }
            List<ParsedMasResource> resources = new ArrayList<>();
            for (JaCaMoWorkspaceParameters workspace : project.getWorkspaces()) {
                resources.add(new ParsedMasResource(
                        MasResourceKind.WORKSPACE, workspace.getName(), Optional.empty()));
            }
            for (JaCaMoOrgParameters organization : project.getOrgs()) {
                resources.add(new ParsedMasResource(
                        MasResourceKind.ORGANIZATION,
                        organization.getName(),
                        optionalSource(directory, organization.getParameter("source"))));
            }
            for (JaCaMoInstParameters institution : project.getInstitutions()) {
                resources.add(new ParsedMasResource(
                        MasResourceKind.INSTITUTION,
                        institution.getName(),
                        optionalSource(directory, institution.getParameter("source"))));
            }
            return new ParsedMasProject(project.getSocName(), agents, resources);
        } catch (ParseException error) {
            Token token = error.currentToken == null ? null : error.currentToken.next;
            throw failure(source, safeMessage(error), token, error);
        } catch (IOException | RuntimeException | LinkageError error) {
            throw failure(source, safeMessage(error), null, error);
        }
    }

    private static Optional<Path> optionalSource(Path directory, String value) {
        return value == null || value.isBlank()
                ? Optional.empty()
                : Optional.of(resolve(directory, Path.of(value)));
    }

    private static Path resolve(Path directory, Path source) {
        return (source.isAbsolute() ? source : directory.resolve(source)).normalize();
    }

    private static MasProjectParseException failure(
            Path source, String message, Token token, Throwable cause) {
        int line = token == null ? 0 : token.beginLine;
        int column = token == null ? 0 : token.beginColumn;
        return new MasProjectParseException(new MasProjectDiagnostic(
                MasProjectDiagnostic.PARSE_ERROR,
                MasProjectDiagnosticSeverity.ERROR,
                source,
                line,
                column,
                message), cause);
    }

    private static String safeMessage(Throwable error) {
        return error.getMessage() == null || error.getMessage().isBlank()
                ? "Could not parse JaCaMo project"
                : error.getMessage();
    }
}
