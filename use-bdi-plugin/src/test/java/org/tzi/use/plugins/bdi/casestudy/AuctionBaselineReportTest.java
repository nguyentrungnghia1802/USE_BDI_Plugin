package org.tzi.use.plugins.bdi.casestudy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingFingerprint;
import org.tzi.use.plugins.bdi.report.HtmlReportExporter;
import org.tzi.use.plugins.bdi.report.ReportData;
import org.tzi.use.plugins.bdi.report.ReportExporter;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseUmlModelFacade;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;
import org.tzi.use.plugins.bdi.validation.ValidationContext;
import org.tzi.use.plugins.bdi.validation.ValidationOrchestrator;
import org.tzi.use.uml.sys.MSystem;

class AuctionBaselineReportTest {
    @TempDir
    Path tempDir;

    @Test
    void exportsDeterministicAuctionBaselineReportsFromTheRealPipeline() throws Exception {
        BdiImportSnapshot imported = new BdiImportService().importFiles(List.of(
                AuctionMappingFixtureTest.fixture("fixtures/casestudy/auction/auctioneer.asl"),
                AuctionMappingFixtureTest.fixture("fixtures/casestudy/auction/bidder.asl")));
        assertTrue(imported.diagnostics().isEmpty(), () -> "Unexpected diagnostics: " + imported.diagnostics());

        MSystem system = AuctionMappingFixtureTest.loadAuctionSystem();
        UseModelSnapshot uml = new UseUmlModelFacade().snapshot(system);
        MappingDocument mapping = AuctionMappingFixtureTest.confirmedMapping(imported, uml);
        List<ConsistencyIssue> issues = new ValidationOrchestrator()
                .evaluate(ValidationContext.from(imported, mapping, Optional.of(uml)));
        assertEquals(27, issues.size());
        assertEquals(Map.of(
                "BEL-001", 2L,
                "OCL-002", 4L,
                "OCL-004", 4L,
                "OWN-001", 3L,
                "REF-001", 10L,
                "SIG-002", 2L,
                "SIG-003", 2L), issues.stream()
                .collect(Collectors.groupingBy(
                        ConsistencyIssue::ruleId,
                        TreeMap::new,
                        Collectors.counting())));

        ReportData report = new ReportData(
                "Auction-Case-Study",
                "0.1.0",
                "USE-7.1.1",
                Instant.parse("2026-08-09T00:00:00Z"),
                issues.size(),
                mapping.bindings().size(),
                "Static Auction baseline; Jason 3.3.0; no mutant applied",
                Optional.of(uml.fingerprint()),
                Optional.of(MappingFingerprint.compute(mapping)),
                issues,
                List.of());

        Path reportDirectory = Path.of("target", "case-study", "auction")
                .toAbsolutePath()
                .normalize();
        Path json = reportDirectory.resolve("auction-baseline.json");
        Path jsonRepeat = tempDir.resolve("auction-baseline-repeat.json");
        Path html = reportDirectory.resolve("auction-baseline.html");
        Path htmlRepeat = tempDir.resolve("auction-baseline-repeat.html");
        ReportExporter.exportJson(report, json);
        ReportExporter.exportJson(report, jsonRepeat);
        HtmlReportExporter.exportHtml(report, html);
        HtmlReportExporter.exportHtml(report, htmlRepeat);

        assertTrue(Files.exists(json));
        assertTrue(Files.exists(html));
        String jsonContent = Files.readString(json);
        String htmlContent = Files.readString(html);
        assertEquals(jsonContent, Files.readString(jsonRepeat));
        assertEquals(htmlContent, Files.readString(htmlRepeat));
        assertTrue(jsonContent.contains("\"projectName\":\"Auction-Case-Study\""));
        assertTrue(jsonContent.contains("\"issuesCount\":" + issues.size()));
        assertTrue(jsonContent.contains("\"mappingsCount\":14"));
        assertTrue(jsonContent.contains("\"modelHash\":\"" + uml.fingerprint() + "\""));
        assertTrue(jsonContent.contains("\"mappingHash\":\"" + MappingFingerprint.compute(mapping) + "\""));
        assertTrue(htmlContent.contains("<h1>BDI Report</h1>"));
        assertTrue(htmlContent.contains("Auction-Case-Study"));
        assertTrue(htmlContent.contains("Consistency Issues"));
        assertTrue(htmlContent.contains(MappingFingerprint.compute(mapping)));
    }
}
