package org.tzi.use.plugins.bdi.evaluation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import org.tzi.use.plugins.bdi.validation.IssueCertainty;

/** Writes stable machine and reviewer-facing summaries without temporary paths. */
public final class EvaluationReportWriter {
    private EvaluationReportWriter() {
    }

    public static void write(Path outputDirectory, EvaluationRunResult result) throws IOException {
        Path directory = outputDirectory.toAbsolutePath().normalize();
        Files.createDirectories(directory);
        atomicWrite(directory.resolve("evaluation-results.json"), json(result));
        atomicWrite(directory.resolve("evaluation-results.csv"), csv(result));
        atomicWrite(directory.resolve("evaluation-results.html"), html(result));
    }

    static String json(EvaluationRunResult result) {
        StringBuilder json = new StringBuilder("{\n");
        field(json, "schemaVersion", result.schemaVersion(), true);
        field(json, "caseStudy", result.caseStudy(), true);
        field(json, "toolVersion", result.toolVersion(), true);
        field(json, "useVersion", result.useVersion(), true);
        field(json, "configurationProfile", result.configurationProfile(), true);
        field(json, "timestamp", result.timestamp().toString(), true);
        field(json, "manifestHash", result.manifestHash(), true);
        field(json, "corpusHash", result.corpusHash(), true);
        field(json, "configurationHash", result.configurationHash(), true);
        json.append("  \"metrics\":").append(metrics(result.metrics())).append(",\n");
        json.append("  \"cases\":[\n");
        for (int index = 0; index < result.cases().size(); index++) {
            EvaluationRunResult.EvaluationCaseResult evaluationCase = result.cases().get(index);
            json.append("    {");
            fieldInline(json, "id", evaluationCase.id(), true);
            fieldInline(json, "family", evaluationCase.family(), true);
            fieldInline(json, "layer", evaluationCase.layer(), true);
            fieldInline(json, "status", evaluationCase.status().name(), true);
            numberInline(json, "exitCode", evaluationCase.exitCode(), true);
            arrayInline(json, "requiredRuleIds", evaluationCase.requiredRuleIds(), true);
            arrayInline(json, "forbiddenRuleIds", evaluationCase.forbiddenRuleIds(), true);
            arrayInline(json, "observedRuleIds", evaluationCase.observedRuleIds(), true);
            json.append("\"observedCertainties\":").append(certainties(evaluationCase.observedCertainties())).append(',');
            arrayInline(json, "missingRuleIds", evaluationCase.missingRuleIds(), true);
            arrayInline(json, "violatedForbiddenRuleIds", evaluationCase.violatedForbiddenRuleIds(), true);
            arrayInline(json, "unexpectedRuleIds", evaluationCase.unexpectedRuleIds(), true);
            arrayInline(json, "evidenceAnchors", evaluationCase.evidenceAnchors(), true);
            arrayInline(json, "traceLinks", evaluationCase.traceLinks(), true);
            arrayInline(json, "observedEvidence", evaluationCase.observedEvidence(), true);
            fieldInline(json, "inputHash", evaluationCase.inputHash(), true);
            fieldInline(json, "diagnostic", evaluationCase.diagnostic(), false);
            json.append('}');
            if (index + 1 < result.cases().size()) {
                json.append(',');
            }
            json.append('\n');
        }
        json.append("  ]\n}\n");
        return json.toString();
    }

    static String csv(EvaluationRunResult result) {
        StringBuilder csv = new StringBuilder();
        csv.append("caseId,family,layer,status,exitCode,inputHash,missingCount,violatedForbiddenCount,unexpectedCount,diagnostic\n");
        for (EvaluationRunResult.EvaluationCaseResult evaluationCase : result.cases()) {
            csv.append(csv(evaluationCase.id())).append(',')
                    .append(csv(evaluationCase.family())).append(',')
                    .append(csv(evaluationCase.layer())).append(',')
                    .append(evaluationCase.status()).append(',')
                    .append(evaluationCase.exitCode()).append(',')
                    .append(evaluationCase.inputHash()).append(',')
                    .append(evaluationCase.missingRuleIds().size()).append(',')
                    .append(evaluationCase.violatedForbiddenRuleIds().size()).append(',')
                    .append(evaluationCase.unexpectedRuleIds().size()).append(',')
                    .append(csv(evaluationCase.diagnostic())).append('\n');
        }
        return csv.toString();
    }

