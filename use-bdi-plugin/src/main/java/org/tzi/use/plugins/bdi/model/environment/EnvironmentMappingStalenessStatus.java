package org.tzi.use.plugins.bdi.model.environment;

/** Revalidation state for a persisted environment mapping target. */
public enum EnvironmentMappingStalenessStatus {
    CURRENT,
    STALE,
    UNKNOWN
}
