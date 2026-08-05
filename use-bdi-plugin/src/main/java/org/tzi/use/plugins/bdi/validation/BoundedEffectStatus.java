package org.tzi.use.plugins.bdi.validation;

/** Outcome of an effect attempted only in a disposable USE variation. */
public enum BoundedEffectStatus {
    PASS,
    INVARIANT_VIOLATED,
    SKIPPED,
    UNKNOWN
}
