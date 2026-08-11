package org.tzi.use.plugins.bdi.model.mas;

import java.util.Objects;

import org.tzi.use.plugins.bdi.model.organization.OrganizationModel;

/** Deterministic portable serialization used by golden and relocation tests. */
public final class MasProjectModelJsonSerializer {
    public String serialize(MasProjectModel project) {
        Objects.requireNonNull(project, "project");
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"name\":\"").append(escape(project.name())).append("\",\n")
                .append("  \"source\":\"").append(escape(project.source().canonical())).append("\",\n")
                .append("  \"agents\":[");
        for (int index = 0; index < project.agents().size(); index++) {
            MasAgentInstanceModel agent = project.agents().get(index);
            json.append(index == 0 ? "\n" : ",\n")
                    .append("    {\"name\":\"").append(escape(agent.name()))
                    .append("\",\"source\":\"").append(escape(agent.source().canonical()))
                    .append("\",\"status\":\"").append(agent.status()).append("\"}");
        }
        json.append(project.agents().isEmpty() ? "],\n" : "\n  ],\n")
                .append("  \"resources\":[");
        for (int index = 0; index < project.resources().size(); index++) {
            MasResourceReference resource = project.resources().get(index);
            json.append(index == 0 ? "\n" : ",\n")
                    .append("    {\"kind\":\"").append(resource.kind())
                    .append("\",\"name\":\"").append(escape(resource.name()))
                    .append("\",\"source\":");
            resource.source().ifPresentOrElse(
                    source -> json.append('"').append(escape(source.canonical())).append('"'),
                    () -> json.append("null"));
            json.append(",\"status\":\"").append(resource.status()).append("\"}");
        }
        json.append(project.resources().isEmpty() ? "],\n" : "\n  ],\n")
                .append("  \"organizations\":[");
        for (int index = 0; index < project.organizations().size(); index++) {
            OrganizationModel organization = project.organizations().get(index);
            json.append(index == 0 ? "\n" : ",\n")
                    .append("    {\"id\":\"").append(escape(organization.id()))
                    .append("\",\"source\":\"").append(escape(organization.source().canonical()))
                    .append("\",\"positioned\":").append(organization.span().positioned())
                    .append(",\"roles\":[");
            appendIds(json, organization.roles().stream().map(OrganizationModel.Role::qualifiedId).toList());
            json.append("],\"groups\":[");
            appendIds(json, organization.groups().stream().map(OrganizationModel.Group::qualifiedId).toList());
            json.append("],\"schemes\":[");
            appendIds(json, organization.schemes().stream().map(OrganizationModel.Scheme::qualifiedId).toList());
            json.append("],\"norms\":[");
            appendIds(json, organization.norms().stream().map(OrganizationModel.Norm::qualifiedId).toList());
            json.append("],\"unsupported\":[");
            appendIds(json, organization.unsupportedFeatures().stream()
                    .map(OrganizationModel.UnsupportedFeature::code).toList());
            json.append("]}");
        }
        return json.append(project.organizations().isEmpty() ? "]\n}\n" : "\n  ]\n}\n").toString();
    }

    private static void appendIds(StringBuilder json, java.util.List<String> values) {
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                json.append(',');
            }
            json.append('"').append(escape(values.get(index))).append('"');
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }
}
