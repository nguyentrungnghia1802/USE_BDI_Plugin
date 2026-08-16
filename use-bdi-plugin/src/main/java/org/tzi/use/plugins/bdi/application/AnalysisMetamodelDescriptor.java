package org.tzi.use.plugins.bdi.application;

/** Stable identity of the specification-only analysis profile used by one snapshot. */
public record AnalysisMetamodelDescriptor(String id, String version, String profileName) {
    public static final String CURRENT_ID = "https://useocl.github.io/bdi/metamodel/analysis/1.0";
    public static final String CURRENT_VERSION = "1.0.0";
    public static final String CURRENT_PROFILE_NAME = "JaCaMo Consistency Analysis Profile";

    public AnalysisMetamodelDescriptor {
        requireText(id, "id");
        requireText(version, "version");
        requireText(profileName, "profileName");
    }

    public static AnalysisMetamodelDescriptor current() {
        return new AnalysisMetamodelDescriptor(CURRENT_ID, CURRENT_VERSION, CURRENT_PROFILE_NAME);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
