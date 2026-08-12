package org.tzi.use.plugins.bdi.ui;

import java.util.Locale;
import java.util.Objects;

import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;

/** Resolves display state from the immutable diagram projection only. */
final class DiagramVisualStateResolver {
    private DiagramVisualStateResolver() {
    }

    static DiagramVisualState resolve(DiagramNode node) {
        Objects.requireNonNull(node, "node");
        if (node.issueMarker().isPresent()) {
            return switch (node.issueMarker().orElseThrow().certainty()) {
                case CONFIRMED -> DiagramVisualState.CONFIRMED_ISSUE;
                case POTENTIAL -> DiagramVisualState.POTENTIAL_ISSUE;
                case UNKNOWN -> DiagramVisualState.UNKNOWN;
            };
        }

        String mappingStatus = node.attributes().getOrDefault("mappingStatus", "");
        if ("MISSING".equalsIgnoreCase(mappingStatus) || node.type() == DiagramNodeType.GAP) {
            return DiagramVisualState.MISSING_MAPPING;
        }
        String targetState = node.attributes().getOrDefault("targetState", "")
                .toUpperCase(Locale.ROOT);
        if ("STALE".equals(targetState)) {
            return DiagramVisualState.STALE_MAPPING;
        }
        if ("UNKNOWN".equals(targetState)
                || IssueCertainty.UNKNOWN.name().equalsIgnoreCase(node.attributes().get("certainty"))) {
            return DiagramVisualState.UNKNOWN;
        }
        return DiagramVisualState.CLEAN;
    }
}
