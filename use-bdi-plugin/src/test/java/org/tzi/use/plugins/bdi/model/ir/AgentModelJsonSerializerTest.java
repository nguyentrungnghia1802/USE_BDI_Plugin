package org.tzi.use.plugins.bdi.model.ir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.importer.JasonAslParserAdapter;

class AgentModelJsonSerializerTest {
    @Test
    void serializesMinimalModelWithPortableSourcePaths() throws Exception {
        Path source = fixture("fixtures/asl/valid/minimal.asl");
        AgentModel model = new JasonAslParserAdapter().parseModel(source);
        String expected = resource("fixtures/expected/minimal-agent-model.json");

        AgentModelJsonSerializer serializer = new AgentModelJsonSerializer();

        assertEquals(expected, serializer.serialize(model, source.getParent()));
        assertEquals(expected, serializer.serialize(model, source.getParent()));
    }

    private static String resource(String name) throws IOException {
        try (InputStream input = AgentModelJsonSerializerTest.class.getClassLoader().getResourceAsStream(name)) {
            if (input == null) {
                throw new IllegalStateException("Missing test resource: " + name);
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8).trim();
        }
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = AgentModelJsonSerializerTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}
