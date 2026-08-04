package org.tzi.use.plugins.bdi.problems;

public enum BdiProblemGrouping {
    NONE("No grouping"),
    GROUP("Problem group"),
    SOURCE("Source file"),
    CODE("Problem code");

    private final String label;

    BdiProblemGrouping(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
