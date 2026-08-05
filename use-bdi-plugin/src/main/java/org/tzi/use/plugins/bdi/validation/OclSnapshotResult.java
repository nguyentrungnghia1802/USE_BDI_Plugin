package org.tzi.use.plugins.bdi.validation;

import java.util.List;
import java.util.Objects;

/** Evidence returned by the USE-side OCL adapter without exposing USE types. */
public record OclSnapshotResult(String subject, OclSnapshotStatus status, List<String> evidence) {
    public OclSnapshotResult {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }
        Objects.requireNonNull(status, "status");
        evidence = List.copyOf(Objects.requireNonNull(evidence, "evidence"));
    }
}
