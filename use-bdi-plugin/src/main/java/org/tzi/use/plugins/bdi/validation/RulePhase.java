package org.tzi.use.plugins.bdi.validation;

/** Ordered static phases; snapshot OCL and bounded simulation are later slices. */
public enum RulePhase {
    PARSE,
    IR_WELL_FORMEDNESS,
    REFERENCE,
    MAPPING,
    SIGNATURE,
    SNAPSHOT_OCL,
    BOUNDED_SIMULATION
}
