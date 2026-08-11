package org.tzi.use.plugins.bdi.evaluation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tzi.use.plugins.bdi.validation.IssueCertainty;

/** Dependency-free deterministic codec for the reviewed evaluation manifest. */
public final class EvaluationManifestCodec {
    private EvaluationManifestCodec() {
    }

    public static EvaluationManifest load(Path file) throws IOException {
        return decode(Files.readString(file.toAbsolutePath().normalize(), StandardCharsets.UTF_8));
    }

    public static void save(Path file, EvaluationManifest manifest) throws IOException {
        Path target = file.toAbsolutePath().normalize();
        if (target.getParent() == null) {
            throw new IOException("Manifest path has no parent directory: " + target);
        }
        Files.createDirectories(target.getParent());
        Files.writeString(target, encode(manifest), StandardCharsets.UTF_8);
    }

    public static String encode(EvaluationManifest manifest) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        field(json, "schemaVersion", quote(manifest.schemaVersion()), true);
        field(json, "caseStudy", quote(manifest.caseStudy()), true);
        field(json, "toolVersion", quote(manifest.toolVersion()), true);
        field(json, "useVersion", quote(manifest.useVersion()), true);
        field(json, "configurationProfile", quote(manifest.configurationProfile()), true);
        arrayField(json, "excludedLayers", manifest.excludedLayers(), true, 1);
        json.append("  \"cases\": [\n");
        for (int index = 0; index < manifest.cases().size(); index++) {
            EvaluationManifest.EvaluationCase evaluationCase = manifest.cases().get(index);
            json.append("    {");
            fieldInline(json, "id", evaluationCase.id(), true);
            fieldInline(json, "family", evaluationCase.family(), true);
            fieldInline(json, "layer", evaluationCase.layer(), true);
            fieldInline(json, "useFile", evaluationCase.useFile(), true);
            arrayInline(json, "aslFiles", evaluationCase.aslFiles(), true);
            nullableFieldInline(json, "jcmFile", evaluationCase.jcmFile().orElse(null), true);
            nullableFieldInline(json, "mappingFile", evaluationCase.mappingFile().orElse(null), true);
            arrayInline(json, "requiredRuleIds", evaluationCase.requiredRuleIds(), true);
            arrayInline(json, "forbiddenRuleIds", evaluationCase.forbiddenRuleIds(), true);
            json.append("\"expectedCertainties\":{");
            List<String> certaintyKeys = evaluationCase.expectedCertainties().keySet().stream().sorted().toList();
            for (int certaintyIndex = 0; certaintyIndex < certaintyKeys.size(); certaintyIndex++) {
                if (certaintyIndex > 0) {
                    json.append(',');
                }
                String ruleId = certaintyKeys.get(certaintyIndex);
                json.append(quote(ruleId)).append(':')
                        .append(quote(evaluationCase.expectedCertainties().get(ruleId).name()));
            }
            json.append("},");
            arrayInline(json, "evidenceAnchors", evaluationCase.evidenceAnchors(), true);
            arrayInline(json, "allowedUnsupportedLayers", evaluationCase.allowedUnsupportedLayers(), true);
            nullableFieldInline(json, "stateFixture", evaluationCase.stateFixture().orElse(null), true);
            json.append("\"evidenceTokens\":{");
            List<String> tokenKeys = evaluationCase.evidenceTokens().keySet().stream().sorted().toList();
            for (int tokenIndex = 0; tokenIndex < tokenKeys.size(); tokenIndex++) {
                if (tokenIndex > 0) {
                    json.append(',');
                }
                String ruleId = tokenKeys.get(tokenIndex);
                json.append(quote(ruleId)).append(':').append(quote(evaluationCase.evidenceTokens().get(ruleId)));
            }
            json.append("},");
            json.append("\"timeoutSeconds\":").append(evaluationCase.timeout().toSeconds());
            json.append("}");
            if (index + 1 < manifest.cases().size()) {
                json.append(',');
            }
            json.append('\n');
        }
        json.append("  ]\n}\n");
        return json.toString();
    }

    public static EvaluationManifest decode(String json) {
        Map<String, Object> root = object(new Parser(json).parse(), "root");
        keys(root, "root", Set.of(
                "schemaVersion", "caseStudy", "toolVersion", "useVersion", "configurationProfile",
                "excludedLayers", "cases"));
        List<EvaluationManifest.EvaluationCase> cases = new ArrayList<>();
        for (Object value : arrayValue(root.get("cases"), "cases")) {
            Map<String, Object> raw = object(value, "case");
            keys(raw, "case", Set.of(
                    "id", "family", "layer", "useFile", "aslFiles", "jcmFile", "mappingFile",
                    "requiredRuleIds", "forbiddenRuleIds", "expectedCertainties", "evidenceAnchors",
                    "allowedUnsupportedLayers", "stateFixture", "evidenceTokens", "timeoutSeconds"));
            Map<String, IssueCertainty> certainties = new LinkedHashMap<>();
            Map<String, Object> rawCertainties = object(raw.get("expectedCertainties"), "expectedCertainties");
            rawCertainties.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry ->
                    certainties.put(entry.getKey(), enumValue(IssueCertainty.class, string(entry.getValue(), entry.getKey()))));
            Map<String, String> evidenceTokens = new LinkedHashMap<>();
            object(raw.get("evidenceTokens"), "evidenceTokens").entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> evidenceTokens.put(entry.getKey(), string(entry.getValue(), entry.getKey())));
            cases.add(new EvaluationManifest.EvaluationCase(
                    string(raw.get("id"), "case.id"),
                    string(raw.get("family"), "case.family"),
                    string(raw.get("layer"), "case.layer"),
                    string(raw.get("useFile"), "case.useFile"),
                    strings(raw.get("aslFiles"), "case.aslFiles"),
                    optionalString(raw.get("jcmFile"), "case.jcmFile"),
                    optionalString(raw.get("mappingFile"), "case.mappingFile"),
                    strings(raw.get("requiredRuleIds"), "case.requiredRuleIds"),
                    strings(raw.get("forbiddenRuleIds"), "case.forbiddenRuleIds"),
                    certainties,
                    strings(raw.get("evidenceAnchors"), "case.evidenceAnchors"),
                    strings(raw.get("allowedUnsupportedLayers"), "case.allowedUnsupportedLayers"),
                    Duration.ofSeconds(integer(raw.get("timeoutSeconds"), "case.timeoutSeconds")),
                    optionalString(raw.get("stateFixture"), "case.stateFixture"),
                    evidenceTokens));
        }
        return new EvaluationManifest(
                string(root.get("schemaVersion"), "schemaVersion"),
                string(root.get("caseStudy"), "caseStudy"),
                string(root.get("toolVersion"), "toolVersion"),
                string(root.get("useVersion"), "useVersion"),
                string(root.get("configurationProfile"), "configurationProfile"),
                strings(root.get("excludedLayers"), "excludedLayers"),
                cases);
    }

    private static void field(StringBuilder json, String name, String value, boolean comma) {
        json.append("  ").append(quote(name)).append(':').append(value);
        if (comma) {
            json.append(',');
        }
        json.append('\n');
    }

    private static void arrayField(StringBuilder json, String name, List<String> values, boolean comma, int indent) {
        json.append("  ").append(quote(name)).append(':');
        array(json, values, indent);
        if (comma) {
            json.append(',');
        }
        json.append('\n');
    }

    private static void fieldInline(StringBuilder json, String name, String value, boolean comma) {
        json.append(quote(name)).append(':').append(quote(value));
        if (comma) {
            json.append(',');
        }
    }

    private static void nullableFieldInline(StringBuilder json, String name, String value, boolean comma) {
        json.append(quote(name)).append(':').append(value == null ? "null" : quote(value));
        if (comma) {
            json.append(',');
        }
    }

    private static void arrayInline(StringBuilder json, String name, List<String> values, boolean comma) {
        json.append(quote(name)).append(':');
        array(json, values, 0);
        if (comma) {
            json.append(',');
        }
    }

    private static void array(StringBuilder json, List<String> values, int indent) {
        json.append('[');
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                json.append(',');
            }
            json.append(quote(values.get(index)));
        }
        json.append(']');
    }

    private static String quote(String value) {
        StringBuilder quoted = new StringBuilder(value.length() + 2).append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> quoted.append("\\\"");
                case '\\' -> quoted.append("\\\\");
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

    private static void keys(Map<String, Object> object, String label, Set<String> expected) {
        if (!object.keySet().equals(expected)) {
            throw new IllegalArgumentException(label + " fields must be exactly " + expected + "; got " + object.keySet());
        }
    }

    private static List<Object> arrayValue(Object value, String label) {
        if (!(value instanceof List<?> raw)) {
            throw new IllegalArgumentException(label + " must be an array");
        }
        return new ArrayList<>(raw);
    }

    private static List<String> strings(Object value, String label) {
        return arrayValue(value, label).stream().map(item -> string(item, label + " item")).toList();
    }

    private static String string(Object value, String label) {
        if (!(value instanceof String text) || text.isBlank()) {
            throw new IllegalArgumentException(label + " must be a non-blank string");
        }
        return text;
    }

    private static java.util.Optional<String> optionalString(Object value, String label) {
        return value == null ? java.util.Optional.empty() : java.util.Optional.of(string(value, label));
    }

    private static int integer(Object value, String label) {
        if (!(value instanceof Number number) || number.doubleValue() != number.intValue()) {
            throw new IllegalArgumentException(label + " must be an integer");
        }
        return number.intValue();
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
            Object value = value();
            whitespace();
            if (offset != input.length()) {
                throw error("Unexpected trailing content");
            }
            return value;
        }

        private Object value() {
            whitespace();
            if (offset >= input.length()) {
                throw error("Unexpected end of JSON");
            }
            return switch (input.charAt(offset)) {
                case '{' -> objectValue();
                case '[' -> arrayValue();
                case '"' -> stringValue();
                case 'n' -> literal("null", null);
                case 't' -> literal("true", Boolean.TRUE);
                case 'f' -> literal("false", Boolean.FALSE);
                default -> numberValue();
            };
        }

        private Map<String, Object> objectValue() {
            expect('{');
            Map<String, Object> result = new LinkedHashMap<>();
            whitespace();
            if (consume('}')) {
                return result;
            }
            while (true) {
                whitespace();
                if (offset >= input.length() || input.charAt(offset) != '"') {
                    throw error("Object key must be a string");
                }
                String key = stringValue();
                whitespace();
                expect(':');
                if (result.containsKey(key)) {
                    throw error("Duplicate object key: " + key);
                }
                result.put(key, value());
                whitespace();
                if (consume('}')) {
                    return result;
                }
                expect(',');
            }
        }

        private List<Object> arrayValue() {
            expect('[');
            List<Object> result = new ArrayList<>();
            whitespace();
            if (consume(']')) {
                return result;
            }
            while (true) {
                result.add(value());
                whitespace();
                if (consume(']')) {
                    return result;
                }
                expect(',');
            }
        }

        private String stringValue() {
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
                        case 'u' -> result.append(unicode());
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

        private char unicode() {
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

        private Number numberValue() {
            int start = offset;
            if (consume('-')) {
                // Sign is consumed before the digit check below.
            }
            int integerStart = offset;
            while (offset < input.length() && Character.isDigit(input.charAt(offset))) {
                offset++;
            }
            if (integerStart == offset) {
                throw error("Invalid JSON number");
            }
            boolean decimal = false;
            if (consume('.')) {
                decimal = true;
                int fractionStart = offset;
                while (offset < input.length() && Character.isDigit(input.charAt(offset))) {
                    offset++;
                }
                if (fractionStart == offset) {
                    throw error("Invalid JSON number fraction");
                }
            }
            if (offset < input.length() && (input.charAt(offset) == 'e' || input.charAt(offset) == 'E')) {
                decimal = true;
                offset++;
                if (offset < input.length() && (input.charAt(offset) == '+' || input.charAt(offset) == '-')) {
                    offset++;
                }
                int exponentStart = offset;
                while (offset < input.length() && Character.isDigit(input.charAt(offset))) {
                    offset++;
                }
                if (exponentStart == offset) {
                    throw error("Invalid JSON number exponent");
                }
            }
            String text = input.substring(start, offset);
            try {
                return decimal ? Double.valueOf(text) : Long.valueOf(text);
            } catch (NumberFormatException error) {
                throw new IllegalArgumentException("Invalid JSON number: " + text, error);
            }
        }

        private Object literal(String literal, Object value) {
            if (!input.startsWith(literal, offset)) {
                throw error("Invalid literal");
            }
            offset += literal.length();
            return value;
        }

        private void whitespace() {
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
