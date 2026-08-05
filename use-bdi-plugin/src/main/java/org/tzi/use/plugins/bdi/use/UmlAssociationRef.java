package org.tzi.use.plugins.bdi.use;

import java.util.List;
import java.util.Objects;

public record UmlAssociationRef(
        String name,
        boolean derived,
        boolean union,
        List<UmlAssociationEndRef> ends) {
    public UmlAssociationRef {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        ends = List.copyOf(Objects.requireNonNull(ends, "ends"));
    }

    public String reference() {
        return name;
    }
}
