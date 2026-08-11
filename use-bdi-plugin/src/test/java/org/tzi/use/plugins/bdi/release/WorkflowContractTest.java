package org.tzi.use.plugins.bdi.release;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class WorkflowContractTest {
    @Test
    void usesSupportedNode24ActionsAndAJava21Distribution() throws Exception {
        Path root = repositoryRoot();
        String continuousIntegration = Files.readString(root.resolve(".github/workflows/maven.yml"));
        String release = Files.readString(root.resolve(".github/workflows/release.yml"));

        for (String workflow : new String[] {continuousIntegration, release}) {
            assertTrue(workflow.contains("actions/checkout@v5"));
            assertTrue(workflow.contains("actions/setup-java@v5"));
            assertTrue(workflow.contains("java-version: '21'"));
            assertTrue(workflow.contains("distribution: 'temurin'"));
            assertFalse(workflow.contains("actions/checkout@v3"));
            assertFalse(workflow.contains("actions/setup-java@v3"));
        }
        assertTrue(continuousIntegration.contains("actions/upload-artifact@v4"));
        assertFalse(release.contains("JasonEtco/create-an-issue"));
        assertTrue(release.contains("gh issue create"));
        assertTrue(release.contains("issues: write"));

        String issueTemplate = Files.readString(
                root.resolve(".github/ISSUE_TEMPLATE/version_update.md"));
        assertFalse(issueTemplate.contains("tiome"));
    }

    private static Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("docs/project/16_PROJECT_COMPLETION_CHECKLIST.md"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate repository root");
    }
}
