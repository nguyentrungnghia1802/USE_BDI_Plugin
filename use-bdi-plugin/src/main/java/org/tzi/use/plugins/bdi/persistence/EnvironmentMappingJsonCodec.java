package org.tzi.use.plugins.bdi.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingConfirmation;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingDocument;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingStaleness;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentMappingStalenessStatus;
import org.tzi.use.plugins.bdi.model.environment.EnvironmentSourceProvenance;
import org.tzi.use.plugins.bdi.model.environment.PersistedEnvironmentMapping;
import org.tzi.use.plugins.bdi.model.environment.PersistedEnvironmentOperationMapping;
import org.tzi.use.plugins.bdi.model.environment.PersistedEnvironmentPropertyMapping;
import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;

/** Strict deterministic JSON codec for the separate CArtAgO mapping document. */
final class EnvironmentMappingJsonCodec {
    private static final Set<String> ROOT_FIELDS = Set.of("schemaVersion", "mappings");
    private static final Set<String> OPERATION_FIELDS = Set.of(
            "kind", "bdiAction", "actionArity", "workspace", "artifact", "artifactType",
            "operation", "operationArity", "parameterTypes", "umlTarget", "confirmation",
            "provenance", "staleness", "evidence");
    private static final Set<String> PROPERTY_FIELDS = Set.of(
            "kind", "belief", "beliefArity", "workspace", "artifact", "artifactType",
            "property", "propertyArity", "propertyType", "umlTarget", "confirmation",
            "provenance", "staleness", "evidence");
    private static final Set<String> PROVENANCE_FIELDS = Set.of("sourceId", "origin");
    private static final Set<String> STALENESS_FIELDS = Set.of("status", "reasons");

    private EnvironmentMappingJsonCodec() {
    }

    static String encode(EnvironmentMappingDocument document, java.nio.file.Path projectRoot) {
        document.validateProjectRoot(projectRoot);
        StringBuilder json = new StringBuilder("{\n");
        appendString(json, "schemaVersion", document.schemaVersion(), true);
        json.append("  \"mappings\": [");
        if (!document.mappings().isEmpty()) {
            json.append('\n');
        }
        for (int index = 0; index < document.mappings().size(); index++) {
            PersistedEnvironmentMapping mapping = document.mappings().get(index);
            json.append("    {");
            if (mapping instanceof PersistedEnvironmentOperationMapping operation) {
                appendOperation(json, operation);
            } else if (mapping instanceof PersistedEnvironmentPropertyMapping property) {
                appendProperty(json, property);
            } else {
                throw new IllegalArgumentException("Unsupported environment mapping type: " + mapping.getClass());
            }
            json.append("}");
            if (index + 1 < document.mappings().size()) {
                json.append(',');
            }
            json.append('\n');
        }
        json.append("  ]\n}\n");
        return json.toString();
    }

    static EnvironmentMappingDocument decode(String json, java.nio.file.Path projectRoot) {
        Map<String, Object> root = object(MappingJsonCodec.parseJson(json), "root");
        requireKeys(root, ROOT_FIELDS, "root");
        String schemaVersion = string(root, "schemaVersion");
        if (!EnvironmentMappingDocument.CURRENT_SCHEMA_VERSION.equals(schemaVersion)) {
            throw new IllegalArgumentException("Unsupported environment mapping schema version: " + schemaVersion);
        }
        List<PersistedEnvironmentMapping> mappings = new ArrayList<>();
        for (Object value : array(root, "mappings")) {
            Map<String, Object> mapping = object(value, "mapping");
            String kind = string(mapping, "kind");
            if ("OPERATION".equals(kind)) {
                requireKeys(mapping, OPERATION_FIELDS, "operation mapping");
                mappings.add(new PersistedEnvironmentOperationMapping(
                        string(mapping, "bdiAction"),
                        integer(mapping, "actionArity"),
                        string(mapping, "workspace"),
                        string(mapping, "artifact"),
                        string(mapping, "artifactType"),
                        string(mapping, "operation"),
                        integer(mapping, "operationArity"),
                        strings(mapping, "parameterTypes"),
                        string(mapping, "umlTarget"),
                        enumValue(EnvironmentMappingConfirmation.class, string(mapping, "confirmation")),
                        provenance(mapping.get("provenance")),
                        staleness(mapping.get("staleness")),
                        strings(mapping, "evidence")));
            } else if ("PROPERTY".equals(kind)) {
                requireKeys(mapping, PROPERTY_FIELDS, "property mapping");
                mappings.add(new PersistedEnvironmentPropertyMapping(
                        string(mapping, "belief"),
                        integer(mapping, "beliefArity"),
                        string(mapping, "workspace"),
                        string(mapping, "artifact"),
                        string(mapping, "artifactType"),
                        string(mapping, "property"),
                        integer(mapping, "propertyArity"),
                        string(mapping, "propertyType"),
                        string(mapping, "umlTarget"),
                        enumValue(EnvironmentMappingConfirmation.class, string(mapping, "confirmation")),
                        provenance(mapping.get("provenance")),
                        staleness(mapping.get("staleness")),
                        strings(mapping, "evidence")));
            } else {
                throw new IllegalArgumentException("Unknown environment mapping kind: " + kind);
            }
        }
        EnvironmentMappingDocument document = new EnvironmentMappingDocument(schemaVersion, mappings);
        document.validateProjectRoot(projectRoot);
        return document;
    }

