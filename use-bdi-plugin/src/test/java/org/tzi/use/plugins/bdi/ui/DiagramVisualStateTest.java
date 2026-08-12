package org.tzi.use.plugins.bdi.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.diagram.DiagramIssueMarker;
import org.tzi.use.plugins.bdi.diagram.DiagramNode;
import org.tzi.use.plugins.bdi.diagram.DiagramNodeType;
import org.tzi.use.plugins.bdi.diagram.DiagramSelectionRef;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueSeverity;
import org.tzi.use.plugins.bdi.validation.IssueStatus;

class DiagramVisualStateTest {
    @Test
    void distinguishesIssueCertaintyAndMappingLifecycleStates() {
        assertEquals(DiagramVisualState.CONFIRMED_ISSUE,
                DiagramVisualStateResolver.resolve(issue(IssueCertainty.CONFIRMED)));
        assertEquals(DiagramVisualState.POTENTIAL_ISSUE,
                DiagramVisualStateResolver.resolve(issue(IssueCertainty.POTENTIAL)));
        assertEquals(DiagramVisualState.UNKNOWN,
                DiagramVisualStateResolver.resolve(issue(IssueCertainty.UNKNOWN)));
        assertEquals(DiagramVisualState.MISSING_MAPPING,
                DiagramVisualStateResolver.resolve(node(DiagramNodeType.GAP, Map.of(
                        "mappingStatus", "MISSING"))));
        assertEquals(DiagramVisualState.STALE_MAPPING,
                DiagramVisualStateResolver.resolve(node(DiagramNodeType.UML_OPERATION, Map.of(
                        "mappingStatus", "CONFIRMED", "targetState", "STALE"))));
        assertEquals(DiagramVisualState.UNKNOWN,
                DiagramVisualStateResolver.resolve(node(DiagramNodeType.UML_OPERATION, Map.of(
                        "mappingStatus", "CONFIRMED", "targetState", "UNKNOWN"))));
        assertEquals(DiagramVisualState.CLEAN,
                DiagramVisualStateResolver.resolve(node(DiagramNodeType.ACTION, Map.of())));
    }

    private static DiagramNode issue(IssueCertainty certainty) {
        return new DiagramNode(
                DiagramNodeType.ISSUE,
                DiagramSelectionRef.of("issue", certainty.name()),
                certainty.name(),
                Optional.empty(),
                Optional.of(new DiagramIssueMarker(
                        "MAP-003", IssueSeverity.ERROR, IssueStatus.OPEN, certainty, List.of("evidence"))),
                Map.of());
    }

    private static DiagramNode node(DiagramNodeType type, Map<String, String> attributes) {
        return new DiagramNode(
                type,
                DiagramSelectionRef.of("state", type.name()),
                type.name(),
                Optional.empty(),
                Optional.empty(),
                attributes);
    }
}
