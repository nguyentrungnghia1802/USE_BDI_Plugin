package org.tzi.use.plugins.bdi.release;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class DocumentationContractTest {
    private static final List<String> CANONICAL_DOCUMENTS = List.of(
            "README.md",
            "00_PROJECT_CONTEXT.md",
            "01_PRODUCT_REQUIREMENTS.md",
            "02_SYSTEM_ARCHITECTURE.md",
            "03_DOMAIN_AND_FLOWS.md",
            "04_DATABASE.md",
            "05_API.md",
            "06_CODEBASE_GUIDE.md",
            "07_DEVELOPMENT_AND_TESTING.md",
            "08_DEPLOYMENT_AND_OPERATIONS.md",
            "09_DECISIONS_AND_RISKS.md",
            "10_DOCUMENTATION_SYNC_CHECKLIST.md",
            "11_TENANT_ISOLATION_AND_AUDIT.md",
            "12_REQUIREMENT_TRACEABILITY.md");
    private static final Pattern MARKDOWN_LINK = Pattern.compile("\\[[^]]+\\]\\(([^)]+)\\)");

    @Test
    void keepsCanonicalSpecificationCompleteLinkedAndSourceBacked() throws IOException {
        Path root = repositoryRoot();
        Path project = root.resolve("docs/project");

        for (String document : CANONICAL_DOCUMENTS) {
            Path path = project.resolve(document);
            assertTrue(Files.isRegularFile(path), () -> "Missing canonical document: " + path);
            String content = Files.readString(path);
            assertTrue(!content.isBlank(), () -> "Empty canonical document: " + path);
            assertTrue(content.contains("Last verified: 2026-08-09"),
                    () -> "Missing verification metadata: " + path);
            assertLocalLinksResolve(path, content);
        }

        assertLocalLinksResolve(root.resolve("docs/README.md"), Files.readString(root.resolve("docs/README.md")));

        String context = read(project.resolve("00_PROJECT_CONTEXT.md"));
        assertTrue(context.contains("USE `7.1.1`"));
        assertTrue(context.contains("Plugin | `use-bdi-plugin`, manifest version `0.1.0`"));
        assertTrue(context.contains("jason-interpreter:3.3.0"));

        String requirements = read(project.resolve("01_PRODUCT_REQUIREMENTS.md"));
        assertTrue(requirements.contains("FR-PLG-001"));
        assertTrue(requirements.contains("FR-REP-004"));
        assertTrue(requirements.contains("FR-REL-004"));

        String persistence = read(project.resolve("04_DATABASE.md"));
        assertTrue(persistence.contains("no relational or"));
        assertTrue(persistence.contains("embedded database"));
        assertTrue(persistence.contains("Current schema version: `0.1.0`"));
        assertTrue(persistence.contains("does not reject every unknown object"));

        String api = read(project.resolve("05_API.md"));
        String descriptor = read(root.resolve("use-bdi-plugin/src/main/resources/useplugin.xml"));
        for (String marker : List.of(
                "org.tzi.use.plugins.bdi",
                "Hello BDI Plugin",
                "Import AgentSpeak...")) {
            assertTrue(api.contains(marker), () -> "API document misses plugin contract: " + marker);
            assertTrue(descriptor.contains(marker), () -> "Descriptor misses plugin contract: " + marker);
        }

        String pluginPom = read(root.resolve("use-bdi-plugin/pom.xml"));
        assertTrue(pluginPom.contains("<jason.version>3.3.0</jason.version>"));

        String testing = read(project.resolve("07_DEVELOPMENT_AND_TESTING.md"));
        assertTrue(testing.contains("mvn --batch-mode --no-transfer-progress clean verify"));
        assertTrue(testing.contains("scripts\\auction-evidence.ps1"));
        assertTrue(api.contains("AUCTION_EVIDENCE_OK"));

        String risks = read(project.resolve("09_DECISIONS_AND_RISKS.md"));
        assertTrue(risks.contains("ADR-0019"));
        assertTrue(risks.contains("OD-004"));

        String ignore = read(root.resolve(".gitignore"));
        assertTrue(ignore.lines().anyMatch(line -> line.trim().equals("/docs/agent/")));

        String docsIndex = read(root.resolve("docs/README.md"));
        assertTrue(!docsIndex.contains("](agent/"));
        assertTrue(!docsIndex.contains("](docs/agent/"));
    }

    private static void assertLocalLinksResolve(Path document, String content) {
        Matcher matcher = MARKDOWN_LINK.matcher(content);
        while (matcher.find()) {
            String target = matcher.group(1).trim();
            if (target.startsWith("http://") || target.startsWith("https://")
                    || target.startsWith("mailto:") || target.startsWith("#")) {
                continue;
            }
            if (target.startsWith("<") && target.endsWith(">")) {
                target = target.substring(1, target.length() - 1);
            }
            int fragment = target.indexOf('#');
            if (fragment >= 0) {
                target = target.substring(0, fragment);
            }
            if (target.isBlank()) {
                continue;
            }
            String linkTarget = target;
            Path resolved = document.getParent().resolve(linkTarget).normalize();
            assertTrue(Files.exists(resolved),
                    () -> "Broken local link in " + document + ": " + linkTarget + " -> " + resolved);
        }
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path);
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("docs/project/16_PROJECT_COMPLETION_CHECKLIST.md"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate repository root from the test working directory");
    }
}
