package org.tzi.use.plugins.bdi.model.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class MappingModelTest {
    @Test
    void upsertReplacesOneRelationWithoutMutatingTheOriginalDocument() {
        MappingDocument original = MappingDocument.empty("use-fingerprint");
        MappingBinding first = new MappingBinding(
                MappingKind.AGENT_CLASS,
                "agent.asl",
                "ManagerAgent",
                Optional.empty(),
                List.of("exact name"));
        MappingDocument withFirst = original.upsert(first);
        MappingDocument withReplacement = withFirst.upsert(
                new MappingBinding(MappingKind.AGENT_CLASS, "agent.asl", "QueueAgent"));

        assertEquals(0, original.bindings().size());
        assertEquals("ManagerAgent", withFirst.find(MappingKind.AGENT_CLASS, "agent.asl").orElseThrow().target());
        assertEquals("QueueAgent", withReplacement.find(MappingKind.AGENT_CLASS, "agent.asl").orElseThrow().target());
        assertEquals(1, withReplacement.bindings().size());
    }

    @Test
    void duplicateKeysAreRejectedByTheSchemaRoot() {
        MappingBinding binding = new MappingBinding(MappingKind.BELIEF_ATTRIBUTE, "ready/0", "Queue::ready");
        assertThrows(IllegalArgumentException.class, () -> new MappingDocument(
                MappingDocument.CURRENT_SCHEMA_VERSION,
                "0.1.0",
                "fingerprint",
                List.of(binding, binding)));
    }
}
