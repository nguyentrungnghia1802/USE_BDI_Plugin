package org.tzi.use.plugins.bdi.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.validation.IssueCertainty;

class EvaluationManifestCodecTest {
    @Test
    void encodesAndDecodesCanonicalManifestBytes() {
        EvaluationManifest manifest = manifest();
        String encoded = EvaluationManifestCodec.encode(manifest);
        EvaluationManifest decoded = EvaluationManifestCodec.decode(encoded);

        assertEquals(manifest, decoded);
        assertEquals(encoded, EvaluationManifestCodec.encode(decoded));
        assertTrue(encoded.contains("\"schemaVersion\":\"0.1.0\""));
    }

    @Test
    void rejectsUnknownFieldsDuplicateIdsTraversalAndMissingOracle() {
        String encoded = EvaluationManifestCodec.encode(manifest());
        assertThrows(IllegalArgumentException.class, () -> EvaluationManifestCodec.decode(
                encoded.replace("\"cases\": [", "\"unknown\": true,\n  \"cases\": [")));
        assertThrows(IllegalArgumentException.class, () -> EvaluationManifestCodec.decode(
                encoded.replace("\"caseStudy\":\"Auction\"", "\"caseStudy\":\"Auction\",\"caseStudy\":\"Again\"")));
        assertThrows(IllegalArgumentException.class, () -> new EvaluationManifest.EvaluationCase(
                "unsafe", "MUTANT", "BDI_UML_OCL", "../Auction.use", List.of("agent.asl"),
                Optional.empty(), Optional.empty(), List.of("MAP-001"), List.of(),
                Map.of("MAP-001", IssueCertainty.CONFIRMED), List.of("docs/evidence.md"), List.of(), Duration.ofSeconds(5)));
        assertThrows(IllegalArgumentException.class, () -> new EvaluationManifest.EvaluationCase(
                "no-oracle", "MUTANT", "BDI_UML_OCL", "Auction.use", List.of("agent.asl"),
                Optional.empty(), Optional.empty(), List.of(), List.of(), Map.of(),
                List.of("docs/evidence.md"), List.of(), Duration.ofSeconds(5)));
        assertThrows(IllegalArgumentException.class, () -> new EvaluationManifest.EvaluationCase(
                "short-timeout", "MUTANT", "BDI_UML_OCL", "Auction.use", List.of("agent.asl"),
                Optional.empty(), Optional.empty(), List.of("MAP-001"), List.of(),
                Map.of("MAP-001", IssueCertainty.CONFIRMED), List.of("docs/evidence.md"), List.of(),
                Duration.ofMillis(999)));
        EvaluationManifest.EvaluationCase duplicate = manifest().cases().get(0);
        assertThrows(IllegalArgumentException.class, () -> new EvaluationManifest(
                EvaluationManifest.CURRENT_SCHEMA_VERSION, "Auction", "0.1.0", "USE-7.1.1",
                "standard", List.of("MOISE"), List.of(duplicate, duplicate)));
    }

    @Test
    void rejectsMalformedJsonNumbers() {
        String encoded = EvaluationManifestCodec.encode(manifest());
        assertThrows(IllegalArgumentException.class, () -> EvaluationManifestCodec.decode(
                encoded.replace("\"timeoutSeconds\":5", "\"timeoutSeconds\":5.")));
        assertThrows(IllegalArgumentException.class, () -> EvaluationManifestCodec.decode(
                encoded.replace("\"timeoutSeconds\":5", "\"timeoutSeconds\":5e")));
    }

    private static EvaluationManifest manifest() {
        EvaluationManifest.EvaluationCase evaluationCase = new EvaluationManifest.EvaluationCase(
                "baseline", "BASELINE", "BDI_UML_OCL", "Auction.use", List.of("agent.asl"),
                Optional.empty(), Optional.empty(), List.of(), List.of("MAP-001"), Map.of(),
                List.of("docs/evidence.md#baseline"), List.of(), Duration.ofSeconds(5));
        return new EvaluationManifest(
                EvaluationManifest.CURRENT_SCHEMA_VERSION,
                "Auction",
                "0.1.0",
                "USE-7.1.1",
                "standard",
                List.of("MOISE"),
                List.of(evaluationCase));
    }
}
