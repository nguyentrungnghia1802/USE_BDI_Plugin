package org.tzi.use.plugins.bdi.report;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.application.AnalysisMetamodelDescriptor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueSeverity;
import org.tzi.use.plugins.bdi.validation.IssueStatus;
import org.tzi.use.plugins.bdi.validation.Suppression;

public class HtmlReportExporterTest {

    private static final String MODEL_HASH = "c".repeat(64);
    private static final String MAPPING_HASH = "d".repeat(64);
    private static final String SUPPRESSION_FINGERPRINT = "f".repeat(64);

    @Test
    public void exportHtml_writesFileAndContainsProjectName() throws Exception {
        ReportData data = new ReportData(
                "html-test-project",
                "0.1.0-test",
                "USE-TEST-7.1.1",
                Instant.now(),
                1,
                2,
                "<script>alert('x')</script>",
                Optional.of(MODEL_HASH),
                Optional.of(MAPPING_HASH),
                List.of(new ConsistencyIssue(
                        "MAP-001",
                        IssueSeverity.ERROR,
                        IssueStatus.OPEN,
                        "Missing <agent> mapping",
                        Optional.of("agent"),
                        Optional.empty(),
                        Optional.of(new SourceSpan(Path.of("agent.asl"), 4, 2, 4, 8)),
                        Optional.of("Worker"),
                        List.of("source <agent>", "target & absent"),
                        Optional.of("Add mapping"),
                        IssueCertainty.CONFIRMED)),
                List.of(new Suppression(
                        "MAP-001",
                        SUPPRESSION_FINGERPRINT,
                        "mapping approved for baseline"))
        );

        Path tmp = Files.createTempDirectory("bdi-html-report-test");
        Path out = tmp.resolve("report.html");

        HtmlReportExporter.exportHtml(data, out);

        Assertions.assertTrue(Files.exists(out));
        String content = Files.readString(out);
        Assertions.assertTrue(content.length() > 0);
        Assertions.assertTrue(content.contains("html-test-project"));
        Assertions.assertTrue(content.contains("<html") || content.contains("<!doctype"));
        Assertions.assertTrue(content.contains("MAP-001"));
        Assertions.assertTrue(content.contains("agent.asl:4:2"));
        Assertions.assertTrue(content.contains("source &lt;agent&gt;"));
        Assertions.assertTrue(content.contains("&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;"));
        Assertions.assertTrue(content.contains(MODEL_HASH));
        Assertions.assertTrue(content.contains(MAPPING_HASH));
        Assertions.assertTrue(content.contains("Suppressions Count"));
        Assertions.assertTrue(content.contains(SUPPRESSION_FINGERPRINT));
        Assertions.assertTrue(content.contains("mapping approved for baseline"));
        Assertions.assertTrue(content.contains("Analysis Metamodel ID"));
        Assertions.assertTrue(content.contains(AnalysisMetamodelDescriptor.CURRENT_ID));
        Assertions.assertTrue(content.contains("JaCaMo Consistency Analysis Profile"));
        Assertions.assertFalse(content.contains("<script>alert"));
    }
}
