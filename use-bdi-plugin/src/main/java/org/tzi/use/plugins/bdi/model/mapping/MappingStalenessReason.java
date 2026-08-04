package org.tzi.use.plugins.bdi.model.mapping;

/** Reasons why a persisted mapping needs revalidation against a new snapshot. */
public enum MappingStalenessReason {
    BDI_METAMODEL_VERSION_CHANGED,
    USE_FINGERPRINT_CHANGED,
    SOURCE_MISSING,
    TARGET_MISSING
}
