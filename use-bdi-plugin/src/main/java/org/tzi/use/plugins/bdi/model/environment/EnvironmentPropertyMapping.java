package org.tzi.use.plugins.bdi.model.environment;

public record EnvironmentPropertyMapping(
        String belief,
        String workspace,
        String artifact,
        String property,
        int propertyArity,
        String umlTarget) implements EnvironmentMapping {
    public EnvironmentPropertyMapping {
        EnvironmentOperation.requireText(belief, "belief");
        EnvironmentOperation.requireText(workspace, "workspace");
        EnvironmentOperation.requireText(artifact, "artifact");
        EnvironmentOperation.requireText(property, "property");
        EnvironmentOperation.requireText(umlTarget, "umlTarget");
        if (propertyArity < 0) {
            throw new IllegalArgumentException("propertyArity must not be negative");
        }
    }

    @Override
    public String key() {
        return "property:" + belief + "->" + workspace + "/" + artifact + "#" + property + "/" + propertyArity;
    }
}
