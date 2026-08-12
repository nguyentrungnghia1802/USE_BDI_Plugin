package org.tzi.use.plugins.bdi.diagram;

final class DiagramIdentity {
    private DiagramIdentity() {
    }

    static String frame(String prefix, String... components) {
        StringBuilder identity = new StringBuilder(prefix);
        for (String component : components) {
            DiagramValues.requireText(component, "identity component");
            identity.append(':').append(component.length()).append(':').append(component);
        }
        return identity.toString();
    }
}
