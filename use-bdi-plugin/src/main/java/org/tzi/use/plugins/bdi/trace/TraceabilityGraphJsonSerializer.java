package org.tzi.use.plugins.bdi.trace;

import java.util.Objects;

/** Deterministic debug serialization; no checkout-absolute paths are emitted. */
public final class TraceabilityGraphJsonSerializer {
    public String serialize(TraceabilityGraph graph) {
        Objects.requireNonNull(graph, "graph");
        StringBuilder json = new StringBuilder("{\"nodes\":[");
        for (int index = 0; index < graph.nodes().size(); index++) {
            TraceNode node = graph.nodes().get(index);
            if (index > 0) {
                json.append(',');
            }
            json.append("{\"id\":\"").append(escape(node.id()))
                    .append("\",\"kind\":\"").append(node.kind())
                    .append("\",\"label\":\"").append(escape(node.label()))
                    .append("\",\"status\":\"").append(node.status().map(Enum::name).orElse(""))
                    .append("\",\"certainty\":\"").append(node.certainty().map(Enum::name).orElse(""))
                    .append("\"}");
        }
        json.append("],\"edges\":[");
        for (int index = 0; index < graph.edges().size(); index++) {
            TraceEdge edge = graph.edges().get(index);
            if (index > 0) {
                json.append(',');
            }
            json.append("{\"id\":\"").append(escape(edge.id()))
                    .append("\",\"from\":\"").append(escape(edge.from()))
                    .append("\",\"to\":\"").append(escape(edge.to()))
                    .append("\",\"relation\":\"").append(edge.relation())
                    .append("\",\"certainty\":\"").append(edge.certainty()).append("\"}");
        }
        return json.append("]}\n").toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }
}
