package org.tzi.use.plugins.bdi.model.organization;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;

/** Immutable parser-independent subset of one static Moise organization specification. */
public record OrganizationModel(
        String id,
        ProjectSourceId source,
        SourceSpan span,
        List<Role> roles,
        List<Group> groups,
        List<Scheme> schemes,
        List<Norm> norms,
        List<UnsupportedFeature> unsupportedFeatures) {
    public OrganizationModel {
        id = required(id, "id");
        source = Objects.requireNonNull(source, "source");
        span = Objects.requireNonNull(span, "span");
        roles = unique(List.copyOf(Objects.requireNonNull(roles, "roles")), Role::qualifiedId, "role");
        groups = unique(List.copyOf(Objects.requireNonNull(groups, "groups")), Group::qualifiedId, "group");
        schemes = unique(List.copyOf(Objects.requireNonNull(schemes, "schemes")), Scheme::qualifiedId, "scheme");
        norms = unique(List.copyOf(Objects.requireNonNull(norms, "norms")), Norm::qualifiedId, "norm");
        unsupportedFeatures = List.copyOf(Objects.requireNonNull(unsupportedFeatures, "unsupportedFeatures"));
    }

    public record SourceSpan(ProjectSourceId source, int beginLine, int beginColumn, int endLine, int endColumn) {
        public SourceSpan {
            source = Objects.requireNonNull(source, "source");
            if (beginLine < 0 || beginColumn < 0 || endLine < 0 || endColumn < 0) {
                throw new IllegalArgumentException("Source positions must not be negative");
            }
            if ((beginLine == 0) != (endLine == 0) || (beginColumn == 0) != (endColumn == 0)) {
                throw new IllegalArgumentException("Source position bounds must both be known or unknown");
            }
        }

        public static SourceSpan unknown(ProjectSourceId source) {
            return new SourceSpan(source, 0, 0, 0, 0);
        }

        public boolean positioned() {
            return beginLine > 0;
        }
    }

    public record Cardinality(int minimum, int maximum) {
        public static final int UNBOUNDED = Integer.MAX_VALUE;

        public Cardinality {
            if (minimum < 0 || maximum < minimum) {
                throw new IllegalArgumentException("Invalid cardinality " + minimum + ".." + maximum);
            }
        }
    }

    public record Role(String id, String qualifiedId, SourceSpan span) {
        public Role {
            id = required(id, "role id");
            qualifiedId = required(qualifiedId, "role qualifiedId");
            span = Objects.requireNonNull(span, "span");
        }
    }

    public record RoleCardinality(String roleQualifiedId, Cardinality cardinality) {
        public RoleCardinality {
            roleQualifiedId = required(roleQualifiedId, "roleQualifiedId");
            cardinality = Objects.requireNonNull(cardinality, "cardinality");
        }
    }

    public record Group(
            String id,
            String qualifiedId,
            String parentQualifiedId,
            List<RoleCardinality> roles,
            SourceSpan span) {
        public Group {
            id = required(id, "group id");
            qualifiedId = required(qualifiedId, "group qualifiedId");
            roles = List.copyOf(Objects.requireNonNull(roles, "roles"));
            span = Objects.requireNonNull(span, "span");
        }
    }

    public record Goal(String id, String qualifiedId, SourceSpan span) {
        public Goal {
            id = required(id, "goal id");
            qualifiedId = required(qualifiedId, "goal qualifiedId");
            span = Objects.requireNonNull(span, "span");
        }
    }

    public record Mission(
            String id,
            String qualifiedId,
            Cardinality cardinality,
            List<String> goalQualifiedIds,
            SourceSpan span) {
        public Mission {
            id = required(id, "mission id");
            qualifiedId = required(qualifiedId, "mission qualifiedId");
            cardinality = Objects.requireNonNull(cardinality, "cardinality");
            goalQualifiedIds = List.copyOf(Objects.requireNonNull(goalQualifiedIds, "goalQualifiedIds"));
            span = Objects.requireNonNull(span, "span");
        }
    }

    public record Scheme(
            String id,
            String qualifiedId,
            List<Goal> goals,
            List<Mission> missions,
            SourceSpan span) {
        public Scheme {
            id = required(id, "scheme id");
            qualifiedId = required(qualifiedId, "scheme qualifiedId");
            goals = unique(List.copyOf(Objects.requireNonNull(goals, "goals")), Goal::qualifiedId, "goal");
            missions = unique(List.copyOf(Objects.requireNonNull(missions, "missions")), Mission::qualifiedId, "mission");
            span = Objects.requireNonNull(span, "span");
        }
    }

    public enum NormType {
        PERMISSION,
        OBLIGATION
    }

    public record Norm(
            String id,
            String qualifiedId,
            NormType type,
            String roleQualifiedId,
            String missionQualifiedId,
            SourceSpan span) {
        public Norm {
            id = required(id, "norm id");
            qualifiedId = required(qualifiedId, "norm qualifiedId");
            type = Objects.requireNonNull(type, "type");
            roleQualifiedId = required(roleQualifiedId, "roleQualifiedId");
            missionQualifiedId = required(missionQualifiedId, "missionQualifiedId");
            span = Objects.requireNonNull(span, "span");
        }
    }

    public record UnsupportedFeature(String code, String elementQualifiedId, String detail, SourceSpan span) {
        public UnsupportedFeature {
            code = required(code, "code");
            elementQualifiedId = required(elementQualifiedId, "elementQualifiedId");
            detail = required(detail, "detail");
            span = Objects.requireNonNull(span, "span");
        }
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private static <T> List<T> unique(
            List<T> values, java.util.function.Function<T, String> identity, String kind) {
        Set<String> identities = new HashSet<>();
        for (T value : values) {
            if (!identities.add(identity.apply(value))) {
                throw new IllegalArgumentException("Duplicate " + kind + " identity: " + identity.apply(value));
            }
        }
        return values;
    }
}
