package org.tzi.use.plugins.bdi.use;

import java.util.List;
import java.util.Objects;

/** Immutable read-only projection of a USE model and its current system state. */
public record UseModelSnapshot(
        String modelName,
        String filename,
        List<UmlClassRef> classes,
        List<UmlAttributeRef> attributes,
        List<UmlAssociationRef> associations,
        List<UmlOperationRef> operations,
        List<UmlConstraintRef> classInvariants,
        List<UmlObjectRef> objects,
        List<UmlLinkRef> links,
        String fingerprint) {
    public UseModelSnapshot {
        requireText(modelName, "modelName");
        filename = Objects.requireNonNull(filename, "filename");
        classes = List.copyOf(Objects.requireNonNull(classes, "classes"));
        attributes = List.copyOf(Objects.requireNonNull(attributes, "attributes"));
        associations = List.copyOf(Objects.requireNonNull(associations, "associations"));
        operations = List.copyOf(Objects.requireNonNull(operations, "operations"));
        classInvariants = List.copyOf(Objects.requireNonNull(classInvariants, "classInvariants"));
        objects = List.copyOf(Objects.requireNonNull(objects, "objects"));
        links = List.copyOf(Objects.requireNonNull(links, "links"));
        requireText(fingerprint, "fingerprint");
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
