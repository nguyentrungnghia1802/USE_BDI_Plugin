package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Path;

public final class PackagedParserSmoke {
    private PackagedParserSmoke() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Expected the path to a valid AgentSpeak fixture.");
            System.exit(2);
        }

        AslParseSummary result = new JasonAslParserAdapter().parse(Path.of(args[0]));
        if (!"3.3.0".equals(result.parserVersion())
                || result.beliefCount() != 1
                || result.goalCount() != 1
                || result.planCount() != 1) {
            throw new IllegalStateException("Unexpected packaged parser result: " + result);
        }

        System.out.println("PARSER_SMOKE_OK: Jason 3.3.0 parsed 1 belief, 1 goal, 1 plan");
    }
}
