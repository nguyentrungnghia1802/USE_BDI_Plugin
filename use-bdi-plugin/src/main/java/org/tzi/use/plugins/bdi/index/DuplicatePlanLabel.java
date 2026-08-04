package org.tzi.use.plugins.bdi.index;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.tzi.use.plugins.bdi.model.ir.SourceSpan;

/** Duplicate explicit plan label occurrences within one imported source. */
public record DuplicatePlanLabel(
        Path source,
        String label,
        List<SourceSpan> occurrences) {
    public DuplicatePlanLabel {
        source = Objects.requireNonNull(source, "source").toAbsolutePath().normalize();
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("label must not be blank");
        }
        occurrences = List.copyOf(Objects.requireNonNull(occurrences, "occurrences"));
        if (occurrences.size() < 2) {
            throw new IllegalArgumentException("duplicate label needs at least two occurrences");
        }
    }
}
