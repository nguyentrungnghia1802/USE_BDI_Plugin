package org.tzi.use.plugins.bdi.casestudy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.tzi.use.parser.use.USECompiler;
import org.tzi.use.plugins.bdi.application.MasProjectImportService;
import org.tzi.use.plugins.bdi.model.organization.OrganizationCardinalityMapping;
import org.tzi.use.plugins.bdi.model.organization.OrganizationMapping;
import org.tzi.use.plugins.bdi.model.organization.OrganizationMappingConfirmation;
import org.tzi.use.plugins.bdi.model.organization.OrganizationMissionMapping;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.Cardinality;
import org.tzi.use.plugins.bdi.model.organization.OrganizationRoleMapping;
import org.tzi.use.plugins.bdi.trace.OrganizationTraceabilityGraphBuilder;
import org.tzi.use.plugins.bdi.trace.TraceNode;
import org.tzi.use.plugins.bdi.trace.TraceNodeKind;
import org.tzi.use.plugins.bdi.trace.TraceRelationKind;
import org.tzi.use.plugins.bdi.trace.TraceabilityGraph;
import org.tzi.use.plugins.bdi.trace.TraceabilityGraphJsonSerializer;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseUmlModelFacade;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;
import org.tzi.use.plugins.bdi.validation.IssueSeverity;
import org.tzi.use.plugins.bdi.validation.IssueStatus;
import org.tzi.use.plugins.bdi.validation.organization.OrganizationConsistencyValidator;
import org.tzi.use.plugins.bdi.validation.organization.OrganizationFinding;
import org.tzi.use.plugins.bdi.validation.organization.OrganizationValidationContext;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.sys.MSystem;

class AuctionOrganizationConsistencyTest {
    private static final List<String> REVIEW_EVIDENCE = List.of("Reviewed Auction organization mapping");

    @Test
    void validStaticAuctionMappingHasNoConfirmedErrorAndPreservesRuntimeUnknown() throws Exception {
        MSystem system = loadUmlSystem();
        UseUmlModelFacade facade = new UseUmlModelFacade();
        UseModelSnapshot before = facade.snapshot(system);
        OrganizationValidationContext context = new OrganizationValidationContext(
                organization(), before, validMappings());

        List<OrganizationFinding> findings = new OrganizationConsistencyValidator().evaluate(context);

        assertEquals(before.fingerprint(), facade.snapshot(system).fingerprint());
        assertFalse(findings.stream().anyMatch(finding ->
                finding.issue().severity() == IssueSeverity.ERROR
                        && finding.issue().certainty() == IssueCertainty.CONFIRMED));
        assertEquals(List.of("ORG-003", "ORG-003"), findings.stream()
                .map(finding -> finding.issue().ruleId()).toList());
        assertTrue(findings.stream().allMatch(finding -> finding.issue().severity() == IssueSeverity.INFO));
        assertTrue(findings.stream().allMatch(finding -> finding.issue().certainty() == IssueCertainty.UNKNOWN));
        assertTrue(findings.stream().allMatch(finding -> finding.organizationSource().isPresent()));
        assertTrue(findings.stream().allMatch(finding -> finding.issue().evidence().stream()
                .anyMatch(value -> value.startsWith("source=bdi-source-v2:"))));
    }

    @Test
    void detectsRoleMissionAndCardinalityMutantsWithExactConfirmedEvidence() throws Exception {
        List<OrganizationMapping> mutants = List.of(
                role("role:auctioneer", "MissingRole"),
                mission("scheme:doAuction/mission:mAuctioneer", "Auctioneer::missing()"),
                cardinality("role:participant", "Bidder::ParticipantCapacity", Optional.of(new Cardinality(0, 2))));

        List<OrganizationFinding> findings = new OrganizationConsistencyValidator().evaluate(context(mutants));

        assertEquals(List.of("ORG-001", "ORG-002", "ORG-003"), findings.stream()
                .map(finding -> finding.issue().ruleId()).toList());
        assertTrue(findings.stream().allMatch(finding -> finding.issue().severity() == IssueSeverity.ERROR));
        assertTrue(findings.stream().allMatch(finding -> finding.issue().status() == IssueStatus.OPEN));
        assertTrue(findings.stream().allMatch(finding -> finding.issue().certainty() == IssueCertainty.CONFIRMED));
        assertTrue(findings.stream().allMatch(finding -> finding.issue().evidence().stream()
                .anyMatch(value -> value.startsWith("mapping="))));

        TraceabilityGraph graph = new OrganizationTraceabilityGraphBuilder().build(context(mutants), findings);
        assertTrue(graph.nodes().stream().anyMatch(node -> node.kind() == TraceNodeKind.GAP));
        assertTrue(graph.edges().stream().anyMatch(edge -> edge.relation() == TraceRelationKind.MISSING_TARGET));
        assertTrue(graph.edges().stream().anyMatch(edge -> edge.relation() == TraceRelationKind.ORGANIZATION_TARGET));
    }

