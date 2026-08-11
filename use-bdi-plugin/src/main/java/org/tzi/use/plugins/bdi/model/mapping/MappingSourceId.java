package org.tzi.use.plugins.bdi.model.mapping;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.index.ActionCallSite;
import org.tzi.use.plugins.bdi.index.AgentObjectReference;
import org.tzi.use.plugins.bdi.index.PredicateSignature;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;

/** Stable source identifiers shared by mapping suggestions and consistency checks. */
public final class MappingSourceId {
    private MappingSourceId() {
    }

    public static String agent(AgentModel agent) {
        return normalizedPath(Objects.requireNonNull(agent, "agent").source());
    }

    public static String action(ActionCallSite action) {
        Objects.requireNonNull(action, "action");
        return normalizedPath(action.sourceSpan().source())
                + "#plan:" + stablePlanLabel(action.planLabel())
                + "#step:" + action.stepIndex();
    }

    public static String argument(ActionCallSite action, int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must not be negative");
        }
        return action(action) + "/argument/" + index;
    }

    public static String receiver(AgentObjectReference reference) {
        Objects.requireNonNull(reference, "reference");
        return normalizedPath(reference.sourceSpan().source())
                + "#receiver:" + reference.name()
                + "#line:" + reference.sourceSpan().beginLine()
                + "#column:" + reference.sourceSpan().beginColumn();
    }

    public static String belief(PredicateSignature signature) {
        return Objects.requireNonNull(signature, "signature").toString();
    }

    public static Optional<Path> sourcePath(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            return Optional.empty();
        }
        int marker = sourceId.indexOf('#');
        String candidate = marker < 0 ? sourceId : sourceId.substring(0, marker);
        try {
            Path path = Path.of(candidate);
            return path.isAbsolute() ? Optional.of(path.toAbsolutePath().normalize()) : Optional.empty();
        } catch (InvalidPathException error) {
            return Optional.empty();
        }
    }

    private static String normalizedPath(Path source) {
        return Objects.requireNonNull(source, "source").toAbsolutePath().normalize().toString().replace('\\', '/');
    }

    /** Jason may include an absolute source URL in a generated plan label. */
    public static String stablePlanLabel(String label) {
        String value = Objects.requireNonNull(label, "label");
        int marker = value.indexOf(",url(\"");
        if (marker < 0) {
            return value;
        }
        int end = value.indexOf("\")", marker + 6);
        if (end < 0) {
            return value;
        }
        return value.substring(0, marker) + value.substring(end + 2);
    }
}
