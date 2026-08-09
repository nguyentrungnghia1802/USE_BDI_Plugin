package org.tzi.use.plugins.bdi.use;

/** Captures a fresh immutable projection of the current USE model and state. */
@FunctionalInterface
public interface UseSnapshotProvider {
    UseSnapshotContext capture();
}