    static String html(EvaluationRunResult result) {
        StringBuilder html = new StringBuilder("<!doctype html>\n<html lang=\"en\"><head><meta charset=\"utf-8\">");
        html.append("<title>BDI Evaluation Evidence</title></head><body>");
        html.append("<h1>BDI Evaluation Evidence</h1><table border=\"1\"><tbody>");
        row(html, "Case study", result.caseStudy());
        row(html, "Tool version", result.toolVersion());
        row(html, "USE version", result.useVersion());
        row(html, "Configuration", result.configurationProfile());
        row(html, "Timestamp", result.timestamp().toString());
        row(html, "Manifest hash", result.manifestHash());
        row(html, "Corpus hash", result.corpusHash());
        row(html, "Configuration hash", result.configurationHash());
        html.append("</tbody></table><h2>Metrics</h2><table border=\"1\"><tbody>");
        row(html, "Total cases", Integer.toString(result.metrics().totalCases()));
        row(html, "PASS", Integer.toString(result.metrics().passed()));
        row(html, "DETECTED", Integer.toString(result.metrics().detected()));
        row(html, "MISSED", Integer.toString(result.metrics().missed()));
        row(html, "UNEXPECTED", Integer.toString(result.metrics().unexpected()));
        row(html, "UNKNOWN", Integer.toString(result.metrics().unknown()));
        row(html, "UNSUPPORTED", Integer.toString(result.metrics().unsupported()));
        row(html, "INVALID_INPUT", Integer.toString(result.metrics().invalidInput()));
        row(html, "TIMEOUT", Integer.toString(result.metrics().timeouts()));
        row(html, "EXECUTION_ERROR", Integer.toString(result.metrics().executionErrors()));
        html.append("</tbody></table><h2>Cases</h2><table border=\"1\"><thead><tr>");
        for (String heading : List.of("ID", "Family", "Layer", "Status", "Observed rules", "Diagnostic")) {
            html.append("<th>").append(escape(heading)).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        for (EvaluationRunResult.EvaluationCaseResult evaluationCase : result.cases()) {
            html.append("<tr><td>").append(escape(evaluationCase.id())).append("</td><td>")
                    .append(escape(evaluationCase.family())).append("</td><td>")
                    .append(escape(evaluationCase.layer())).append("</td><td>")
                    .append(escape(evaluationCase.status().name())).append("</td><td>")
                    .append(escape(String.join(", ", evaluationCase.observedRuleIds()))).append("</td><td>")
                    .append(escape(evaluationCase.diagnostic())).append("</td></tr>");
        }
        return html.append("</tbody></table></body></html>\n").toString();
    }

    private static String metrics(EvaluationRunResult.EvaluationMetrics metrics) {
        return "{\"totalCases\":" + metrics.totalCases()
                + ",\"passed\":" + metrics.passed()
                + ",\"detected\":" + metrics.detected()
                + ",\"missed\":" + metrics.missed()
                + ",\"unexpected\":" + metrics.unexpected()
                + ",\"unknown\":" + metrics.unknown()
                + ",\"unsupported\":" + metrics.unsupported()
                + ",\"invalidInput\":" + metrics.invalidInput()
                + ",\"timeouts\":" + metrics.timeouts()
                + ",\"executionErrors\":" + metrics.executionErrors() + '}';
    }

    private static String certainties(Map<String, IssueCertainty> certainties) {
        StringBuilder json = new StringBuilder("{");
        List<String> keys = certainties.keySet().stream().sorted().toList();
        for (int index = 0; index < keys.size(); index++) {
            if (index > 0) {
                json.append(',');
            }
            String key = keys.get(index);
            json.append(quote(key)).append(':').append(quote(certainties.get(key).name()));
        }
        return json.append('}').toString();
    }

    private static void field(StringBuilder json, String name, String value, boolean comma) {
        json.append("  ").append(quote(name)).append(':').append(quote(value));
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

    private static void numberInline(StringBuilder json, String name, int value, boolean comma) {
        json.append(quote(name)).append(':').append(value);
        if (comma) {
            json.append(',');
        }
    }

    private static void arrayInline(StringBuilder json, String name, List<String> values, boolean comma) {
        json.append(quote(name)).append(':').append('[');
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                json.append(',');
            }
            json.append(quote(values.get(index)));
        }
        json.append(']');
        if (comma) {
            json.append(',');
        }
    }

    private static void row(StringBuilder html, String key, String value) {
        html.append("<tr><th>").append(escape(key)).append("</th><td>").append(escape(value)).append("</td></tr>");
    }

    private static String csv(String value) {
        String safe = value == null ? "" : value;
        return '"' + safe.replace("\"", "\"\"") + '"';
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

    private static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static void atomicWrite(Path target, String content) throws IOException {
        Path temporary = Files.createTempFile(target.getParent(), ".evaluation-", ".tmp");
        try {
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException error) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
