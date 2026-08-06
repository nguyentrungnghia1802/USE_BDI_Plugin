package org.tzi.use.plugins.bdi.report;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public class HtmlReportExporterTest {

    @Test
    public void exportHtml_writesFileAndContainsProjectName() throws Exception {
        ReportData data = new ReportData(
                "html-test-project",
                "0.1.0-test",
                "USE-TEST-7.1.1",
                Instant.now(),
                1,
                2,
                "html-unit-test"
        );

        Path tmp = Files.createTempDirectory("bdi-html-report-test");
        Path out = tmp.resolve("report.html");

        HtmlReportExporter.exportHtml(data, out);

        Assertions.assertTrue(Files.exists(out));
        String content = Files.readString(out);
        Assertions.assertTrue(content.length() > 0);
        Assertions.assertTrue(content.contains("html-test-project"));
        Assertions.assertTrue(content.contains("<html") || content.contains("<!doctype"));
    }
}
