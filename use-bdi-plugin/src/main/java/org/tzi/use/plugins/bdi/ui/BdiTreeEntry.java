package org.tzi.use.plugins.bdi.ui;

import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.ir.SourceSpan;

/** Display data kept in a Swing tree node. */
public record BdiTreeEntry(
        String label,
        String details,
        Optional<SourceSpan> sourceSpan) {
    public BdiTreeEntry {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("label must not be blank");
        }
        Objects.requireNonNull(details, "details");
        sourceSpan = Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    @Override
    public String toString() {
        return label;
    }
}
