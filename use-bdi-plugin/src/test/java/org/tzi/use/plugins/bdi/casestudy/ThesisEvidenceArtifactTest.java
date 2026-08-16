package org.tzi.use.plugins.bdi.casestudy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class ThesisEvidenceArtifactTest {
    @Test
    void keepsThesisEvidenceDocumentsAndUiScreenshotsTraceable() throws IOException {
        Path root = repositoryRoot();
        List<String> documents = List.of(
                "08_CONSISTENCY_RULE_CATALOG.md",
                "evidence/ui-screenshots.md",
                "evidence/auction-experiment-protocol.md",
                "evidence/auction-classification-metrics.md",
                "evidence/performance-baseline.md",
                "evidence/diagram-performance.md",
                "evidence/metamodel-coverage.md",
                "evidence/correspondence-coverage.md",
                "evidence/threats-to-validity.md",
                "evidence/limitations.md",
                "evidence/future-work.md",
                "evidence/traceability-diagram.mmd",
                "evidence/thesis-integration-validation.md",
                "research/thesis-integration-outline.md",
                "PLUGIN_INSTALL_GUIDE.md");
        for (String document : documents) {
            Path path = root.resolve("docs/project").resolve(document);
            assertTrue(Files.isRegularFile(path), () -> "Missing thesis evidence: " + path);
            assertTrue(Files.size(path) > 0, () -> "Empty thesis evidence: " + path);
        }

        String metamodelCoverage = Files.readString(root.resolve("docs/project/evidence/metamodel-coverage.md"));
        assertTrue(metamodelCoverage.contains("**48** | **43** | **4** | **1** | **0**"));
        assertTrue(metamodelCoverage.contains("not “100% semantic coverage.”"));
        String correspondenceCoverage = Files.readString(
                root.resolve("docs/project/evidence/correspondence-coverage.md"));
        assertTrue(correspondenceCoverage.contains("**11** | **11** | **11** | **11** | **8**"));
        assertTrue(correspondenceCoverage.contains("organization correspondence values are"));
        String diagramPerformance = Files.readString(root.resolve("docs/project/evidence/diagram-performance.md"));
        assertTrue(diagramPerformance.contains("DIAGRAM_PERFORMANCE_OK"));
        assertTrue(diagramPerformance.contains("Family Person"));
        assertTrue(diagramPerformance.contains("Smart Queue"));
        assertTrue(diagramPerformance.contains("Smart Home"));
        assertTrue(diagramPerformance.contains("Auction"));
        assertTrue(Files.isRegularFile(root.resolve("use-bdi-plugin/scripts/diagram-performance.ps1")));

        for (String image : List.of(
                "demo_import_button.png",
                "demo_bdi_explorer.png",
                "demo_bdi_explorer_detail.png",
                "demo_bdi_problems.png",
                "demo_uml_class_diagram.png")) {
            Path path = root.resolve("docs/report/images").resolve(image);
            assertTrue(Files.isRegularFile(path), () -> "Missing UI screenshot: " + path);
            assertTrue(Files.size(path) > 0, () -> "Empty UI screenshot: " + path);
        }
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("docs/project/16_PROJECT_COMPLETION_CHECKLIST.md"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate repository root from the test working directory");
    }
}
