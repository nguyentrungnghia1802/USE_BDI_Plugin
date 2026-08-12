package org.tzi.use.plugins.bdi.ui;

import java.awt.Color;

/** Presentation state for a diagram node; semantic status remains in the model. */
enum DiagramVisualState {
    CLEAN("CLEAN", new Color(239, 242, 245), new Color(96, 105, 115), false),
    CONFIRMED_ISSUE("CONFIRMED ISSUE", new Color(255, 225, 225), new Color(176, 45, 45), false),
    POTENTIAL_ISSUE("POTENTIAL ISSUE", new Color(255, 239, 208), new Color(187, 117, 26), true),
    UNKNOWN("UNKNOWN", new Color(235, 235, 235), new Color(100, 100, 100), true),
    MISSING_MAPPING("MISSING MAPPING", new Color(255, 239, 208), new Color(202, 116, 38), true),
    STALE_MAPPING("STALE MAPPING", new Color(239, 229, 248), new Color(119, 76, 153), true);

    private final String badge;
    private final Color fill;
    private final Color border;
    private final boolean dashed;

    DiagramVisualState(String badge, Color fill, Color border, boolean dashed) {
        this.badge = badge;
        this.fill = fill;
        this.border = border;
        this.dashed = dashed;
    }

    String badge() {
        return badge;
    }

    Color fill() {
        return fill;
    }

    Color border() {
        return border;
    }

    boolean dashed() {
        return dashed;
    }
}
