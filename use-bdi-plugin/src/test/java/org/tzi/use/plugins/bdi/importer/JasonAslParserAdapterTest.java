package org.tzi.use.plugins.bdi.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class JasonAslParserAdapterTest {
    private final JasonAslParserAdapter parser = new JasonAslParserAdapter();

    @Test
    void parsesValidAgentSpeakProgram() throws Exception {
        Path source = fixture("fixtures/asl/valid/minimal.asl");

        AslParseSummary result = parser.parse(source);

        assertEquals(source.toAbsolutePath().normalize(), result.source());
        assertEquals("3.3.0", result.parserVersion());
        assertEquals(1, result.beliefCount());
        assertEquals(1, result.goalCount());
        assertEquals(1, result.planCount());
    }

    @Test
    void parsesMigratedSmartQueuePrototypeFixture() throws Exception {
        Path source = fixture("fixtures/smartqueue/Smart_manager_agent.asl");

        AslParseSummary result = parser.parse(source);

        assertEquals(source.toAbsolutePath().normalize(), result.source());
        assertEquals("3.3.0", result.parserVersion());
        assertEquals(9, result.beliefCount());
        assertEquals(1, result.goalCount());
        assertEquals(5, result.planCount());
    }

    @Test
    void rejectsMissingSource() {
        Path missing = Path.of("target", "missing-agent.asl");

        AslParseException error = assertThrows(AslParseException.class, () -> parser.parse(missing));

        assertEquals(
                "AgentSpeak source is not a regular file: " + missing.toAbsolutePath().normalize(),
                error.getMessage());
        assertTrue(error.diagnostic().isEmpty());
    }

    @Test
    void reportsSyntaxErrorWithSourcePosition() throws Exception {
        Path source = fixture("fixtures/asl/invalid/missing-plan-body.asl");

        AslParseException error = assertThrows(AslParseException.class, () -> parser.parse(source));

        AslDiagnostic diagnostic = error.diagnostic().orElseThrow();
        assertEquals(AslDiagnostic.SYNTAX_ERROR_CODE, diagnostic.code());
        assertEquals(AslDiagnosticSeverity.ERROR, diagnostic.severity());
        assertEquals(source.toAbsolutePath().normalize(), diagnostic.source());
        assertEquals(3, diagnostic.line());
        assertEquals(8, diagnostic.column());
        assertTrue(diagnostic.hasSourcePosition());
        assertTrue(diagnostic.message().contains("Encountered"));
        assertTrue(diagnostic.message().contains("Was expecting"));
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = JasonAslParserAdapterTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}
