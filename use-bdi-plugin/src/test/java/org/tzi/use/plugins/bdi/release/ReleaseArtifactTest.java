package org.tzi.use.plugins.bdi.release;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class ReleaseArtifactTest {
    @Test
    void keepsReleaseGuidesNoticesEvidenceAndBackupProcedureTraceable() throws IOException {
        Path root = repositoryRoot();
        Path project = root.resolve("docs/project");
        for (String document : List.of(
                "USER_GUIDE.md",
                "DEVELOPER_GUIDE.md",
                "THIRD_PARTY_NOTICES.md",
                "evidence/release-package.md")) {
            Path path = project.resolve(document);
            assertTrue(Files.isRegularFile(path), () -> "Missing release document: " + path);
            assertTrue(Files.size(path) > 0, () -> "Empty release document: " + path);
        }

        String userGuide = read(project.resolve("USER_GUIDE.md"));
        assertTrue(userGuide.contains("Plugins > AgentSpeak > Import AgentSpeak..."));
        assertTrue(userGuide.contains("View > Create View > Object diagram"));

        String developerGuide = read(project.resolve("DEVELOPER_GUIDE.md"));
        assertTrue(developerGuide.contains("mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test"));
        assertTrue(developerGuide.contains("Session.hasSystem()"));
        assertTrue(developerGuide.contains("ViewFrame"));

        String notices = read(project.resolve("THIRD_PARTY_NOTICES.md"));
        assertTrue(notices.contains("io.github.jason-lang:jason-interpreter:3.3.0"));
        assertTrue(notices.contains("net.sf.ingenias:jade:4.3"));
        assertTrue(notices.contains("org.glassfish:javax.json:1.1.4"));

        String evidence = read(project.resolve("evidence/release-package.md"));
        assertTrue(evidence.contains("CLEAN_CLONE_REPRODUCIBILITY_OK"));
        assertTrue(evidence.contains("THESIS_BACKUP_BLOCKED_EXTERNAL"));

        Path backupScript = root.resolve("use-bdi-plugin/scripts/backup-thesis-artifacts.ps1");
        assertTrue(Files.isRegularFile(backupScript), () -> "Missing backup script: " + backupScript);
        String script = read(backupScript);
        assertTrue(script.contains("git -C $repoRoot archive"));
        assertTrue(script.contains("THESIS_BACKUP_OK"));
        assertTrue(script.contains("THESIS_BACKUP_BLOCKED_EXTERNAL"));
        assertTrue(script.contains("backupComplete"));
        assertTrue(Files.isRegularFile(root.resolve("use-bdi-plugin/scripts/release-evidence-manifest.ps1")));
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
