package org.tzi.use.plugins.bdi.model.environment;

public sealed interface EnvironmentMapping permits EnvironmentOperationMapping, EnvironmentPropertyMapping {
    String workspace();
    String artifact();
    String umlTarget();
    String key();
}
