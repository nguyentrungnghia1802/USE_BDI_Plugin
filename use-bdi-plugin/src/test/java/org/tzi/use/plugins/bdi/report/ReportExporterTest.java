package org.tzi.use.plugins.bdi.report;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public class ReportExporterTest {

    @Test
    public void exportJson_writesFile() throws Exception {
        ReportData data = new ReportData(
                "test-project",
                "0.1.0-test",
                "USE-TEST-7.1.1",
                Instant.now(),
                3,
                5,
                "unit-test"
        );

        Path tmp = Files.createTempDirectory("bdi-report-test");
        Path out = tmp.resolve("report.json");

        ReportExporter.exportJson(data, out);

        Assertions.assertTrue(Files.exists(out));
        Assertions.assertTrue(Files.size(out) > 0);
    }
}
