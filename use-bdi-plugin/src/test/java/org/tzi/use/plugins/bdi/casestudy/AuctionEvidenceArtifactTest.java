package org.tzi.use.plugins.bdi.casestudy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class AuctionEvidenceArtifactTest {
    @Test
    void keepsGroundTruthMetricsAndThesisEvidenceArtifactsTraceable() throws Exception {
        Path root = repositoryRoot();
        Path evidence = root.resolve("docs/project/evidence");

        String groundTruth = read(evidence.resolve("auction-ground-truth.json"));
        assertTrue(groundTruth.contains("\"schemaVersion\": \"0.1.0\""));
        assertTrue(groundTruth.contains("\"confirmedMappings\": 14"));
        assertTrue(groundTruth.contains("\"id\": \"STR-001-remove-bidder\""));
        assertTrue(groundTruth.contains("\"id\": \"SIG-001-open-arity\""));
        assertTrue(groundTruth.contains("\"id\": \"REF-001-bidder2\""));
        assertTrue(groundTruth.contains("\"id\": \"OCL-001-open-precondition\""));
        assertTrue(groundTruth.contains("\"mutantCount\": 9"));
        assertTrue(groundTruth.contains("\"mutantCount\": 4"));

        List<String> metrics = Files.readAllLines(evidence.resolve("auction-metrics.csv"));
        assertEquals(5, metrics.size());
        assertTrue(metrics.get(0).startsWith("mutantId,family,ruleId"));
        assertTrue(metrics.stream().anyMatch(line -> line.startsWith("STR-001-remove-bidder,STRUCTURAL,MAP-003,")));
        assertTrue(metrics.stream().anyMatch(line -> line.startsWith("SIG-001-open-arity,SIGNATURE,SIG-001,")));
        assertTrue(metrics.stream().anyMatch(line -> line.startsWith("REF-001-bidder2,REFERENCE,REF-001,")));
        assertTrue(metrics.stream().anyMatch(line -> line.startsWith("OCL-001-open-precondition,OCL,OCL-001,")));
        assertTrue(metrics.stream().allMatch(line -> line.equals(metrics.get(0)) || line.endsWith(",true")));

        String architecture = read(evidence.resolve("auction-architecture.mmd"));
        String ir = read(evidence.resolve("ir-class-diagram.mmd"));
        String bdi = read(evidence.resolve("bdi-metamodel-diagram.mmd"));
        String mappings = read(evidence.resolve("auction-mapping-examples.md"));
        assertTrue(architecture.contains("ValidationOrchestrator"));
        assertTrue(architecture.contains("UseUmlModelFacade"));
        assertTrue(ir.contains("AgentModel *-- PlanModel"));
        assertTrue(ir.contains("PlanStepModel <|-- ActionStepModel"));
        assertTrue(bdi.contains("BdiAgent *-- \"0..* ordered\" Plan : plans"));
        assertTrue(bdi.contains("ExternalAction *-- \"1\" Term : action"));
        assertTrue(mappings.contains("14 bindings"));
        assertTrue(mappings.contains("SIG-001"));
        assertTrue(mappings.contains("OCL-001"));
    }

    private static String read(Path path) throws IOException {
        assertTrue(Files.isRegularFile(path), () -> "Missing evidence artifact: " + path);
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