    @Test
    void unconfirmedMappingAndUnavailableReviewedBoundsRemainUnknown() throws Exception {
        OrganizationRoleMapping candidate = new OrganizationRoleMapping(
                "role:auctioneer", "Auctioneer", OrganizationMappingConfirmation.CANDIDATE,
                List.of("Name-based candidate only"));
        OrganizationCardinalityMapping unavailable = cardinality(
                "role:participant", "Bidder::ParticipantCapacity", Optional.empty());

        OrganizationValidationContext context = context(List.of(candidate, unavailable));
        List<OrganizationFinding> findings = new OrganizationConsistencyValidator().evaluate(context);

        assertEquals(List.of("ORG-001", "ORG-003"), findings.stream()
                .map(finding -> finding.issue().ruleId()).toList());
        assertTrue(findings.stream().allMatch(finding -> finding.issue().certainty() == IssueCertainty.UNKNOWN));
        TraceabilityGraph graph = new OrganizationTraceabilityGraphBuilder().build(context, findings);
        assertTrue(graph.edges().stream().anyMatch(edge -> edge.relation() == TraceRelationKind.MISSING_MAPPING));
        assertTrue(graph.nodes().stream().anyMatch(node -> node.label().equals("Unconfirmed organization mapping")));
    }

    @Test
    void rejectsDuplicateMappingsAndSerializesDeduplicatedPortableTrace() throws Exception {
        OrganizationRoleMapping mapping = role("role:auctioneer", "MissingRole");
        assertThrows(IllegalArgumentException.class, () -> context(List.of(mapping, mapping)));

        OrganizationValidationContext context = context(List.of(mapping));
        List<OrganizationFinding> findings = new OrganizationConsistencyValidator().evaluate(context);
        List<OrganizationFinding> duplicated = new ArrayList<>(findings);
        duplicated.addAll(findings);
        OrganizationTraceabilityGraphBuilder builder = new OrganizationTraceabilityGraphBuilder();
        TraceabilityGraph graph = builder.build(context, duplicated);

        assertEquals(graph.nodes().size(), graph.nodes().stream().map(TraceNode::id).distinct().count());
        assertEquals(graph.edges().size(), graph.edges().stream().map(edge -> edge.id()).distinct().count());
        String first = new TraceabilityGraphJsonSerializer().serialize(graph);
        String second = new TraceabilityGraphJsonSerializer().serialize(builder.build(context, findings));
        assertEquals(first, second);
        assertFalse(first.matches("(?s).*\\b[A-Za-z]:\\\\.*"), first);
        assertTrue(first.contains("ORGANIZATION_ROLE"));
    }

    @Test
    void organizationDomainRulesAndTraceExposeNoMoiseConcreteTypes() throws Exception {
        Path sourceRoot = repositoryRoot().resolve("use-bdi-plugin/src/main/java");
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
    }

    private static List<OrganizationMapping> validMappings() {
        return List.of(
                role("role:auctioneer", "Auctioneer"),
                role("role:participant", "Bidder"),
                mission("scheme:doAuction/mission:mAuctioneer", "Auctioneer::openAuction()"),
                mission("scheme:doAuction/mission:mParticipant", "Bidder::submitBid()"),
                cardinality("role:auctioneer", "Auctioneer::ExactlyOneAuctioneerRole",
                        Optional.of(new Cardinality(1, 1))),
                cardinality("role:participant", "Bidder::ParticipantCapacity",
                        Optional.of(new Cardinality(0, 300))));
    }

    private static OrganizationRoleMapping role(String source, String target) {
        return new OrganizationRoleMapping(source, target,
                OrganizationMappingConfirmation.CONFIRMED, REVIEW_EVIDENCE);
    }

    private static OrganizationMissionMapping mission(String source, String target) {
        return new OrganizationMissionMapping(source, target,
                OrganizationMappingConfirmation.CONFIRMED, REVIEW_EVIDENCE);
    }

    private static OrganizationCardinalityMapping cardinality(
            String role, String target, Optional<Cardinality> bounds) {
        return new OrganizationCardinalityMapping(
                "group:auctionGroup", role, target, bounds,
                OrganizationMappingConfirmation.CONFIRMED, REVIEW_EVIDENCE);
    }

    private static OrganizationValidationContext context(List<? extends OrganizationMapping> mappings)
            throws Exception {
        return new OrganizationValidationContext(organization(),
                new UseUmlModelFacade().snapshot(loadUmlSystem()), List.copyOf(mappings));
    }

    private static OrganizationModel organization() throws Exception {
        return new MasProjectImportService()
                .importProject(fixture("fixtures/casestudy/auction/auction.jcm"))
                .project().orElseThrow().organizations().get(0);
    }

    private static MSystem loadUmlSystem() throws Exception {
        Path source = fixture("fixtures/casestudy/auction/auction-organization.use");
        StringWriter errors = new StringWriter();
        MModel model = USECompiler.compileSpecification(
                Files.newInputStream(source), source.toString(), new PrintWriter(errors), new ModelFactory());
        if (model == null) {
            throw new AssertionError(errors.toString());
        }
        model.setFilename(source.toString());
        return new MSystem(model);
    }

    private static Path fixture(String name) throws Exception {
        var resource = AuctionOrganizationConsistencyTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing fixture: " + name);
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
