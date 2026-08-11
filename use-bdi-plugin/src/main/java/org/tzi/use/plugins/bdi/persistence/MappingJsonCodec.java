package org.tzi.use.plugins.bdi.persistence;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.mapping.MappingBinding;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;

/** Small dependency-free JSON codec for the deliberately narrow mapping schema. */
final class MappingJsonCodec {
    private static final String LEGACY_SCHEMA_VERSION = "0.1.0";

    private MappingJsonCodec() {
    }

    static String encode(MappingDocument document, Path projectRoot) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        field(json, "schemaVersion", quote(document.schemaVersion()), true);
        field(json, "bdiMetamodelVersion", quote(document.bdiMetamodelVersion()), true);
        field(json, "useFingerprint", quote(document.useFingerprint()), true);
        json.append("  \"bindings\": [");
        if (!document.bindings().isEmpty()) {
            json.append('\n');
        }
        for (int index = 0; index < document.bindings().size(); index++) {
            MappingBinding binding = document.bindings().get(index);
            json.append("    {");
            json.append("\"kind\":").append(quote(binding.kind().name())).append(',');
            json.append("\"source\":")
                    .append(quote(MappingSourceMigration.toPortable(binding, projectRoot)))
                    .append(',');
            json.append("\"target\":").append(quote(binding.target())).append(',');
            json.append("\"expression\":")
                    .append(binding.expression().map(MappingJsonCodec::quote).orElse("null"))
                    .append(',');
            json.append("\"evidence\":[");
            for (int evidenceIndex = 0; evidenceIndex < binding.evidence().size(); evidenceIndex++) {
                if (evidenceIndex > 0) {
                    json.append(',');
                }
                json.append(quote(binding.evidence().get(evidenceIndex)));
            }
            json.append("]}");
            if (index + 1 < document.bindings().size()) {
                json.append(',');
            }
            json.append('\n');
        }
        json.append("  ]\n");
        json.append('}');
        return json.toString();
    }

    static MappingDocument decode(String json, Path projectRoot) {
        Object parsed = parseJson(json);
        Map<String, Object> root = object(parsed, "root");
        String schemaVersion = string(root, "schemaVersion");
        boolean legacy = LEGACY_SCHEMA_VERSION.equals(schemaVersion);
        if (!legacy && !MappingDocument.CURRENT_SCHEMA_VERSION.equals(schemaVersion)) {
            throw new IllegalArgumentException("Unsupported mapping schema version: " + schemaVersion);
        }
        String bdiMetamodelVersion = string(root, "bdiMetamodelVersion");
        String useFingerprint = string(root, "useFingerprint");
        List<Object> rawBindings = array(root, "bindings");
        List<MappingBinding> bindings = new ArrayList<>();
        for (Object rawBinding : rawBindings) {
            Map<String, Object> binding = object(rawBinding, "binding");
            MappingKind kind = enumValue(MappingKind.class, string(binding, "kind"));
            String source = MappingSourceMigration.toRuntime(
                    kind,
                    string(binding, "source"),
                    projectRoot,
                    legacy);
            String target = string(binding, "target");
            Optional<String> expression = optionalString(binding, "expression");
            List<String> evidence = array(binding, "evidence").stream()
                    .map(value -> requireString(value, "evidence item"))
                    .toList();
            bindings.add(new MappingBinding(kind, source, target, expression, evidence));
        }
        return new MappingDocument(
                MappingDocument.CURRENT_SCHEMA_VERSION,
                bdiMetamodelVersion,
                useFingerprint,
                bindings);
    }

    static Object parseJson(String json) {
        return new Parser(json).parse();
    }

    private static void field(StringBuilder json, String name, String value, boolean comma) {
        json.append("  ").append(quote(name)).append(':').append(value);
        if (comma) {
            json.append(',');
        }
        json.append('\n');
    }

    static String quote(String value) {
        StringBuilder quoted = new StringBuilder(value.length() + 2).append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> quoted.append("\\\"");
                case '\\' -> quoted.append("\\\\");
                case '\b' -> quoted.append("\\b");
                case '\f' -> quoted.append("\\f");
                case '\n' -> quoted.append("\\n");
                case '\r' -> quoted.append("\\r");
                case '\t' -> quoted.append("\\t");
                default -> {
                    if (character < 0x20) {
                        quoted.append(String.format("\\u%04x", (int) character));
                    } else {
                        quoted.append(character);
                    }
                }
            }
        }
        return quoted.append('"').toString();
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

    private static Optional<String> optionalString(Map<String, Object> object, String field) {
        Object value = object.get(field);
        return value == null ? Optional.empty() : Optional.of(requireString(value, field));
    }

    private static String requireString(Object value, String field) {
        if (!(value instanceof String text) || text.isBlank()) {
            throw new IllegalArgumentException(field + " must be a non-blank string");
        }
        return text;
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value) {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException("Unknown " + type.getSimpleName() + ": " + value, error);
        }
    }

    private static final class Parser {
        private final String input;
        private int offset;

        private Parser(String input) {
            this.input = input == null ? "" : input;
        }

        private Object parse() {
            Object value = parseValue();
            skipWhitespace();
            if (offset != input.length()) {
                throw error("Unexpected trailing content");
            }
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (offset >= input.length()) {
                throw error("Unexpected end of JSON");
            }
            return switch (input.charAt(offset)) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 'n' -> parseLiteral("null", null);
                case 't' -> parseLiteral("true", Boolean.TRUE);
                case 'f' -> parseLiteral("false", Boolean.FALSE);
                case '-' -> parseNumber();
                default -> {
                    if (!Character.isDigit(input.charAt(offset))) {
                        throw error("Unsupported JSON value");
                    }
                    yield parseNumber();
                }
            };
        }

        private Number parseNumber() {
            int start = offset;
            if (consume('-')) {
                if (offset >= input.length() || !Character.isDigit(input.charAt(offset))) {
                    throw error("Invalid JSON number");
                }
            }
            while (offset < input.length() && Character.isDigit(input.charAt(offset))) {
                offset++;
            }
            boolean decimal = false;
            if (consume('.')) {
                decimal = true;
                if (offset >= input.length() || !Character.isDigit(input.charAt(offset))) {
                    throw error("Invalid JSON number");
                }
                while (offset < input.length() && Character.isDigit(input.charAt(offset))) {
                    offset++;
                }
            }
            if (offset < input.length() && (input.charAt(offset) == 'e' || input.charAt(offset) == 'E')) {
                decimal = true;
                offset++;
                if (offset < input.length() && (input.charAt(offset) == '+' || input.charAt(offset) == '-')) {
                    offset++;
                }
                if (offset >= input.length() || !Character.isDigit(input.charAt(offset))) {
                    throw error("Invalid JSON number");
                }
                while (offset < input.length() && Character.isDigit(input.charAt(offset))) {
                    offset++;
                }
            }
            String value = input.substring(start, offset);
            try {
                if (decimal) {
                    return Double.valueOf(value);
                }
                long parsed = Long.parseLong(value);
                return parsed >= Integer.MIN_VALUE && parsed <= Integer.MAX_VALUE
                        ? Integer.valueOf((int) parsed)
                        : Long.valueOf(parsed);
            } catch (NumberFormatException error) {
                throw error("Invalid JSON number: " + value);
            }
        }

        private Map<String, Object> parseObject() {
            expect('{');
            Map<String, Object> result = new LinkedHashMap<>();
            skipWhitespace();
            if (consume('}')) {
                return result;
            }
            while (true) {
                skipWhitespace();
                if (offset >= input.length() || input.charAt(offset) != '"') {
                    throw error("Object key must be a string");
                }
                String key = parseString();
                skipWhitespace();
                expect(':');
                if (result.containsKey(key)) {
                    throw error("Duplicate object key: " + key);
                }
                result.put(key, parseValue());
                skipWhitespace();
                if (consume('}')) {
                    return result;
                }
                expect(',');
            }
        }

        private List<Object> parseArray() {
            expect('[');
            List<Object> result = new ArrayList<>();
            skipWhitespace();
            if (consume(']')) {
                return result;
            }
            while (true) {
                result.add(parseValue());
                skipWhitespace();
                if (consume(']')) {
                    return result;
                }
                expect(',');
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder result = new StringBuilder();
            while (offset < input.length()) {
                char character = input.charAt(offset++);
                if (character == '"') {
                    return result.toString();
                }
                if (character == '\\') {
                    if (offset >= input.length()) {
                        throw error("Unterminated escape");
                    }
                    char escaped = input.charAt(offset++);
                    switch (escaped) {
                        case '"', '\\', '/' -> result.append(escaped);
                        case 'b' -> result.append('\b');
                        case 'f' -> result.append('\f');
                        case 'n' -> result.append('\n');
                        case 'r' -> result.append('\r');
                        case 't' -> result.append('\t');
                        case 'u' -> result.append(parseUnicode());
                        default -> throw error("Unsupported escape: " + escaped);
                    }
                } else if (character < 0x20) {
                    throw error("Control character in string");
                } else {
                    result.append(character);
                }
            }
            throw error("Unterminated string");
        }

        private char parseUnicode() {
            if (offset + 4 > input.length()) {
                throw error("Incomplete unicode escape");
            }
            String hex = input.substring(offset, offset + 4);
            offset += 4;
            try {
                return (char) Integer.parseInt(hex, 16);
            } catch (NumberFormatException error) {
                throw new IllegalArgumentException("Invalid unicode escape: " + hex, error);
            }
        }

        private Object parseLiteral(String literal, Object value) {
            if (!input.startsWith(literal, offset)) {
                throw error("Invalid literal");
            }
            offset += literal.length();
            return value;
        }

        private void skipWhitespace() {
            while (offset < input.length() && Character.isWhitespace(input.charAt(offset))) {
                offset++;
            }
        }

        private void expect(char expected) {
            if (offset >= input.length() || input.charAt(offset) != expected) {
                throw error("Expected '" + expected + "'");
            }
            offset++;
        }

        private boolean consume(char value) {
            if (offset < input.length() && input.charAt(offset) == value) {
                offset++;
                return true;
            }
            return false;
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " at offset " + offset);
        }
    }
}
