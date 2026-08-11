package org.tzi.use.plugins.bdi.evaluation;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;

/** CLI entry point for the reviewed Auction evaluation corpus. */
public final class EvaluationRunnerMain {
    private EvaluationRunnerMain() {
    }

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    public static int run(String[] args, PrintStream out, PrintStream err) {
        try {
            Arguments arguments = Arguments.parse(args);
            if (arguments.help()) {
                out.println(usage());
                return 0;
            }
            EvaluationManifest manifest = EvaluationManifestCodec.load(arguments.manifest());
            EvaluationRunResult result = new EvaluationRunner().run(
                    manifest, arguments.sourceRoot(), arguments.timestamp());
            EvaluationReportWriter.write(arguments.outputDirectory(), result);
            out.println("BDI_EVALUATION_RESULT=" + result.metrics());
            return exitCode(result);
        } catch (IllegalArgumentException error) {
            err.println("BDI_EVALUATION_INPUT_ERROR: " + error.getMessage());
            return 3;
        } catch (IOException error) {
            err.println("BDI_EVALUATION_INFRASTRUCTURE_ERROR: " + error.getMessage());
            return 4;
        } catch (RuntimeException error) {
            err.println("BDI_EVALUATION_INFRASTRUCTURE_ERROR: "
                    + error.getClass().getSimpleName() + ": " + error.getMessage());
            return 4;
        }
    }

    public static String usage() {
        return """
                USE BDI evaluation runner
                  --manifest <evaluation.json>  required reviewed manifest
                  --root <checkout>             required manifest input root
                  --out <directory>             required report directory
                  --timestamp <ISO-8601>        optional, defaults to epoch
                  --help
                Exit: 0 all declared cases PASS/DETECTED, 1 semantic miss/unexpected,
                      2 UNKNOWN/UNSUPPORTED, 3 invalid manifest/input, 4 infrastructure error.
                """;
    }

    private static int exitCode(EvaluationRunResult result) {
        EvaluationRunResult.EvaluationMetrics metrics = result.metrics();
        if (metrics.invalidInput() > 0) {
            return 3;
        }
        if (metrics.timeouts() > 0 || metrics.executionErrors() > 0) {
            return 4;
        }
        if (metrics.missed() > 0 || metrics.unexpected() > 0) {
            return 1;
        }
        if (metrics.unknown() > 0 || metrics.unsupported() > 0) {
            return 2;
        }
        return 0;
    }

    private record Arguments(boolean help, Path manifest, Path sourceRoot, Path outputDirectory, Instant timestamp) {
        private static Arguments parse(String[] args) {
            if (args == null) {
                throw new IllegalArgumentException("Arguments must not be null");
            }
            Path manifest = null;
            Path root = null;
            Path output = null;
            Instant timestamp = Instant.EPOCH;
            boolean help = false;
            for (int index = 0; index < args.length; index++) {
                String option = args[index];
                switch (option) {
                    case "--help", "-h" -> help = true;
                    case "--manifest" -> manifest = Path.of(value(args, ++index, option));
                    case "--root" -> root = Path.of(value(args, ++index, option));
                    case "--out" -> output = Path.of(value(args, ++index, option));
                    case "--timestamp" -> timestamp = timestamp(value(args, ++index, option));
                    default -> throw new IllegalArgumentException("Unknown option: " + option);
                }
            }
            if (help) {
                return new Arguments(true, null, null, null, timestamp);
            }
            if (manifest == null || root == null || output == null) {
                throw new IllegalArgumentException("--manifest, --root, and --out are required");
            }
            return new Arguments(false, manifest, root, output, timestamp);
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
