package org.tzi.use.plugins.bdi.model.mas;

import java.util.Objects;

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
        return json.append(project.resources().isEmpty() ? "]\n}\n" : "\n  ]\n}\n").toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }
}
