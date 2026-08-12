package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.diagram.DiagramEdge;
import org.tzi.use.plugins.bdi.diagram.DiagramEdgeType;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;
import org.tzi.use.plugins.bdi.diagram.DiagramSelectionRef;

class DiagramSvgExporterTest {
    @TempDir
    Path tempDir;

    @Test
    void writesDeterministicUtf8EscapedSvgWithoutAbsoluteLabels() throws Exception {
        DiagramNode agent = node(DiagramNodeType.AGENT, "agent", "Đấu giá & kiểm tra");
        DiagramNode target = node(DiagramNodeType.UML_OPERATION, "target", "Auction::open() <safe>");
        DiagramNode privateSource = new DiagramNode(
                DiagramNodeType.TRACE_SOURCE,
                DiagramSelectionRef.of("source", "private-source"),
                "C:\\Users\\Alice\\secret.asl",
                Optional.empty(), Optional.empty(), Map.of());
        DiagramEdge edge = new DiagramEdge(
                DiagramEdgeType.MAPS_TO,
                agent.id(), target.id(),
                DiagramSelectionRef.of("edge", "maps"),
                Optional.of("maps <to>"),
                Map.of());
        DiagramModel model = new DiagramModel(List.of(agent, target, privateSource), List.of(edge), List.of());
        DiagramSvgExporter exporter = new DiagramSvgExporter();
        Path first = tempDir.resolve("first.svg");
        Path second = tempDir.resolve("second.svg");

        exporter.export(model, Set.of(agent.id()), Set.of(edge.id()), Optional.of(agent.id()), first, false);
        exporter.export(model, Set.of(agent.id()), Set.of(edge.id()), Optional.of(agent.id()), second, false);

        String svg = Files.readString(first);
        assertEquals(svg, Files.readString(second));
        assertTrue(svg.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(svg.contains("Đấu giá &amp; kiểm tra"));
        assertTrue(svg.contains("Auction::open() &lt;safe&gt;"));
        assertTrue(svg.contains("maps &lt;to&gt;"));
        assertTrue(svg.contains("[non-portable source omitted]"));
        assertFalse(svg.contains("C:\\Users"));
        assertTrue(svg.contains("SELECTED"));
    }

    @Test
    void refusesUnconfirmedOverwriteAndLeavesModelAndExistingFileUntouched() throws Exception {
        DiagramModel model = new DiagramModel(
                List.of(node(DiagramNodeType.AGENT, "agent", "auctioneer")), List.of(), List.of());
        DiagramSvgExporter exporter = new DiagramSvgExporter();
        Path output = tempDir.resolve("diagram.svg");
        exporter.export(model, Set.of(), Set.of(), Optional.empty(), output, false);
        String original = Files.readString(output);

        IOException error = assertThrows(IOException.class,
                () -> exporter.export(model, Set.of(), Set.of(), Optional.empty(), output, false));

        assertTrue(error.getMessage().contains("overwrite was not confirmed"));
        assertEquals(original, Files.readString(output));
        assertEquals(1, model.nodes().size());
    }

    @Test
    void panelExportsOnlyTheCurrentLayerFilteredProjection() throws Exception {
        DiagramNode agent = node(DiagramNodeType.AGENT, "agent", "auctioneer-visible");
        DiagramNode uml = new DiagramNode(
                DiagramNodeType.UML_CLASS,
                DiagramSelectionRef.of("uml", "Auctioneer"),
                "Auctioneer-hidden",
                Optional.empty(), Optional.empty(), Map.of("layer", "UML"));
        DiagramModel source = new DiagramModel(List.of(agent, uml), List.of(), List.of());
        BdiDiagramPanel panel = new BdiDiagramPanel();

        SwingUtilities.invokeAndWait(() -> panel.setDiagram(source));
        SwingUtilities.invokeAndWait(() -> panel.showUmlOclForTest().doClick());
        Path output = tempDir.resolve("filtered.svg");
        SwingUtilities.invokeAndWait(() -> {
            try {
                panel.exportSvgForTest(output, false);
            } catch (IOException error) {
                throw new AssertionError(error);
            }
        });

        String svg = Files.readString(output);
        assertTrue(panel.exportSvgForTest().isEnabled());
        assertTrue(svg.contains("auctioneer-visible"));
        assertFalse(svg.contains("Auctioneer-hidden"));
        assertEquals(source, panel.sourceModelForTest());
        assertEquals(List.of(agent), panel.modelForTest().nodes());
    }

    private static DiagramNode node(DiagramNodeType type, String namespace, String label) {
        return new DiagramNode(
                type,
                DiagramSelectionRef.of(namespace, label),
                label,
                Optional.empty(), Optional.empty(), Map.of());
    }
}
