package org.tzi.use.plugins.bdi.report;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

public class ReportExporterTest {

    private static final String MODEL_HASH = "a".repeat(64);
    private static final String MAPPING_HASH = "b".repeat(64);
    private static final String SUPPRESSION_FINGERPRINT = "e".repeat(64);

    @Test
    public void exportJson_writesFile() throws Exception {
        ReportData data = new ReportData(
                "test-project",
                "0.1.0-test",
                "USE-TEST-7.1.1",
                Instant.now(),
                3,
                5,
                "unit-test",
                Optional.of(MODEL_HASH),
                Optional.of(MAPPING_HASH),
                List.of(new ConsistencyIssue(
                        "ASL-001",
                        IssueSeverity.ERROR,
                        IssueStatus.OPEN,
                        "bad \"syntax\"",
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(new SourceSpan(Path.of("bad.asl"), 2, 3, 2, 4)),
                        Optional.empty(),
                        List.of("parser evidence"),
                        Optional.empty(),
                        IssueCertainty.CONFIRMED)),
                List.of(new Suppression(
                        "ASL-001",
                        SUPPRESSION_FINGERPRINT,
                        "accepted parser fixture"))
        );

        Path tmp = Files.createTempDirectory("bdi-report-test");
        Path out = tmp.resolve("report.json");

        ReportExporter.exportJson(data, out);

        Assertions.assertTrue(Files.exists(out));
        String content = Files.readString(out);
        Assertions.assertTrue(Files.size(out) > 0);
        Assertions.assertTrue(content.contains("\"issues\":[{"));
        Assertions.assertTrue(content.contains("ASL-001"));
        Assertions.assertTrue(content.contains("bad \\\"syntax\\\""));
        Assertions.assertTrue(content.contains("bad.asl:2:3"));
        Assertions.assertTrue(content.contains("\"modelHash\":\"" + MODEL_HASH + "\""));
        Assertions.assertTrue(content.contains("\"mappingHash\":\"" + MAPPING_HASH + "\""));
        Assertions.assertTrue(content.contains("\"suppressions\":[{"));
        Assertions.assertTrue(content.contains(SUPPRESSION_FINGERPRINT));
        Assertions.assertTrue(content.contains("accepted parser fixture"));
    }
}
