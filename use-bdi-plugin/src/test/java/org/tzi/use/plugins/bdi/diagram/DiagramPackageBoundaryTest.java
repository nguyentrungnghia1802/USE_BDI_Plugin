package org.tzi.use.plugins.bdi.diagram;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class DiagramPackageBoundaryTest {
    private static final List<Class<?>> PUBLIC_VALUES = List.of(
            DiagramModel.class,
            DiagramNode.class,
            DiagramEdge.class,
            DiagramGroup.class,
            DiagramIssueMarker.class,
            DiagramSelectionRef.class,
            DiagramNodeType.class,
            DiagramEdgeType.class);

    @Test
    void publicDiagramValuesAreFinalAndPluginOwned() {
        List<String> forbiddenTypes = List.of(
                "jason.",
                "jacamo.",
                "cartago.",
                "moise.",
                "javax.swing",
                "java.awt",
                "org.tzi.use.gui",
                "org.tzi.use.uml");

        for (Class<?> value : PUBLIC_VALUES) {
            assertTrue(Modifier.isFinal(value.getModifiers()), () -> value.getName() + " must be final");
            if (value.isRecord()) {
                for (java.lang.reflect.RecordComponent component : value.getRecordComponents()) {
                    assertTrue(forbiddenTypes.stream()
                                    .noneMatch(component.getGenericType().getTypeName()::contains),
                            () -> "Forbidden diagram component type: " + component.getGenericType());
                }
            }
        }
    }

    @Test
    void productionPackageHasNoAdapterRuntimeOrUiImports() throws IOException {
        Path packageRoot = Path.of("src/main/java/org/tzi/use/plugins/bdi/diagram");
        List<String> forbiddenImports = List.of(
                "import jason.",
                "import jacamo.",
                "import cartago.",
                "import moise.",
                "import javax.swing",
                "import java.awt",
                "import org.tzi.use.gui",
                "import org.tzi.use.uml");

        try (var files = Files.list(packageRoot)) {
            for (Path source : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                String content = Files.readString(source);
                for (String forbiddenImport : forbiddenImports) {
                    assertFalse(content.contains(forbiddenImport),
                            () -> source + " contains forbidden boundary import " + forbiddenImport);
                }
            }
        }
    }
}
