package org.tzi.use.plugins.bdi.release;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.diagram.DiagramEdgeType;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;
import org.tzi.use.plugins.bdi.ui.DiagramViewMode;

class GraphicalViewpointContractTest {
    @Test
    void mapsTheClosedDiagramVocabularyToMetamodelCorrespondenceAndTraceEvidence() throws Exception {
        Path root = repositoryRoot();
        Path metamodel = root.resolve("docs/project/metamodel");
        String syntax = Files.readString(metamodel.resolve("GRAPHICAL_CONCRETE_SYNTAX.md"));
        String viewpoints = Files.readString(metamodel.resolve("GRAPHICAL_VIEWPOINTS.md"));

        for (DiagramNodeType type : DiagramNodeType.values()) {
            assertTrue(syntax.contains("`" + type.name() + "`"),
                    () -> "Missing node concrete syntax: " + type);
        }
        for (DiagramEdgeType type : DiagramEdgeType.values()) {
            assertTrue(syntax.contains("`" + type.name() + "`"),
                    () -> "Missing edge concrete syntax: " + type);
        }
        for (DiagramViewMode mode : DiagramViewMode.values()) {
            assertTrue(viewpoints.contains("DiagramViewMode." + mode.name()),
                    () -> "Missing viewpoint mode: " + mode);
        }

        assertTrue(syntax.contains("read-only graphical concrete"));
        assertTrue(syntax.contains("FR-DIA-007 is **RESIDUAL**"));
        assertTrue(syntax.contains("No JaCaMo runtime | No Moise enactment | No live CArtAgO state"));
        assertTrue(syntax.contains("Sirius/DSML4JaCaMo"));
        assertTrue(syntax.contains("not an editable DSML graphical"));
        assertTrue(viewpoints.contains("Static MAS"));
        assertTrue(viewpoints.contains("Focused issue/evidence"));
        assertTrue(viewpoints.contains("Issues | `ISSUE`"));
    }

    @Test
    void preservesExactModesLayersVisualStatesAndOrderedStepExportContract() throws Exception {
        assertEquals(Set.of("ALL", "BDI_PLAN", "AGENT_OVERVIEW", "MAPPING"),
                names(DiagramViewMode.values()));
        assertEquals(Set.of("ISSUES", "UML_OCL", "ORGANIZATION", "ENVIRONMENT"),
                reflectedEnumNames("org.tzi.use.plugins.bdi.ui.DiagramLayer"));
        assertEquals(Set.of(
                "CLEAN", "CONFIRMED_ISSUE", "POTENTIAL_ISSUE", "UNKNOWN",
                "MISSING_MAPPING", "STALE_MAPPING"),
                reflectedEnumNames("org.tzi.use.plugins.bdi.ui.DiagramVisualState"));

        Path source = repositoryRoot().resolve("use-bdi-plugin/src/main/java/org/tzi/use/plugins/bdi");
        String builder = Files.readString(source.resolve("diagram/BdiDiagramBuilder.java"));
        String modes = Files.readString(source.resolve("ui/DiagramModeProjector.java"));
        String navigation = Files.readString(source.resolve("ui/DiagramNavigationProjector.java"));
        String svg = Files.readString(source.resolve("ui/DiagramSvgExporter.java"));
        assertTrue(builder.contains("index + 1"));
        assertTrue(builder.contains("Map.of(\"order\", Integer.toString(stepIndex))"));
        assertFalse(modes.contains("parse("));
        assertFalse(navigation.contains("validate("));
        assertTrue(svg.contains("state.badge()"));
        assertTrue(svg.contains("[non-portable source omitted]"));
    }

    private static Set<String> names(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).collect(Collectors.toSet());
    }

    private static Set<String> reflectedEnumNames(String name) throws Exception {
        Object[] constants = Class.forName(name).getEnumConstants();
        return Arrays.stream(constants).map(value -> ((Enum<?>) value).name()).collect(Collectors.toSet());
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
