package org.tzi.use.plugins.bdi.diagram;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;

public record DiagramNode(
        DiagramNodeType type,
        DiagramSelectionRef selection,
        String label,
        Optional<ProjectSourceId> source,
        Optional<DiagramIssueMarker> issueMarker,
        Map<String, String> attributes) {
    public DiagramNode {
        type = Objects.requireNonNull(type, "type");
        selection = Objects.requireNonNull(selection, "selection");
        DiagramValues.requireText(label, "label");
        source = Objects.requireNonNull(source, "source");
        issueMarker = Objects.requireNonNull(issueMarker, "issueMarker");
        attributes = DiagramValues.immutableSortedMap(attributes, "attributes");
    }

    public String id() {
        return DiagramIdentity.frame("diagram-node-v1", type.name(), selection.canonical());
    }
}
