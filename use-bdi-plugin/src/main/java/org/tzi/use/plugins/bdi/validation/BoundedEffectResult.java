package org.tzi.use.plugins.bdi.validation;

import java.util.List;
import java.util.Objects;

/** Evidence returned after a bounded effect variation has been restored. */
public record BoundedEffectResult(BoundedEffectStatus status, List<String> evidence) {
    public BoundedEffectResult {
        Objects.requireNonNull(status, "status");
        evidence = List.copyOf(Objects.requireNonNull(evidence, "evidence"));
    }
}
