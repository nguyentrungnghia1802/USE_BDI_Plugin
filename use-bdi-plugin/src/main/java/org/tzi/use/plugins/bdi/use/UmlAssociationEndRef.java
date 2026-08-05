package org.tzi.use.plugins.bdi.use;

public record UmlAssociationEndRef(
        String associationName,
        String className,
        String roleName,
        String multiplicity,
        String aggregationKind,
        boolean ordered,
        boolean navigable,
        boolean explicitNavigable,
        boolean derived,
        boolean union) {
    public UmlAssociationEndRef {
        requireText(associationName, "associationName");
        requireText(className, "className");
        requireText(roleName, "roleName");
        requireText(multiplicity, "multiplicity");
        requireText(aggregationKind, "aggregationKind");
    }

    public String reference() {
        return associationName + "::" + roleName;
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
