package org.tzi.use.plugins.bdi.evaluation;

/** Outcome of one declared evaluation case, kept separate from process exit codes. */
public enum EvaluationStatus {
    PASS,
    DETECTED,
    MISSED,
    UNEXPECTED,
    UNKNOWN,
    UNSUPPORTED,
    INVALID_INPUT,
    TIMEOUT,
    EXECUTION_ERROR
}
