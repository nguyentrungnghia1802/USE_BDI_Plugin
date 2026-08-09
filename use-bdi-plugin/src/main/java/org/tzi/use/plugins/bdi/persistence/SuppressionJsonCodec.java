package org.tzi.use.plugins.bdi.persistence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tzi.use.plugins.bdi.validation.Suppression;

/** Dependency-free JSON codec for the versioned suppressions.json schema. */
final class SuppressionJsonCodec {
    static final String CURRENT_SCHEMA_VERSION = "0.1.0";
    private static final Set<String> ROOT_FIELDS = Set.of("schemaVersion", "suppressions");
    private static final Set<String> SUPPRESSION_FIELDS = Set.of("ruleId", "sourceFingerprint", "reason");

    private SuppressionJsonCodec() {
    }

    static String encode(List<Suppression> suppressions) {
        List<Suppression> ordered = List.copyOf(suppressions);
        Set<String> keys = new HashSet<>();
        for (Suppression suppression : ordered) {
            if (suppression == null || !keys.add(suppression.key())) {
                throw new IllegalArgumentException("Duplicate or null suppression");
            }
        }
        ordered = ordered.stream().sorted(Comparator.comparing(Suppression::key)).toList();

        StringBuilder json = new StringBuilder();
        json.append("{\n  \"schemaVersion\":")
                .append(MappingJsonCodec.quote(CURRENT_SCHEMA_VERSION))
                .append(",\n  \"suppressions\": [");
        if (!ordered.isEmpty()) {
            json.append('\n');
        }
        for (int index = 0; index < ordered.size(); index++) {
            Suppression suppression = ordered.get(index);
            json.append("    {\"ruleId\":")
                    .append(MappingJsonCodec.quote(suppression.ruleId()))
                    .append(",\"sourceFingerprint\":")
                    .append(MappingJsonCodec.quote(suppression.sourceFingerprint()))
                    .append(",\"reason\":")
                    .append(MappingJsonCodec.quote(suppression.reason()))
                    .append('}');
            if (index + 1 < ordered.size()) {
                json.append(',');
            }
            json.append('\n');
        }
        return json.append("  ]\n}\n").toString();
    }

    static List<Suppression> decode(String json) {
        Map<String, Object> root = object(MappingJsonCodec.parseJson(json), "root");
        rejectUnknownFields(root, ROOT_FIELDS, "root");
        String schemaVersion = string(root, "schemaVersion");
        if (!CURRENT_SCHEMA_VERSION.equals(schemaVersion)) {
            throw new IllegalArgumentException("Unsupported suppression schema version: " + schemaVersion);
        }
        List<Suppression> suppressions = new ArrayList<>();
        for (Object raw : array(root, "suppressions")) {
            Map<String, Object> value = object(raw, "suppression");
            rejectUnknownFields(value, SUPPRESSION_FIELDS, "suppression");
            suppressions.add(new Suppression(
                    string(value, "ruleId"),
                    string(value, "sourceFingerprint"),
                    string(value, "reason")));
        }
        Set<String> keys = new HashSet<>();
        for (Suppression suppression : suppressions) {
            if (!keys.add(suppression.key())) {
                throw new IllegalArgumentException("Duplicate suppression: " + suppression.key());
            }
        }
        return List.copyOf(suppressions);
    }

    private static void rejectUnknownFields(Map<String, Object> value, Set<String> expected, String label) {
        Set<String> unknown = new java.util.TreeSet<>(value.keySet());
        unknown.removeAll(expected);
        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException("Unknown " + label + " fields: " + unknown);
        }
    }

    private static Map<String, Object> object(Object value, String label) {
        if (!(value instanceof Map<?, ?> raw)) {
            throw new IllegalArgumentException(label + " must be an object");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        raw.forEach((key, item) -> {
            if (!(key instanceof String name)) {
                throw new IllegalArgumentException(label + " has a non-string key");
            }
            result.put(name, item);
        });
        return result;
    }

    private static List<Object> array(Map<String, Object> object, String field) {
        Object value = object.get(field);
        if (!(value instanceof List<?> raw)) {
            throw new IllegalArgumentException(field + " must be an array");
        }
        return new ArrayList<>(raw);
    }

    private static String string(Map<String, Object> object, String field) {
        Object value = object.get(field);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new IllegalArgumentException(field + " must be a non-blank string");
        }
        return text;
    }
}
