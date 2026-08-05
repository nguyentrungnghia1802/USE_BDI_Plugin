package org.tzi.use.plugins.bdi.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class BdiImportServiceTest {
    private final BdiImportService service = new BdiImportService();

    @Test
    void keepsSuccessfulModelsAndDiagnosticsInInputOrder() throws Exception {
        BdiImportSnapshot snapshot = service.importFiles(List.of(
                fixture("fixtures/asl/valid/minimal.asl"),
                fixture("fixtures/asl/invalid/missing-plan-body.asl"),
                fixture("fixtures/asl/valid/review-agent.asl")));

        assertEquals(2, snapshot.fileCount());
        assertEquals(1, snapshot.diagnostics().size());
        assertEquals("minimal.asl", snapshot.models().get(0).source().getFileName().toString());
        assertEquals("review-agent.asl", snapshot.models().get(1).source().getFileName().toString());
        assertTrue(snapshot.hasErrors());
        assertEquals(2, snapshot.index().models().size());
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = BdiImportServiceTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}
