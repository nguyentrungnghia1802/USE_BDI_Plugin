package org.tzi.use.plugins.bdi.use;

import java.util.Objects;

import org.tzi.use.plugins.bdi.validation.SnapshotOclEvaluator;

/** Immutable projection and evaluator captured from the same current USE system. */
public record UseSnapshotContext(
        UseModelSnapshot snapshot,
        SnapshotOclEvaluator oclEvaluator) {
    public UseSnapshotContext {
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(oclEvaluator, "oclEvaluator");
    }
}
