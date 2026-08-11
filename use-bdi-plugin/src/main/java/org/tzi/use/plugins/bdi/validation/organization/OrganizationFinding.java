package org.tzi.use.plugins.bdi.validation.organization;

import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.organization.OrganizationMapping;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.SourceSpan;
import org.tzi.use.plugins.bdi.validation.ConsistencyIssue;

public record OrganizationFinding(
        OrganizationMapping mapping,
        Optional<SourceSpan> organizationSource,
        ConsistencyIssue issue) {
    public OrganizationFinding {
        mapping = Objects.requireNonNull(mapping, "mapping");
        organizationSource = Objects.requireNonNull(organizationSource, "organizationSource");
        issue = Objects.requireNonNull(issue, "issue");
    }
}
