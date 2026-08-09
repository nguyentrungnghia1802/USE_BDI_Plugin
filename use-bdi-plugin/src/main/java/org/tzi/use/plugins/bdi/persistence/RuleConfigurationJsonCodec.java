package org.tzi.use.plugins.bdi.persistence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.tzi.use.plugins.bdi.validation.RuleConfiguration;

/** Dependency-free JSON codec for the versioned rules.json schema. */
final class RuleConfigurationJsonCodec {
    private static final Set<String> FIELDS = Set.of("schemaVersion", "enabledRules");

    private RuleConfigurationJsonCodec() {
    }

    static String encode(RuleConfiguration configuration) {
        StringBuilder json = new StringBuilder();
        json.append("{\n  \"schemaVersion\":")
                .append(MappingJsonCodec.quote(configuration.schemaVersion()))
                .append(",\n  \"enabledRules\": [\n");
        List<String> ruleIds = configuration.enabledRuleIds().stream().sorted().toList();
        for (int index = 0; index < ruleIds.size(); index++) {
            json.append("    ").append(MappingJsonCodec.quote(ruleIds.get(index)));
            if (index + 1 < ruleIds.size()) {
                json.append(',');
            }
            json.append('\n');
        }
        return json.append("  ]\n}\n").toString();
    }

    static RuleConfiguration decode(String json) {
        Object parsed = MappingJsonCodec.parseJson(json);
        Map<String, Object> root = object(parsed, "root");
        Set<String> unknownFields = new TreeSet<>(root.keySet());
        unknownFields.removeAll(FIELDS);
        if (!unknownFields.isEmpty()) {
            throw new IllegalArgumentException("Unknown rule configuration fields: " + unknownFields);
        }
        String schemaVersion = string(root, "schemaVersion");
        List<Object> rawRules = array(root, "enabledRules");
        List<String> ruleIds = rawRules.stream()
                .map(value -> requireString(value, "enabledRules item"))
                .toList();
        return RuleConfiguration.from(schemaVersion, ruleIds);
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
        return requireString(object.get(field), field);
    }

    private static String requireString(Object value, String field) {
        if (!(value instanceof String text) || text.isBlank()) {
            throw new IllegalArgumentException(field + " must be a non-blank string");
        }
        return text;
    }
}
