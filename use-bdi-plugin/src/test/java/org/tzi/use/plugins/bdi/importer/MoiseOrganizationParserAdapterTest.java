package org.tzi.use.plugins.bdi.importer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.tzi.use.plugins.bdi.application.MasProjectImportService;
import org.tzi.use.plugins.bdi.model.mas.MasResourceKind;
import org.tzi.use.plugins.bdi.model.mas.MasResourceStatus;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.NormType;

class MoiseOrganizationParserAdapterTest {
    private final MoiseOrganizationParserAdapter adapter = new MoiseOrganizationParserAdapter();

    @Test
    void normalizesAuctionOrganizationDeterministicallyThroughProjectImport() throws Exception {
        Path project = fixture("fixtures/casestudy/auction/auction.jcm");
        var first = new MasProjectImportService().importProject(project);
        var second = new MasProjectImportService().importProject(project);

        var resource = first.project().orElseThrow().resources().stream()
                .filter(value -> value.kind() == MasResourceKind.ORGANIZATION)
                .findFirst().orElseThrow();
        var organization = first.project().orElseThrow().organizations().get(0);

        assertEquals(MasResourceStatus.NORMALIZED, resource.status());
        assertEquals(organization, second.project().orElseThrow().organizations().get(0));
        assertEquals(List.of("role:auctioneer", "role:participant", "role:soc"),
                organization.roles().stream().map(value -> value.qualifiedId()).toList());
        assertEquals(List.of("group:auctionGroup"),
                organization.groups().stream().map(value -> value.qualifiedId()).toList());
        assertEquals(1, organization.groups().get(0).roles().get(0).cardinality().minimum());
        assertEquals(300, organization.groups().get(0).roles().get(1).cardinality().maximum());
        assertEquals(List.of("scheme:doAuction/mission:mAuctioneer", "scheme:doAuction/mission:mParticipant"),
                organization.schemes().get(0).missions().stream().map(value -> value.qualifiedId()).toList());
        assertEquals(List.of(NormType.PERMISSION, NormType.OBLIGATION),
                organization.norms().stream().map(value -> value.type()).toList());
        assertFalse(organization.span().positioned());
        assertTrue(organization.unsupportedFeatures().isEmpty());
        assertFalse(first.diagnostics().stream()
                .filter(value -> value.code().equals(MasProjectDiagnostic.UNSUPPORTED_RESOURCE))
                .anyMatch(value -> value.source().getFileName().toString().equals("auction-organization.xml")));
    }

    @Test
    void reportsMissingInvalidAndUnsupportedOrganizationEvidence(@TempDir Path root) throws Exception {
        Path missing = root.resolve("missing.xml");
        assertEquals(MasProjectDiagnostic.MISSING_ORGANIZATION,
                adapter.parse(root, missing).diagnostics().get(0).code());

        Path invalid = root.resolve("invalid.xml");
        Files.writeString(invalid, "<not-an-organization/>", StandardCharsets.UTF_8);
        assertEquals(MasProjectDiagnostic.INVALID_ORGANIZATION,
                adapter.parse(root, invalid).diagnostics().get(0).code());

        Path unsupported = root.resolve("unsupported.xml");
        String auction = Files.readString(
                fixture("fixtures/casestudy/auction/auction-organization.xml"), StandardCharsets.UTF_8);
        Files.writeString(unsupported, auction.replace(
                "    </group-specification>",
                "      <links><link from=\"auctioneer\" to=\"participant\" type=\"communication\" "
                        + "scope=\"intra-group\" extends-subgroups=\"false\" bi-dir=\"true\"/></links>\n"
                        + "    </group-specification>"), StandardCharsets.UTF_8);
        var unsupportedResult = adapter.parse(root, unsupported);

        assertTrue(unsupportedResult.organization().isPresent());
        assertEquals(MasProjectDiagnostic.UNSUPPORTED_ORGANIZATION_FEATURE,
                unsupportedResult.diagnostics().get(0).code());
        assertTrue(unsupportedResult.organization().orElseThrow().unsupportedFeatures().stream()
                .anyMatch(value -> value.code().equals("MOISE-GROUP-LINK")));
        assertEquals(unsupported.toAbsolutePath().normalize(),
                unsupportedResult.diagnostics().get(0).source());
    }

    @Test
    void rejectsDuplicateOrganizationSourceWithoutDuplicatingIr(@TempDir Path root) throws Exception {
        Files.copy(fixture("fixtures/casestudy/auction/auction-organization.xml"),
                root.resolve("first.xml"));
        Files.copy(fixture("fixtures/casestudy/auction/auction-organization.xml"),
                root.resolve("second.xml"));
        Path project = root.resolve("duplicate.jcm");
        Files.writeString(project, """
                mas duplicate {
                    organisation first : first.xml {}
                    organisation second : second.xml {}
                }
                """, StandardCharsets.UTF_8);

        var result = new MasProjectImportService().importProject(project);

        assertEquals(1, result.project().orElseThrow().organizations().size());
        assertEquals(List.of(MasResourceStatus.NORMALIZED, MasResourceStatus.INVALID),
                result.project().orElseThrow().resources().stream().map(value -> value.status()).toList());
        assertTrue(result.diagnostics().stream()
                .anyMatch(value -> value.code().equals(MasProjectDiagnostic.DUPLICATE_ORGANIZATION)));
    }

    @Test
    void confinesMoiseConcreteImportsToTheAdapterAndDeclaresPinnedDependency() throws Exception {
        Path root = repositoryRoot();
        Path sourceRoot = root.resolve("use-bdi-plugin/src/main/java");
        try (var sources = Files.walk(sourceRoot)) {
            List<Path> imports = sources.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> {
                        try {
                            return Files.readString(path).contains("import moise.");
                        } catch (java.io.IOException error) {
                            throw new java.io.UncheckedIOException(error);
                        }
                    })
                    .toList();
            assertEquals(List.of(sourceRoot.resolve(
                    "org/tzi/use/plugins/bdi/importer/MoiseOrganizationParserAdapter.java")), imports);
        }
        String pom = Files.readString(root.resolve("use-bdi-plugin/pom.xml"));
        assertTrue(pom.contains("<artifactId>moise</artifactId>"));
        assertTrue(pom.contains("<moise.version>1.1</moise.version>"));
    }

    private static Path fixture(String name) throws Exception {
        var resource = MoiseOrganizationParserAdapterTest.class.getClassLoader().getResource(name);
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
