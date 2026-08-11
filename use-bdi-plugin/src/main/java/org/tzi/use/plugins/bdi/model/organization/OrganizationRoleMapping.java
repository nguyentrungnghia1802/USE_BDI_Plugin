package org.tzi.use.plugins.bdi.model.organization;

import java.util.List;
import java.util.Objects;

public record OrganizationRoleMapping(
        String sourceQualifiedId,
        String umlClass,
        OrganizationMappingConfirmation confirmation,
        List<String> evidence) implements OrganizationMapping {
    public OrganizationRoleMapping {
        sourceQualifiedId = OrganizationMapping.required(sourceQualifiedId, "sourceQualifiedId");
        umlClass = OrganizationMapping.required(umlClass, "umlClass");
        confirmation = Objects.requireNonNull(confirmation, "confirmation");
        evidence = OrganizationMapping.evidence(evidence);
    }

    @Override
    public String target() {
        return umlClass;
    }

    @Override
    public String key() {
        return "role:" + sourceQualifiedId + "->" + umlClass;
    }
}
