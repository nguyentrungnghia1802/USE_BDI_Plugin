package org.tzi.use.plugins.bdi.model.ir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.importer.AslImportResult;
import org.tzi.use.plugins.bdi.importer.AslParseSummary;
import org.tzi.use.plugins.bdi.importer.JasonAslParserAdapter;

class AgentModelTest {
    private final JasonAslParserAdapter parser = new JasonAslParserAdapter();
    private final AslAgentModelNormalizer normalizer = new AslAgentModelNormalizer();

    @Test
    void normalizesParserSummaryIntoRootModel() throws Exception {
        Path source = fixture("fixtures/asl/valid/minimal.asl");
        AslParseSummary summary = parser.parse(source);

        AgentModel model = normalizer.normalize(summary);

        assertEquals(source.toAbsolutePath().normalize(), model.source());
        assertEquals("3.3.0", model.parserVersion());
        assertEquals(1, model.beliefCount());
        assertEquals(1, model.goalCount());
        assertEquals(1, model.planCount());
        assertEquals(3, model.elementCount());
    }

    @Test
    void normalizesSuccessfulFilesInOrderWithoutFailedFileModels() throws Exception {
        AslParseSummary first = parser.parse(fixture("fixtures/asl/valid/minimal.asl"));
        AslParseSummary second = parser.parse(fixture("fixtures/asl/valid/review-agent.asl"));

        List<AgentModel> models = normalizer.normalize(new AslImportResult(List.of(first, second)));

        assertEquals(List.of(first.source(), second.source()), models.stream().map(AgentModel::source).toList());
        assertEquals(2, models.size());
        assertThrows(UnsupportedOperationException.class, () -> models.clear());
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = AgentModelTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}
