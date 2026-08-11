package org.tzi.use.plugins.bdi.validation.organization;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.tzi.use.plugins.bdi.model.organization.OrganizationMapping;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;

public record OrganizationValidationContext(
        OrganizationModel organization,
        UseModelSnapshot uml,
        List<OrganizationMapping> mappings) {
    public OrganizationValidationContext {
        organization = Objects.requireNonNull(organization, "organization");
        uml = Objects.requireNonNull(uml, "uml");
        mappings = List.copyOf(Objects.requireNonNull(mappings, "mappings")).stream()
                .sorted(Comparator.comparing(OrganizationMapping::key)).toList();
        if (mappings.stream().map(OrganizationMapping::key).distinct().count() != mappings.size()) {
            throw new IllegalArgumentException("Duplicate organization mapping");
        }
    }
}
