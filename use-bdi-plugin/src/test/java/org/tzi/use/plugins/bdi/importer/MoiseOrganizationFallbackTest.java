package org.tzi.use.plugins.bdi.importer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.application.MasProjectImportService;
import org.tzi.use.plugins.bdi.model.mas.MasResourceKind;
import org.tzi.use.plugins.bdi.model.mas.MasResourceStatus;

class MoiseOrganizationFallbackTest {
    @Test
    void retainsAuctionOrganizationAsExplicitUnsupportedDiagnostic() throws Exception {
        Path project = fixture("fixtures/casestudy/auction/auction.jcm");
        var result = new MasProjectImportService().importProject(project);

        var organization = result.project().orElseThrow().resources().stream()
                .filter(resource -> resource.kind() == MasResourceKind.ORGANIZATION)
                .findFirst().orElseThrow();
        var diagnostic = result.diagnostics().stream()
                .filter(value -> value.code().equals(MasProjectDiagnostic.UNSUPPORTED_RESOURCE))
                .filter(value -> value.source().getFileName().toString().equals("auction-organization.xml"))
                .findFirst().orElseThrow();

        assertTrue(organization.source().isPresent());
        assertTrue(organization.status() == MasResourceStatus.UNSUPPORTED);
        assertTrue(diagnostic.message().contains("no verified Moise parser/API is packaged"));
        assertTrue(diagnostic.message().contains("not parsed"));
        assertFalse(diagnostic.message().contains("normalized"));
    }

    @Test
    void sourceAndPackageBoundaryContainNoGuessedMoiseAdapter() throws Exception {
        Path root = repositoryRoot();
        Path sourceRoot = root.resolve("use-bdi-plugin/src/main/java");
        try (var sources = Files.walk(sourceRoot)) {
            assertTrue(sources.filter(path -> path.toString().endsWith(".java"))
                    .noneMatch(path -> {
                        try {
                            String source = Files.readString(path);
                            return source.contains("import moise.")
                                    || source.contains("import ora4mas.")
                                    || source.contains("org.jacamo.moise");
                        } catch (java.io.IOException error) {
                            throw new java.io.UncheckedIOException(error);
                        }
                    }));
        }
        String pom = Files.readString(root.resolve("use-bdi-plugin/pom.xml"));
        assertFalse(pom.contains("<artifactId>moise</artifactId>"));
    }

    private static Path fixture(String name) throws Exception {
        var resource = MoiseOrganizationFallbackTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
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
