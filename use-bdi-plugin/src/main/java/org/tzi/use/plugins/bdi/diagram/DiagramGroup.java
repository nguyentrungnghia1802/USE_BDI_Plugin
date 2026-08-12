package org.tzi.use.plugins.bdi.diagram;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record DiagramGroup(
        DiagramSelectionRef selection,
        String label,
        List<String> nodeIds,
        Map<String, String> attributes) {
    public DiagramGroup {
        selection = Objects.requireNonNull(selection, "selection");
        DiagramValues.requireText(label, "label");
        nodeIds = DiagramValues.immutableTextList(nodeIds, "nodeIds");
        Set<String> uniqueNodeIds = new HashSet<>();
        for (String nodeId : nodeIds) {
            if (!uniqueNodeIds.add(nodeId)) {
                throw new IllegalArgumentException("Diagram group contains duplicate node ID: " + nodeId);
            }
        }
        nodeIds = nodeIds.stream().sorted().toList();
        attributes = DiagramValues.immutableSortedMap(attributes, "attributes");
    }

    public String id() {
        return DiagramIdentity.frame("diagram-group-v1", selection.canonical());
    }
}
