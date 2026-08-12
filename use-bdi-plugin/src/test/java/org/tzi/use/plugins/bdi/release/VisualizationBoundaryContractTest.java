package org.tzi.use.plugins.bdi.release;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class VisualizationBoundaryContractTest {
    private static final List<String> PRESENTATION_FORBIDDEN_IMPORTS = List.of(
            "import jason.",
            "import jacamo.",
            "import cartago.",
            "import moise.",
            "import org.tzi.use.plugins.bdi.importer.",
            "import org.tzi.use.uml.",
            "StandardConsistencyRules",
            "ValidationOrchestrator",
            "EnvironmentConsistencyValidator",
            "OrganizationConsistencyValidator",
            "UseSnapshotOclEvaluator");

    @Test
    void diagramProjectionAndRenderingCannotReparseOrRevalidate() throws IOException {
        assertSourcesExclude(Path.of("src/main/java/org/tzi/use/plugins/bdi/diagram"),
                PRESENTATION_FORBIDDEN_IMPORTS);

        Path uiRoot = Path.of("src/main/java/org/tzi/use/plugins/bdi/ui");
        for (String sourceName : List.of(
                "BdiDiagramCanvas.java",
                "BdiDiagramLayout.java",
                "BdiDiagramPanel.java",
                "DiagramHighlightPath.java",
                "DiagramModeProjector.java",
                "DiagramNavigationProjector.java",
                "DiagramPalette.java",
                "DiagramSvgExporter.java",
                "DiagramVisualState.java",
                "DiagramVisualStateResolver.java")) {
            assertSourceExcludes(uiRoot.resolve(sourceName), PRESENTATION_FORBIDDEN_IMPORTS);
        }
    }

    @Test
    void validationCannotDependBackOnDiagramOrSwingPresentation() throws IOException {
        assertSourcesExclude(Path.of("src/main/java/org/tzi/use/plugins/bdi/validation"), List.of(
                "import org.tzi.use.plugins.bdi.diagram.",
                "import org.tzi.use.plugins.bdi.ui.",
                "import javax.swing.",
                "import java.awt."));
    }

    private static void assertSourcesExclude(Path root, List<String> forbiddenImports) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path source : paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList()) {
                assertSourceExcludes(source, forbiddenImports);
            }
        }
    }

    private static void assertSourceExcludes(Path source, List<String> forbiddenImports) throws IOException {
        String content = Files.readString(source);
        for (String forbiddenImport : forbiddenImports) {
            assertFalse(content.contains(forbiddenImport),
                    () -> source + " crosses the visualization boundary with " + forbiddenImport);
        }
    }
}
