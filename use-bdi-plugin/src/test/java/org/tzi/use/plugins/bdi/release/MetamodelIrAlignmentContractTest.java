package org.tzi.use.plugins.bdi.release;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiProjectConfiguration;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisRequest;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisResult;
import org.tzi.use.plugins.bdi.application.MasProjectAnalysisService;
import org.tzi.use.plugins.bdi.model.ir.AchieveGoalStepModel;
import org.tzi.use.plugins.bdi.model.ir.ActionStepModel;
import org.tzi.use.plugins.bdi.model.ir.BeliefUpdateStepModel;
import org.tzi.use.plugins.bdi.model.ir.InternalActionStepModel;
import org.tzi.use.plugins.bdi.model.ir.TestStepModel;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class MetamodelIrAlignmentContractTest {
    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    private static final Set<String> FORBIDDEN_DOMAIN_IMPORT_PREFIXES = Set.of(
            "import jason.", "import jacamo.", "import cartago.", "import moise.", "import org.jacamo.");

    @Test
    void mapsEveryProfileClassToAnExistingJavaRealizationWithoutParserLeakage() throws Exception {
        Path root = repositoryRoot();
        Path alignment = root.resolve("docs/project/metamodel/METAMODEL_TO_JAVA_ALIGNMENT.md");
        String alignmentText = Files.readString(alignment);
        Document ecore = parse(root.resolve("docs/project/metamodel/use-jacamo-analysis.ecore"));
        NodeList classifiers = ecore.getElementsByTagName("eClassifiers");
        List<String> classNames = new ArrayList<>();
        for (int index = 0; index < classifiers.getLength(); index++) {
            Element classifier = (Element) classifiers.item(index);
            if ("ecore:EClass".equals(classifier.getAttributeNS(XSI_NAMESPACE, "type"))) {
                classNames.add(classifier.getAttribute("name"));
            }
        }
        assertEquals(48, classNames.size());
        classNames.forEach(name -> assertTrue(
                alignmentText.contains("| `" + name + "` |"),
                () -> "Profile class has no alignment row: " + name));
        assertFalse(alignmentText.contains("| `GAP` |"));

        for (String type : expectedJavaTypes()) {
            Class.forName(type);
        }

        Path sourceRoot = root.resolve("use-bdi-plugin/src/main/java/org/tzi/use/plugins/bdi");
        for (String packageName : List.of("model", "index", "validation", "trace", "diagram", "report", "use")) {
            Path packageRoot = sourceRoot.resolve(packageName);
            try (var files = Files.walk(packageRoot)) {
                for (Path source : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                    String content = Files.readString(source);
                    for (String prefix : FORBIDDEN_DOMAIN_IMPORT_PREFIXES) {
                        assertFalse(content.contains(prefix),
                                () -> "Concrete parser/runtime dependency leaked past importer: " + source + " -> " + prefix);
                    }
                }
            }
        }
    }

    @Test
    void canonicalFixturesProduceTheDeclaredConceptualKinds() throws Exception {
        Path root = repositoryRoot();
        BdiImportService importer = new BdiImportService();
        var minimal = importer.importFiles(List.of(
                root.resolve("use-bdi-plugin/src/test/resources/fixtures/asl/valid/minimal.asl")));
        assertEquals(1, minimal.models().size());
        var minimalAgent = minimal.models().get(0);
        assertEquals(1, minimalAgent.beliefs().size());
        assertEquals(1, minimalAgent.goals().size());
        assertEquals(1, minimalAgent.plans().size());
        assertTrue(minimalAgent.plans().get(0).context().isPresent());
        assertTrue(minimalAgent.plans().get(0).steps().stream().anyMatch(InternalActionStepModel.class::isInstance));

        Path smartQueueSource = root.resolve("use-bdi-plugin/demo/smart-queue/smart_queue_manager.asl");
        var smartQueue = importer.importFiles(List.of(smartQueueSource));
        assertEquals(1, smartQueue.models().size());
        var smartAgent = smartQueue.models().get(0);
        assertTrue(smartAgent.plans().stream().anyMatch(plan -> plan.context().isPresent()));
        assertTrue(smartAgent.plans().stream().flatMap(plan -> plan.steps().stream())
                .anyMatch(ActionStepModel.class::isInstance));
        assertTrue(smartAgent.plans().stream().flatMap(plan -> plan.steps().stream())
                .anyMatch(InternalActionStepModel.class::isInstance));
        assertTrue(smartAgent.plans().stream().flatMap(plan -> plan.steps().stream())
                .anyMatch(AchieveGoalStepModel.class::isInstance));
        assertTrue(smartAgent.plans().stream().flatMap(plan -> plan.steps().stream())
                .anyMatch(TestStepModel.class::isInstance));
        assertTrue(smartAgent.plans().stream().flatMap(plan -> plan.steps().stream())
                .anyMatch(BeliefUpdateStepModel.class::isInstance));
        String smartMapping = Files.readString(root.resolve(
                "use-bdi-plugin/demo/smart-queue/SmartQueue.bdimap.json"));
        assertTrue(smartMapping.contains("AGENT_CLASS"));
        assertTrue(smartMapping.contains("ACTION_OPERATION"));

        Path auction = root.resolve(
                "use-bdi-plugin/src/test/resources/fixtures/casestudy/auction/auction.jcm");
        MasProjectAnalysisResult auctionResult = new MasProjectAnalysisService().analyze(
                MasProjectAnalysisRequest.of(
                        auction,
                        Instant.parse("2026-08-17T00:00:00Z"),
                        Optional.empty(),
                        Optional.empty(),
                        MappingDocument.empty("unknown"),
                        BdiProjectConfiguration.defaults()));
        var auctionProject = auctionResult.project().orElseThrow();
        assertEquals(List.of("auctioneer", "bidder1", "bidder2"),
                auctionProject.agents().stream().map(agent -> agent.name()).toList());
        assertEquals(2, auctionResult.snapshot().bdiImport().models().size());
        assertEquals(1, auctionProject.organizations().size());
        assertTrue(auctionProject.resources().size() >= 2);
        assertTrue(auctionResult.projectDiagnostics().stream()
                .allMatch(diagnostic -> !diagnostic.message().isBlank()));
    }

    private static Document parse(Path ecore) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return factory.newDocumentBuilder().parse(ecore.toFile());
    }

    private static List<String> expectedJavaTypes() {
        String base = "org.tzi.use.plugins.bdi.model.";
        return List.of(
                base + "source.ProjectSourceId",
                base + "ir.SourceSpan",
                base + "ir.UnsupportedFeature",
                base + "ir.AgentModel",
                base + "ir.BeliefModel",
                base + "ir.GoalModel",
                base + "ir.PlanModel",
                base + "ir.TriggerModel",
                base + "ir.ContextExpr",
                base + "ir.ContextLiteral",
                base + "ir.ContextUnary",
                base + "ir.ContextBinary",
                base + "ir.ContextUnsupported",
                base + "ir.PlanStepModel",
                base + "ir.ActionStepModel",
                base + "ir.InternalActionStepModel",
                base + "ir.AchieveGoalStepModel",
                base + "ir.TestStepModel",
                base + "ir.BeliefUpdateStepModel",
                base + "ir.ConstraintStepModel",
                base + "ir.UnsupportedStepModel",
                base + "ir.TermModel",
                base + "ir.LiteralTermModel",
                base + "ir.CompoundTermModel",
                base + "ir.VariableTermModel",
                base + "ir.NumberTermModel",
                base + "ir.StringTermModel",
                base + "ir.ListTermModel",
                base + "ir.SetTermModel",
                base + "ir.ArithmeticTermModel",
                base + "ir.UnsupportedTermModel",
                base + "mas.MasProjectModel",
                base + "mas.MasAgentInstanceModel",
                base + "mas.MasResourceReference",
                base + "environment.EnvironmentModel",
                base + "environment.ArtifactModel",
                base + "environment.EnvironmentOperation",
                base + "environment.ObservablePropertyModel",
                base + "organization.OrganizationModel",
                base + "organization.OrganizationModel$Role",
                base + "organization.OrganizationModel$Group",
                base + "organization.OrganizationModel$RoleCardinality",
                base + "organization.OrganizationModel$Cardinality",
                base + "organization.OrganizationModel$Scheme",
                base + "organization.OrganizationModel$Goal",
                base + "organization.OrganizationModel$Mission",
                base + "organization.OrganizationModel$Norm");
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
