package org.tzi.use.plugins.bdi.model.environment;

/** Plugin-owned typed record persisted by the environment mapping repository. */
public sealed interface PersistedEnvironmentMapping
        permits PersistedEnvironmentOperationMapping, PersistedEnvironmentPropertyMapping {
    String kind();

    String key();

    String workspace();

    String artifact();

    String artifactType();

    String umlTarget();

    EnvironmentMappingConfirmation confirmation();

    EnvironmentSourceProvenance provenance();

    EnvironmentMappingStaleness staleness();

    java.util.List<String> evidence();

    EnvironmentMapping toRuntimeMapping();

    PersistedEnvironmentMapping withStaleness(EnvironmentMappingStaleness value);

    default boolean isConfirmedCurrent() {
        return confirmation() == EnvironmentMappingConfirmation.CONFIRMED
                && staleness().status() == EnvironmentMappingStalenessStatus.CURRENT;
    }
}
