package org.tzi.use.plugins.bdi.model.ir;

import java.nio.file.Path;
import java.util.Objects;

/**
 * File-level root of the Java-only BDI intermediate representation.
 *
 * <p>Child IR nodes are intentionally introduced by later slices. The root
 * still gives import and reporting code a stable model value without
 * inventing an AgentSpeak agent name that is not declared by the source.</p>
 */
public record AgentModel(
        Path source,
        String parserVersion,
        int beliefCount,
        int goalCount,
        int planCount) {
    public AgentModel {
        source = Objects.requireNonNull(source, "source").toAbsolutePath().normalize();
        parserVersion = Objects.requireNonNull(parserVersion, "parserVersion");
        requireNonNegative("beliefCount", beliefCount);
        requireNonNegative("goalCount", goalCount);
        requireNonNegative("planCount", planCount);
    }

    public int elementCount() {
        return beliefCount + goalCount + planCount;
    }

    private static void requireNonNegative(String name, int value) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
