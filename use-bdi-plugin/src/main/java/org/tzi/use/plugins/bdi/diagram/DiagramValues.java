package org.tzi.use.plugins.bdi.diagram;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

final class DiagramValues {
    private DiagramValues() {
    }

    static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    static List<String> immutableTextList(List<String> values, String field) {
        List<String> copy = List.copyOf(Objects.requireNonNull(values, field));
        copy.forEach(value -> requireText(value, field + " entry"));
        return copy;
    }

    static Map<String, String> immutableSortedMap(Map<String, String> values, String field) {
        TreeMap<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> entry : Objects.requireNonNull(values, field).entrySet()) {
            requireText(entry.getKey(), field + " key");
            sorted.put(entry.getKey(), Objects.requireNonNull(entry.getValue(), field + " value"));
        }
        return Collections.unmodifiableMap(sorted);
    }
}
