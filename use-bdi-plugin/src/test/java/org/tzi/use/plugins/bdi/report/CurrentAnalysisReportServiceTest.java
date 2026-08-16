package org.tzi.use.plugins.bdi.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.application.AnalysisMetamodelDescriptor;
import org.tzi.use.plugins.bdi.application.AnalysisVersionMetadata;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshot;
import org.tzi.use.plugins.bdi.index.BdiIndex;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingFingerprint;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueSeverity;
import org.tzi.use.plugins.bdi.validation.IssueStatus;
import org.tzi.use.plugins.bdi.validation.Suppression;

class CurrentAnalysisReportServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void exportsJsonAndHtmlFromTheSameCurrentSnapshot() throws Exception {
        CurrentAnalysisSnapshot snapshot = snapshot();
        CurrentAnalysisReportService service = new CurrentAnalysisReportService();
        Path json = tempDir.resolve("analysis.json");
        Path directJson = tempDir.resolve("direct.json");
        Path html = tempDir.resolve("analysis.html");
        Path directHtml = tempDir.resolve("direct.html");

        service.export("Đấu giá", snapshot, ReportFormat.JSON, json, false);
        service.export("Đấu giá", snapshot, ReportFormat.HTML, html, false);
        ReportData direct = service.toReportData("Đấu giá", snapshot);
        ReportExporter.exportJson(direct, directJson);
        HtmlReportExporter.exportHtml(direct, directHtml);

        assertEquals(Files.readString(directJson), Files.readString(json));
        assertEquals(Files.readString(directHtml), Files.readString(html));
        String jsonContent = Files.readString(json, StandardCharsets.UTF_8);
        String htmlContent = Files.readString(html, StandardCharsets.UTF_8);
        for (String evidence : List.of(
                "CFG: quy tắc chuẩn", "MAP-003", "target Auction::missing", snapshot.mappingHash(),
                snapshot.suppressions().get(0).sourceFingerprint())) {
            assertTrue(jsonContent.contains(evidence), evidence);
            assertTrue(htmlContent.contains(evidence), evidence);
        }
        assertTrue(jsonContent.contains("Đấu giá"));
        assertTrue(htmlContent.contains("Đấu giá"));
        for (String metadata : List.of(
                AnalysisMetamodelDescriptor.CURRENT_ID,
                AnalysisMetamodelDescriptor.CURRENT_VERSION,
                AnalysisMetamodelDescriptor.CURRENT_PROFILE_NAME,
                snapshot.versions().bdiMetamodelVersion())) {
            assertTrue(jsonContent.contains(metadata), metadata);
            assertTrue(htmlContent.contains(metadata), metadata);
        }
        assertTrue(jsonContent.contains("\"parserVersions\":[]"));
    }

    @Test
    void refusesUnconfirmedOverwriteAndLeavesFailedTargetAbsent() throws Exception {
        CurrentAnalysisReportService service = new CurrentAnalysisReportService();
        Path existing = Files.writeString(tempDir.resolve("existing.json"), "keep");

        assertThrows(java.io.IOException.class, () ->
                service.export("test", snapshot(), ReportFormat.JSON, existing, false));
        assertEquals("keep", Files.readString(existing));

        Path parentFile = Files.writeString(tempDir.resolve("not-a-directory"), "keep");
        Path failedTarget = parentFile.resolve("analysis.json");
        assertThrows(java.io.IOException.class, () ->
                service.export("test", snapshot(), ReportFormat.JSON, failedTarget, false));
        assertFalse(Files.exists(failedTarget));
    }

    private static CurrentAnalysisSnapshot snapshot() {
        BdiImportSnapshot imported = new BdiImportSnapshot(List.of(), List.of(), BdiIndex.empty());
        MappingDocument mapping = MappingDocument.empty("unknown");
        ConsistencyIssue issue = new ConsistencyIssue(
                "MAP-003",
                IssueSeverity.ERROR,
                IssueStatus.OPEN,
                "Missing target <Auction>",
                Optional.of("auctioneer"),
                Optional.empty(),
                Optional.of(new SourceSpan(Path.of("auctioneer.asl"), 3, 1, 3, 8)),
                Optional.of("Auction::missing"),
                List.of("target Auction::missing"),
                Optional.of("Confirm a valid target"),
                IssueCertainty.CONFIRMED);
        Suppression suppression = new Suppression("MAP-003", "a".repeat(64), "reviewed");
        return new CurrentAnalysisSnapshot(
                Instant.parse("2026-08-10T00:00:00Z"),
                imported,
                Optional.empty(),
                mapping,
                "CFG: quy tắc chuẩn",
                List.of(suppression),
                List.of(issue),
                0,
                0,
                1,
                Optional.empty(),
                MappingFingerprint.compute(mapping),
                new AnalysisVersionMetadata(
                        "0.1.0", "USE-7.1.1", imported.index().metamodelVersion(), List.of()));
    }
}
