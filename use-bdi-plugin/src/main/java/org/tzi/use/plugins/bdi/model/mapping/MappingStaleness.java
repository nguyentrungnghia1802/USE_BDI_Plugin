package org.tzi.use.plugins.bdi.model.mapping;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.ir.SourceSpan;

/** Immutable evidence that a mapping document or individual binding is stale. */
public record MappingStaleness(
        MappingStalenessReason reason,
        Optional<MappingBinding> binding,
        String message,
        Optional<SourceSpan> sourceSpan,
        List<String> evidence) {
    public MappingStaleness {
        Objects.requireNonNull(reason, "reason");
        binding = Objects.requireNonNull(binding, "binding");
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
        sourceSpan = Objects.requireNonNull(sourceSpan, "sourceSpan");
        evidence = List.copyOf(Objects.requireNonNull(evidence, "evidence"));
    }
}
