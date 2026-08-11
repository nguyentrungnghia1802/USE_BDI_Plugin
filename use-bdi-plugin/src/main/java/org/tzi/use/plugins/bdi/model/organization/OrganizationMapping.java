package org.tzi.use.plugins.bdi.model.organization;

import java.util.List;

public sealed interface OrganizationMapping permits OrganizationRoleMapping,
        OrganizationMissionMapping, OrganizationCardinalityMapping {
    String sourceQualifiedId();

    String target();

    OrganizationMappingConfirmation confirmation();

    List<String> evidence();

    String key();

    static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    static List<String> evidence(List<String> values) {
        List<String> copy = List.copyOf(java.util.Objects.requireNonNull(values, "evidence"));
        if (copy.isEmpty() || copy.stream().anyMatch(value -> value == null || value.isBlank())) {
            throw new IllegalArgumentException("Organization mapping evidence must not be empty or blank");
        }
        return copy;
    }
}
