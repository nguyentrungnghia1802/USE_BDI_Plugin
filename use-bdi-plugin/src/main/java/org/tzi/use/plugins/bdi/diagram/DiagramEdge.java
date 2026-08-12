package org.tzi.use.plugins.bdi.diagram;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record DiagramEdge(
        DiagramEdgeType type,
        String sourceNodeId,
        String targetNodeId,
        DiagramSelectionRef selection,
        Optional<String> label,
        Map<String, String> attributes) {
    public DiagramEdge {
        type = Objects.requireNonNull(type, "type");
        DiagramValues.requireText(sourceNodeId, "sourceNodeId");
        DiagramValues.requireText(targetNodeId, "targetNodeId");
        selection = Objects.requireNonNull(selection, "selection");
        label = Objects.requireNonNull(label, "label");
        label.ifPresent(value -> DiagramValues.requireText(value, "label"));
        attributes = DiagramValues.immutableSortedMap(attributes, "attributes");
    }

    public String id() {
        return DiagramIdentity.frame(
                "diagram-edge-v1",
                type.name(),
                sourceNodeId,
                targetNodeId,
                selection.canonical());
    }
}
