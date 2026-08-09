package org.tzi.use.plugins.bdi.model.environment;

public record EnvironmentOperationMapping(
        String bdiAction,
        int actionArity,
        String workspace,
        String artifact,
        String operation,
        String umlTarget) implements EnvironmentMapping {
    public EnvironmentOperationMapping {
        EnvironmentOperation.requireText(bdiAction, "bdiAction");
        EnvironmentOperation.requireText(workspace, "workspace");
        EnvironmentOperation.requireText(artifact, "artifact");
        EnvironmentOperation.requireText(operation, "operation");
        EnvironmentOperation.requireText(umlTarget, "umlTarget");
        if (actionArity < 0) {
            throw new IllegalArgumentException("actionArity must not be negative");
        }
    }

    @Override
    public String key() {
        return "operation:" + bdiAction + "/" + actionArity + "->" + workspace + "/" + artifact + "#" + operation;
    }
}