    private static void appendOperation(StringBuilder json, PersistedEnvironmentOperationMapping mapping) {
        appendString(json, "kind", mapping.kind(), true);
        appendString(json, "bdiAction", mapping.bdiAction(), true);
        appendNumber(json, "actionArity", mapping.actionArity(), true);
        appendString(json, "workspace", mapping.workspace(), true);
        appendString(json, "artifact", mapping.artifact(), true);
        appendString(json, "artifactType", mapping.artifactType(), true);
        appendString(json, "operation", mapping.operation(), true);
        appendNumber(json, "operationArity", mapping.operationArity(), true);
        appendStrings(json, "parameterTypes", mapping.parameterTypes(), true);
        appendString(json, "umlTarget", mapping.umlTarget(), true);
        appendString(json, "confirmation", mapping.confirmation().name(), true);
        appendProvenance(json, mapping.provenance(), true);
        appendStaleness(json, mapping.staleness(), true);
        appendStrings(json, "evidence", mapping.evidence(), false);
    }

    private static void appendProperty(StringBuilder json, PersistedEnvironmentPropertyMapping mapping) {
        appendString(json, "kind", mapping.kind(), true);
        appendString(json, "belief", mapping.belief(), true);
        appendNumber(json, "beliefArity", mapping.beliefArity(), true);
        appendString(json, "workspace", mapping.workspace(), true);
        appendString(json, "artifact", mapping.artifact(), true);
        appendString(json, "artifactType", mapping.artifactType(), true);
        appendString(json, "property", mapping.property(), true);
        appendNumber(json, "propertyArity", mapping.propertyArity(), true);
        appendString(json, "propertyType", mapping.propertyType(), true);
        appendString(json, "umlTarget", mapping.umlTarget(), true);
        appendString(json, "confirmation", mapping.confirmation().name(), true);
        appendProvenance(json, mapping.provenance(), true);
        appendStaleness(json, mapping.staleness(), true);
        appendStrings(json, "evidence", mapping.evidence(), false);
    }

    private static void appendProvenance(
            StringBuilder json, EnvironmentSourceProvenance provenance, boolean comma) {
        json.append("\"provenance\":{\"sourceId\":")
                .append(MappingJsonCodec.quote(provenance.source().canonical()))
                .append(",\"origin\":")
                .append(MappingJsonCodec.quote(provenance.origin()))
                .append("}");
        if (comma) {
            json.append(',');
        }
    }

    private static void appendStaleness(
            StringBuilder json, EnvironmentMappingStaleness staleness, boolean comma) {
        json.append("\"staleness\":{\"status\":")
                .append(MappingJsonCodec.quote(staleness.status().name()))
                .append(",\"reasons\":");
        appendStringArray(json, staleness.reasons());
        json.append('}');
        if (comma) {
            json.append(',');
        }
    }

    private static void appendStrings(StringBuilder json, String name, List<String> values, boolean comma) {
        json.append(MappingJsonCodec.quote(name)).append(':');
        appendStringArray(json, values);
        if (comma) {
            json.append(',');
        }
    }

    private static void appendStringArray(StringBuilder json, List<String> values) {
        json.append('[');
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                json.append(',');
            }
            json.append(MappingJsonCodec.quote(values.get(index)));
        }
        json.append(']');
    }

    private static void appendString(StringBuilder json, String name, String value, boolean comma) {
        json.append(MappingJsonCodec.quote(name)).append(':').append(MappingJsonCodec.quote(value));
        if (comma) {
            json.append(',');
        }
    }

    private static void appendNumber(StringBuilder json, String name, int value, boolean comma) {
        json.append(MappingJsonCodec.quote(name)).append(':').append(value);
        if (comma) {
            json.append(',');
        }
    }

    private static EnvironmentSourceProvenance provenance(Object value) {
        Map<String, Object> object = object(value, "provenance");
        requireKeys(object, PROVENANCE_FIELDS, "provenance");
        return new EnvironmentSourceProvenance(
                ProjectSourceId.parse(string(object, "sourceId")),
                string(object, "origin"));
    }

    private static EnvironmentMappingStaleness staleness(Object value) {
        Map<String, Object> object = object(value, "staleness");
        requireKeys(object, STALENESS_FIELDS, "staleness");
        return new EnvironmentMappingStaleness(
                enumValue(EnvironmentMappingStalenessStatus.class, string(object, "status")),
                strings(object, "reasons"));
    }

    private static void requireKeys(Map<String, Object> object, Set<String> allowed, String label) {
        for (String key : object.keySet()) {
            if (!allowed.contains(key)) {
                throw new IllegalArgumentException("Unknown " + label + " field: " + key);
            }
        }
        for (String key : allowed) {
            if (!object.containsKey(key)) {
                throw new IllegalArgumentException("Missing " + label + " field: " + key);
            }
        }
    }

    private static Map<String, Object> object(Object value, String label) {
        if (!(value instanceof Map<?, ?> raw)) {
            throw new IllegalArgumentException(label + " must be an object");
        }
        java.util.LinkedHashMap<String, Object> result = new java.util.LinkedHashMap<>();
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

    private static int integer(Map<String, Object> object, String field) {
        Object value = object.get(field);
        if (!(value instanceof Integer || value instanceof Long)) {
            throw new IllegalArgumentException(field + " must be an integer");
        }
        try {
            return Math.toIntExact(((Number) value).longValue());
        } catch (ArithmeticException error) {
            throw new IllegalArgumentException(field + " is outside the integer range", error);
        }
    }

    private static List<String> strings(Map<String, Object> object, String field) {
        return array(object, field).stream().map(value -> {
            if (!(value instanceof String text) || text.isBlank()) {
                throw new IllegalArgumentException(field + " item must be a non-blank string");
            }
            return text;
        }).toList();
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value) {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException("Unknown " + type.getSimpleName() + ": " + value, error);
        }
    }
}
