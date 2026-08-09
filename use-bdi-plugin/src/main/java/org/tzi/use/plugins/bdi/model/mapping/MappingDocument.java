package org.tzi.use.plugins.bdi.model.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.tzi.use.plugins.bdi.index.BdiMetamodelVersion;

/** Immutable .bdimap.json schema root. */
public record MappingDocument(
        String schemaVersion,
        String bdiMetamodelVersion,
        String useFingerprint,
        List<MappingBinding> bindings) {
    public static final String CURRENT_SCHEMA_VERSION = "0.2.0";

    public MappingDocument {
        requireText(schemaVersion, "schemaVersion");
        if (!CURRENT_SCHEMA_VERSION.equals(schemaVersion)) {
            throw new IllegalArgumentException("Unsupported mapping schema version: " + schemaVersion);
        }
        requireText(bdiMetamodelVersion, "bdiMetamodelVersion");
        requireText(useFingerprint, "useFingerprint");
        bindings = List.copyOf(Objects.requireNonNull(bindings, "bindings"));
        Set<String> keys = new HashSet<>();
        for (MappingBinding binding : bindings) {
            Objects.requireNonNull(binding, "binding");
            if (!keys.add(binding.key())) {
                throw new IllegalArgumentException("Duplicate mapping binding: " + binding.key());
            }
        }
    }

    public static MappingDocument empty(String useFingerprint) {
        return new MappingDocument(
                CURRENT_SCHEMA_VERSION,
                BdiMetamodelVersion.CURRENT,
                useFingerprint,
                List.of());
    }

    public Optional<MappingBinding> find(MappingKind kind, String source) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(source, "source");
        return bindings.stream()
                .filter(binding -> binding.kind() == kind && binding.source().equals(source))
                .findFirst();
    }

    public MappingDocument upsert(MappingBinding binding) {
        Objects.requireNonNull(binding, "binding");
        List<MappingBinding> updated = new ArrayList<>(bindings);
        for (int index = 0; index < updated.size(); index++) {
            if (updated.get(index).key().equals(binding.key())) {
                updated.set(index, binding);
                return new MappingDocument(schemaVersion, bdiMetamodelVersion, useFingerprint, updated);
            }
        }
        updated.add(binding);
        return new MappingDocument(schemaVersion, bdiMetamodelVersion, useFingerprint, updated);
    }

    public MappingDocument remove(MappingKind kind, String source) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(source, "source");
        List<MappingBinding> updated = bindings.stream()
                .filter(binding -> !(binding.kind() == kind && binding.source().equals(source)))
                .toList();
        return new MappingDocument(schemaVersion, bdiMetamodelVersion, useFingerprint, updated);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
