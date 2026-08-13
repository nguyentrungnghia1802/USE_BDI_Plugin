package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.SwingUtilities;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.diagram.DiagramModel;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;
import org.tzi.use.plugins.bdi.diagram.DiagramSelectionRef;

class BdiDiagramCanvasTest {
    @Test
    void clearsPixelsFromThePreviousDiagramBeforePaintingTheNextFrame() throws Exception {
        BdiDiagramCanvas canvas = new BdiDiagramCanvas();
        BufferedImage frame = new BufferedImage(640, 420, BufferedImage.TYPE_INT_ARGB);
        DiagramNode first = node("first");
        DiagramNode stale = node("stale");

        SwingUtilities.invokeAndWait(() -> {
            canvas.setSize(640, 420);
            canvas.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
            paint(canvas, frame, new DiagramModel(List.of(first, stale), List.of(), List.of()));
            assertNotEquals(canvas.getBackground().getRGB(), frame.getRGB(50, 140),
                    "the fixture pixel must belong to the second node before refresh");

            paint(canvas, frame, new DiagramModel(List.of(first), List.of(), List.of()));
            assertEquals(canvas.getBackground().getRGB(), frame.getRGB(50, 140),
                    "pixels from a removed node must be cleared on the next frame");
        });
    }

    private static void paint(BdiDiagramCanvas canvas, BufferedImage frame, DiagramModel model) {
        canvas.setModel(model);
        canvas.setLayoutSnapshot(BdiDiagramLayout.compute(model));
        java.awt.Graphics2D graphics = frame.createGraphics();
        try {
            canvas.paint(graphics);
        } finally {
            graphics.dispose();
        }
    }

    private static DiagramNode node(String reference) {
        return new DiagramNode(
                DiagramNodeType.ACTION,
                DiagramSelectionRef.of("canvas-test", reference),
                reference,
                Optional.empty(),
                Optional.empty(),
                Map.of());
    }
}
