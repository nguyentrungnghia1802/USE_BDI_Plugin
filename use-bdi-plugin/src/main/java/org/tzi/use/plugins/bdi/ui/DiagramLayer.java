package org.tzi.use.plugins.bdi.ui;

import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;

/** Presentation-only layers that can be hidden without changing diagram evidence. */
enum DiagramLayer {
    ISSUES,
    UML_OCL,
    ORGANIZATION,
    ENVIRONMENT;

    boolean contains(DiagramNode node) {
        String layer = node.attributes().get("layer");
        return switch (this) {
            case ISSUES -> node.type() == DiagramNodeType.ISSUE || "ISSUE".equals(layer);
            case UML_OCL -> "UML".equals(layer) || switch (node.type()) {
                case UML_CLASS, UML_OBJECT, UML_ATTRIBUTE, UML_OPERATION, OCL_CONSTRAINT, TRACE_TARGET -> true;
                default -> false;
            };
            case ORGANIZATION -> "ORGANIZATION".equals(layer) || switch (node.type()) {
                case ORGANIZATION, ROLE, MISSION -> true;
                default -> false;
            };
            case ENVIRONMENT -> "ENVIRONMENT".equals(layer) || switch (node.type()) {
                case ARTIFACT, ARTIFACT_OPERATION -> true;
                default -> false;
            };
        };
    }
}
