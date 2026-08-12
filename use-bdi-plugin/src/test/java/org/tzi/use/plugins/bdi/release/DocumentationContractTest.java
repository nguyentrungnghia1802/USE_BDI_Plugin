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
            "04_SYSTEM_ARCHITECTURE.md",
            "08_CONSISTENCY_RULE_CATALOG.md",
            "10_PLUGIN_TECHNICAL_DESIGN.md",
            "16_PROJECT_COMPLETION_CHECKLIST.md",
            "DECISION_LOG.md",
            "12_REQUIREMENT_TRACEABILITY.md");
    private static final List<String> REMOVED_REDUNDANT_DOCUMENTS = List.of(
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
            "14_ROADMAP_TO_DECEMBER_2026.md",
            "PR_DESCRIPTION.md");
    private static final Pattern MARKDOWN_LINK = Pattern.compile("\\[[^]]+\\]\\(([^)]+)\\)");
    private static final String VERIFICATION_MARKER =
            "Verification: source-backed; see Git history and DocumentationContractTest";

    @Test
    void keepsCanonicalSpecificationCompleteLinkedAndSourceBacked() throws IOException {
        Path root = repositoryRoot();
        Path project = root.resolve("docs/project");

        for (String document : CANONICAL_DOCUMENTS) {
            Path path = project.resolve(document);
            assertTrue(Files.isRegularFile(path), () -> "Missing canonical document: " + path);
            String content = Files.readString(path);
            assertTrue(!content.isBlank(), () -> "Empty canonical document: " + path);
            assertTrue(content.contains(VERIFICATION_MARKER),
                    () -> "Missing verification metadata: " + path);
            assertTrue(!content.contains("Last verified:"),
                    () -> "Canonical document duplicates a volatile verification date: " + path);
            assertTrue(!content.contains("Code baseline:"),
                    () -> "Canonical document duplicates a volatile commit baseline: " + path);
            assertLocalLinksResolve(path, content);
        }
        for (String document : REMOVED_REDUNDANT_DOCUMENTS) {
            assertTrue(Files.notExists(project.resolve(document)),
                    () -> "Redundant project document was reintroduced: " + document);
        }

        assertLocalLinksResolve(root.resolve("docs/README.md"), Files.readString(root.resolve("docs/README.md")));
        Path demoGuide = root.resolve("docs/guide/guide.md");
        assertTrue(Files.isRegularFile(demoGuide));
        assertTrue(!Files.readString(demoGuide).isBlank());
        assertLocalLinksResolve(demoGuide, Files.readString(demoGuide));
        Path demoRoot = root.resolve("use-bdi-plugin/demo");
        for (String demo : List.of("auction", "smart-queue")) {
            Path directory = demoRoot.resolve(demo);
            assertTrue(Files.isDirectory(directory), () -> "Missing canonical demo: " + directory);
            assertTrue(Files.isRegularFile(directory.resolve("README.md")));
            List<String> requiredFiles = demo.equals("auction")
                    ? List.of("Auction.use", "Auction.cmd", "auctioneer.asl", "bidder.asl",
                            "auction.jcm", "auction-organization.xml", "Auction.bdimap.json")
                    : List.of("SmartQueue.use", "SmartQueue.cmd", "smart_queue_manager.asl");
            for (String required : requiredFiles) {
                assertTrue(Files.isRegularFile(directory.resolve(required)),
                        () -> "Missing file in canonical demo: " + directory.resolve(required));
            }
        }

        String context = read(project.resolve("00_PROJECT_CONTEXT.md"));
        assertTrue(context.contains("USE `7.1.1`"));
        assertTrue(context.contains("Plugin | `use-bdi-plugin`, manifest version `0.1.0`"));
        assertTrue(context.contains("jason-interpreter:3.3.0"));
        assertTrue(context.contains("org.jacamo:jacamo:1.3.0"));
        assertTrue(context.contains("org.jacamo:cartago:3.1"));
        assertTrue(context.contains("org.jacamo:moise:1.1"));
        assertTrue(context.contains("Persisted typed mappings"));

        String requirements = read(project.resolve("01_PRODUCT_REQUIREMENTS.md"));
        assertTrue(requirements.contains("FR-PLG-001"));
        assertTrue(requirements.contains("FR-PLG-006"));
        assertTrue(requirements.contains("FR-REP-004"));
        assertTrue(requirements.contains("FR-REL-004"));

        String design = read(project.resolve("10_PLUGIN_TECHNICAL_DESIGN.md"));
        assertTrue(design.contains("no database/network API"));
        assertTrue(design.contains("Current mapping and suppression schema: `0.2.0`"));
        assertTrue(design.contains("Rule configuration remains"));
        assertTrue(design.contains("does not reject every unknown object field"));
        assertTrue(design.contains("BdiFileChooserSupport"));
        assertTrue(design.contains("Options.setLastDirectory"));

        String descriptor = read(root.resolve("use-bdi-plugin/src/main/resources/useplugin.xml"));
        for (String marker : List.of(
                "org.tzi.use.plugins.bdi",
                "Hello BDI Plugin",
                "Import AgentSpeak...")) {
            assertTrue(design.contains(marker), () -> "Technical design misses plugin contract: " + marker);
            assertTrue(descriptor.contains(marker), () -> "Descriptor misses plugin contract: " + marker);
        }

        String pluginPom = read(root.resolve("use-bdi-plugin/pom.xml"));
        assertTrue(pluginPom.contains("<jason.version>3.3.0</jason.version>"));
        assertTrue(pluginPom.contains("<jacamo.version>1.3.0</jacamo.version>"));
        assertTrue(pluginPom.contains("<cartago.version>3.1</cartago.version>"));
        assertTrue(pluginPom.contains("<moise.version>1.1</moise.version>"));

        assertTrue(design.contains("mvn --batch-mode --no-transfer-progress clean verify"));
        assertTrue(design.contains("scripts/auction-evidence.ps1"));
        assertTrue(design.contains("AUCTION_EVIDENCE_OK"));

        String decisions = read(project.resolve("DECISION_LOG.md"));
        assertTrue(decisions.contains("ADR-0019"));
        assertTrue(decisions.contains("OD-004"));

        String architecture = read(project.resolve("04_SYSTEM_ARCHITECTURE.md"));
        assertTrue(architecture.contains("`JaCaMoProjectParserAdapter` uses the official JaCaMo 1.3.0 parser"));
        assertTrue(architecture.contains("`CArtAgOArtifactAdapter`"));
        assertTrue(architecture.contains("`MoiseOrganizationParserAdapter`"));
        String ideas = read(root.resolve("docs/idea/idea.md"));
        assertLocalLinksResolve(root.resolve("docs/idea/idea.md"), ideas);
        for (int idea = 1; idea <= 8; idea++) {
            String expectedHeading = "## Idea " + idea + " -";
            assertTrue(ideas.contains(expectedHeading),
                    () -> "Missing prioritized development heading: " + expectedHeading);
        }
        assertTrue(ideas.contains("CArtAgO"));
        assertTrue(ideas.contains("Moise"));
        assertTrue(ideas.contains("T11-T16 are complete"));

        for (String guide : List.of("PLUGIN_INSTALL_GUIDE.md", "USER_GUIDE.md", "DEVELOPER_GUIDE.md")) {
            String content = read(project.resolve(guide));
            assertTrue(content.contains("$javaExecutable"),
                    () -> "Guide does not resolve the Java executable safely: " + guide);
            assertTrue(!content.matches("(?s).*(?m)^java -(jar|cp).*"),
                    () -> "Guide invokes a potentially stale PATH Java directly: " + guide);
        }

        String ignore = read(root.resolve(".gitignore"));
        assertTrue(ignore.lines().anyMatch(line -> line.trim().equals("/docs/agent/")));

        String docsIndex = read(root.resolve("docs/README.md"));
        assertTrue(!docsIndex.contains("](agent/"));
        assertTrue(!docsIndex.contains("](docs/agent/"));
        assertTrue(!read(project.resolve("README.md")).contains("thesis/snapshot-ocl-slice"));
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
