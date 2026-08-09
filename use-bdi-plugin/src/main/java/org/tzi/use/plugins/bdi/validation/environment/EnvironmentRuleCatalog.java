package org.tzi.use.plugins.bdi.validation.environment;

import java.util.List;

public final class EnvironmentRuleCatalog {
    public static final String OPERATION_EXISTS = "ENV-001";
    public static final String OPERATION_ARITY = "ENV-002";
    public static final String PROPERTY_ATTRIBUTE = "ENV-003";

    private EnvironmentRuleCatalog() {
    }

    public static List<String> ids() {
        return List.of(OPERATION_EXISTS, OPERATION_ARITY, PROPERTY_ATTRIBUTE);
    }
}
