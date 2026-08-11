package org.tzi.use.plugins.bdi.model.organization;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.Cardinality;

/** Confirmed OCL target plus optional reviewer-normalized static bounds. */
public record OrganizationCardinalityMapping(
        String groupQualifiedId,
        String sourceQualifiedId,
        String oclConstraint,
        Optional<Cardinality> normalizedOclBounds,
        OrganizationMappingConfirmation confirmation,
        List<String> evidence) implements OrganizationMapping {
    public OrganizationCardinalityMapping {
        groupQualifiedId = OrganizationMapping.required(groupQualifiedId, "groupQualifiedId");
        sourceQualifiedId = OrganizationMapping.required(sourceQualifiedId, "sourceQualifiedId");
        oclConstraint = OrganizationMapping.required(oclConstraint, "oclConstraint");
        normalizedOclBounds = Objects.requireNonNull(normalizedOclBounds, "normalizedOclBounds");
        confirmation = Objects.requireNonNull(confirmation, "confirmation");
        evidence = OrganizationMapping.evidence(evidence);
    }

    @Override
    public String target() {
        return oclConstraint;
    }

    @Override
    public String key() {
        return "cardinality:" + groupQualifiedId + "/" + sourceQualifiedId + "->" + oclConstraint;
    }
}
