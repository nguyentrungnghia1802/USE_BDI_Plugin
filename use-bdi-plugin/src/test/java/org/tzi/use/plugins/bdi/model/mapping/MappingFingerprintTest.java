package org.tzi.use.plugins.bdi.model.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class MappingFingerprintTest {

    @Test
    void bindingOrderDoesNotChangeFingerprint() {
        MappingBinding agent = new MappingBinding(
                MappingKind.AGENT_CLASS,
                "worker",
                "Worker",
                Optional.of("self.active"),
                List.of("agent declaration"));
        MappingBinding action = new MappingBinding(
                MappingKind.ACTION_OPERATION,
                "serve",
                "Counter::serve",
                Optional.empty(),
                List.of("plan body"));

        MappingDocument first = new MappingDocument(
                MappingDocument.CURRENT_SCHEMA_VERSION,
                "bdi-0.1.0",
                "use-hash",
                List.of(agent, action));
        MappingDocument reordered = new MappingDocument(
                MappingDocument.CURRENT_SCHEMA_VERSION,
                "bdi-0.1.0",
                "use-hash",
                List.of(action, agent));

        String fingerprint = MappingFingerprint.compute(first);

        assertEquals(64, fingerprint.length());
        assertEquals(fingerprint, MappingFingerprint.compute(reordered));
    }

    @Test
    void bindingTargetChangeChangesFingerprint() {
        MappingDocument original = MappingDocument.empty("use-hash").upsert(
                new MappingBinding(MappingKind.AGENT_CLASS, "worker", "Worker"));
        MappingDocument changed = MappingDocument.empty("use-hash").upsert(
                new MappingBinding(MappingKind.AGENT_CLASS, "worker", "Customer"));

        assertNotEquals(MappingFingerprint.compute(original), MappingFingerprint.compute(changed));
    }
}
