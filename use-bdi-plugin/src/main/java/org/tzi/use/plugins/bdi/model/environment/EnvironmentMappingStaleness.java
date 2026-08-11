package org.tzi.use.plugins.bdi.model.environment;

import java.util.List;
import java.util.Objects;

/** Explicit target revalidation state retained for audit. */
public record EnvironmentMappingStaleness(
        EnvironmentMappingStalenessStatus status,
        List<String> reasons) {
    public EnvironmentMappingStaleness {
        status = Objects.requireNonNull(status, "status");
        reasons = List.copyOf(Objects.requireNonNull(reasons, "reasons"));
        if (status == EnvironmentMappingStalenessStatus.CURRENT && !reasons.isEmpty()) {
            throw new IllegalArgumentException("CURRENT mapping staleness must not have reasons");
        }
        if (status != EnvironmentMappingStalenessStatus.CURRENT && reasons.isEmpty()) {
            throw new IllegalArgumentException("Non-current mapping staleness requires reasons");
        }
        for (String reason : reasons) {
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("staleness reasons must not be blank");
            }
        }
    }

    public static EnvironmentMappingStaleness current() {
        return new EnvironmentMappingStaleness(EnvironmentMappingStalenessStatus.CURRENT, List.of());
    }

    public static EnvironmentMappingStaleness stale(String reason) {
        return new EnvironmentMappingStaleness(
                EnvironmentMappingStalenessStatus.STALE, List.of(requireReason(reason)));
    }

    public static EnvironmentMappingStaleness unknown(String reason) {
        return new EnvironmentMappingStaleness(
                EnvironmentMappingStalenessStatus.UNKNOWN, List.of(requireReason(reason)));
    }

    private static String requireReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason must not be blank");
        }
        return reason;
    }
}
