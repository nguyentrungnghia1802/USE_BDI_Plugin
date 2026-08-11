package org.tzi.use.plugins.bdi.model.organization;

import java.util.List;
import java.util.Objects;

public record OrganizationMissionMapping(
        String sourceQualifiedId,
        String umlOperation,
        OrganizationMappingConfirmation confirmation,
        List<String> evidence) implements OrganizationMapping {
    public OrganizationMissionMapping {
        sourceQualifiedId = OrganizationMapping.required(sourceQualifiedId, "sourceQualifiedId");
        umlOperation = OrganizationMapping.required(umlOperation, "umlOperation");
        confirmation = Objects.requireNonNull(confirmation, "confirmation");
        evidence = OrganizationMapping.evidence(evidence);
    }

    @Override
    public String target() {
        return umlOperation;
    }

    @Override
    public String key() {
        return "mission:" + sourceQualifiedId + "->" + umlOperation;
    }
}
