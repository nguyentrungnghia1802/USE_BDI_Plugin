package org.tzi.use.plugins.bdi.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.tzi.use.plugins.bdi.report.CurrentAnalysisReportService;
import org.tzi.use.plugins.bdi.report.ReportFormat;

/** Non-Swing entry point for reproducible BDI/UML/OCL CI analysis. */
public final class BdiQualityGateMain {
    private BdiQualityGateMain() {
    }

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    public static int run(String[] args, PrintStream out, PrintStream err) {
        try {
            CliArguments parsed = CliArguments.parse(args);
            if (parsed.help()) {
                out.println(usage());
                return HeadlessExitCode.CLEAN.code();
            }
            HeadlessAnalysisResult result = new HeadlessAnalysisService().analyze(parsed.request());
            CurrentAnalysisReportService reports = new CurrentAnalysisReportService();
            if (parsed.jsonOutput().isPresent()) {
                reports.export(
                        result.projectName(), result.snapshot(), ReportFormat.JSON,
                        parsed.jsonOutput().orElseThrow(), parsed.overwrite());
            }
            if (parsed.htmlOutput().isPresent()) {
                reports.export(
                        result.projectName(), result.snapshot(), ReportFormat.HTML,
                        parsed.htmlOutput().orElseThrow(), parsed.overwrite());
            }
            HeadlessExitCode exit = result.exitCode();
            out.println("BDI_QUALITY_GATE_RESULT=" + exit.name()
                    + " exit=" + exit.code()
                    + " issues=" + result.snapshot().issueCount()
                    + " mappingHash=" + result.snapshot().mappingHash());
            result.snapshot().issues().forEach(issue -> out.println(
                    issue.ruleId() + " " + issue.certainty() + " " + issue.message()
                            + " evidence=" + String.join("; ", issue.evidence())));
            result.projectDiagnostics().forEach(diagnostic -> out.println(
                    diagnostic.code() + " " + diagnostic.severity() + " " + diagnostic.message()
                            + " source=" + diagnostic.source()));
            return exit.code();
        } catch (HeadlessInputException | IllegalArgumentException error) {
            err.println("BDI_QUALITY_GATE_INPUT_ERROR: " + error.getMessage());
            return HeadlessExitCode.INVALID_INPUT.code();
        } catch (IOException error) {
            err.println("BDI_QUALITY_GATE_INFRASTRUCTURE_ERROR: " + error.getMessage());
            return HeadlessExitCode.INFRASTRUCTURE_FAILURE.code();
        } catch (RuntimeException error) {
            err.println("BDI_QUALITY_GATE_INFRASTRUCTURE_ERROR: "
                    + error.getClass().getSimpleName() + ": " + error.getMessage());
            return HeadlessExitCode.INFRASTRUCTURE_FAILURE.code();
        }
    }

    public static String usage() {
        return """
                USE BDI headless quality gate
                  --use <model.use>          required
                  --asl <agent.asl>          direct AgentSpeak input, repeatable
                  --jcm <project.jcm>        JaCaMo project input; mutually exclusive with --asl
                  --mapping <file>           optional .bdimap.json
                  --rules <file>             optional rules.json
                  --suppressions <file>      optional suppressions.json
                  --json <report.json>       at least one JSON/HTML output is required
                  --html <report.html>
                  --timestamp <ISO-8601>     optional, defaults to 1970-01-01T00:00:00Z
                  --project-name <name>      optional, defaults to USE filename
                  --overwrite                allow replacing existing report files
                  --help
                Exit: 0 clean, 1 confirmed findings, 2 potential/unknown-only,
                      3 invalid input/config, 4 infrastructure/output failure.
                """;
    }

    private record CliArguments(
            boolean help,
            HeadlessAnalysisRequest request,
            Optional<Path> jsonOutput,
            Optional<Path> htmlOutput,
            boolean overwrite) {

        private static CliArguments parse(String[] args) {
            if (args == null) {
                throw new IllegalArgumentException("Arguments must not be null");
            }
            Path use = null;
            List<Path> asl = new ArrayList<>();
            Path jcm = null;
            Path mapping = null;
            Path rules = null;
            Path suppressions = null;
            Path json = null;
            Path html = null;
            Instant timestamp = Instant.EPOCH;
            String projectName = null;
            boolean overwrite = false;
            boolean help = false;

            for (int index = 0; index < args.length; index++) {
                String option = args[index];
                switch (option) {
                    case "--help", "-h" -> help = true;
                    case "--overwrite" -> overwrite = true;
                    case "--use" -> use = Path.of(value(args, ++index, option));
                    case "--asl" -> asl.add(Path.of(value(args, ++index, option)));
                    case "--jcm" -> jcm = Path.of(value(args, ++index, option));
                    case "--mapping" -> mapping = Path.of(value(args, ++index, option));
                    case "--rules" -> rules = Path.of(value(args, ++index, option));
                    case "--suppressions" -> suppressions = Path.of(value(args, ++index, option));
                    case "--json" -> json = Path.of(value(args, ++index, option));
                    case "--html" -> html = Path.of(value(args, ++index, option));
                    case "--timestamp" -> timestamp = timestamp(value(args, ++index, option));
                    case "--project-name" -> projectName = value(args, ++index, option);
                    default -> throw new IllegalArgumentException("Unknown option: " + option);
                }
            }
            if (help) {
                return new CliArguments(true, null, Optional.empty(), Optional.empty(), false);
            }
            if (use == null) {
                throw new IllegalArgumentException("Missing required --use file");
            }
            if (json == null && html == null) {
                throw new IllegalArgumentException("At least one --json or --html output is required");
            }
            return new CliArguments(
                    false,
                    new HeadlessAnalysisRequest(
                            use,
                            asl,
                            Optional.ofNullable(jcm),
                            Optional.ofNullable(mapping),
                            Optional.ofNullable(rules),
                            Optional.ofNullable(suppressions),
                            timestamp,
                            Optional.ofNullable(projectName)),
                    Optional.ofNullable(json),
                    Optional.ofNullable(html),
                    overwrite);
        }

        private static String value(String[] args, int index, String option) {
            if (index >= args.length || args[index].startsWith("--")) {
                throw new IllegalArgumentException("Missing value for " + option);
            }
            return args[index];
        }

        private static Instant timestamp(String value) {
            try {
                return Instant.parse(value);
            } catch (DateTimeParseException error) {
                throw new IllegalArgumentException("Invalid --timestamp value: " + value, error);
            }
        }
    }
}
