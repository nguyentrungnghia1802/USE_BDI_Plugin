package org.tzi.use.plugins.bdi.ui;

/** Presentation-only filters over one immutable diagram snapshot. */
public enum DiagramViewMode {
    ALL("All"),
    BDI_PLAN("BDI Plan"),
    AGENT_OVERVIEW("Agent Overview"),
    MAPPING("Mapping");

    private final String displayName;

    DiagramViewMode(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
