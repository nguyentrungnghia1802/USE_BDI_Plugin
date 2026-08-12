package org.tzi.use.plugins.bdi.ui;

import java.awt.Color;

import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;

/** Shared presentation colors for the Swing canvas and deterministic SVG export. */
final class DiagramPalette {
    private DiagramPalette() {
    }

    static Color fill(DiagramNode node) {
        String layer = node.attributes().get("layer");
        if (layer != null) {
            return switch (layer) {
                case "MAS" -> new Color(232, 236, 241);
                case "BDI" -> new Color(224, 241, 229);
                case "ORGANIZATION" -> new Color(250, 239, 205);
                case "ENVIRONMENT" -> new Color(218, 241, 242);
                case "UML" -> new Color(222, 235, 248);
                case "ISSUE" -> new Color(255, 225, 225);
                case "EVIDENCE" -> new Color(235, 226, 248);
                default -> fill(node.type());
            };
        }
        return fill(node.type());
    }

    static Color cleanBorder(DiagramNodeType type) {
        return type == DiagramNodeType.ISSUE ? new Color(176, 45, 45)
                : type == DiagramNodeType.GAP ? new Color(202, 116, 38)
                : new Color(96, 105, 115);
    }

    private static Color fill(DiagramNodeType type) {
        return switch (type) {
            case ISSUE -> new Color(255, 225, 225);
            case GAP -> new Color(255, 239, 208);
            case UML_CLASS, UML_OBJECT, UML_ATTRIBUTE, UML_OPERATION, OCL_CONSTRAINT, TRACE_TARGET ->
                    new Color(222, 235, 248);
            case AGENT, MAS_PROJECT, ORGANIZATION, ROLE, MISSION -> new Color(224, 241, 229);
            default -> new Color(239, 242, 245);
        };
    }
}
