package org.tzi.use.plugins.bdi.use;

import java.util.List;
import java.util.Objects;

public record UmlLinkRef(
        String associationName,
        List<String> objectNames,
        boolean virtual) {
    public UmlLinkRef {
        if (associationName == null || associationName.isBlank()) {
            throw new IllegalArgumentException("associationName must not be blank");
        }
        objectNames = List.copyOf(Objects.requireNonNull(objectNames, "objectNames"));
    }
}
