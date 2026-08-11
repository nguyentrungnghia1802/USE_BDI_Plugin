package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.model.organization.OrganizationModel;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.Cardinality;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.Goal;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.Group;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.Mission;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.Norm;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.NormType;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.Role;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.RoleCardinality;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.Scheme;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.SourceSpan;
import org.tzi.use.plugins.bdi.model.organization.OrganizationModel.UnsupportedFeature;
import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;

import moise.os.OS;

/** The only boundary allowed to expose official Moise classes. */
public final class MoiseOrganizationParserAdapter {
    public ImportResult parse(Path projectRoot, Path organizationFile) {
        Path root = Objects.requireNonNull(projectRoot, "projectRoot").toAbsolutePath().normalize();
        Path source = Objects.requireNonNull(organizationFile, "organizationFile").toAbsolutePath().normalize();
        if (!Files.isRegularFile(source)) {
            return failure(MasProjectDiagnostic.MISSING_ORGANIZATION, source,
                    "Moise organization source does not exist: " + source);
        }

        OS specification;
        try {
            specification = OS.loadOSFromURI(source.toUri().toString());
        } catch (RuntimeException | LinkageError error) {
            return failure(MasProjectDiagnostic.INVALID_ORGANIZATION, source,
                    "Moise organization parser failed: " + safeMessage(error));
        }
        if (specification == null) {
            return failure(MasProjectDiagnostic.INVALID_ORGANIZATION, source,
                    "Moise rejected the organization source; verify it against the bundled os.xsd schema");
        }

        try {
            ProjectSourceId sourceId = ProjectSourceId.fromPath(root, source);
            SourceSpan span = SourceSpan.unknown(sourceId);
            List<UnsupportedFeature> unsupported = new ArrayList<>();
            List<Role> roles = normalizeRoles(specification, span, unsupported);
            List<Group> groups = normalizeGroups(specification, span, unsupported);
            Map<String, String> missionIds = new LinkedHashMap<>();
            List<Scheme> schemes = normalizeSchemes(specification, span, missionIds, unsupported);
            List<Norm> norms = normalizeNorms(specification, span, missionIds, unsupported);
            unsupported.sort(Comparator.comparing(UnsupportedFeature::code)
                    .thenComparing(UnsupportedFeature::elementQualifiedId)
                    .thenComparing(UnsupportedFeature::detail));

            OrganizationModel model = new OrganizationModel(
                    specification.getId(), sourceId, span, roles, groups, schemes, norms, unsupported);
            List<MasProjectDiagnostic> diagnostics = unsupported.stream()
                    .map(feature -> diagnostic(
                            MasProjectDiagnostic.UNSUPPORTED_ORGANIZATION_FEATURE,
                            MasProjectDiagnosticSeverity.WARNING,
                            source,
                            feature.code() + " at " + feature.elementQualifiedId() + ": " + feature.detail()))
                    .toList();
            return new ImportResult(Optional.of(model), diagnostics);
        } catch (RuntimeException | LinkageError error) {
            return failure(MasProjectDiagnostic.INVALID_ORGANIZATION, source,
                    "Could not normalize Moise organization: " + safeMessage(error));
        }
    }

    private static List<Role> normalizeRoles(
            OS specification, SourceSpan span, List<UnsupportedFeature> unsupported) {
        return specification.getSS().getRolesDef().stream()
                .sorted(Comparator.comparing(moise.os.ss.Role::getFullId))
                .map(role -> {
                    String qualifiedId = roleId(role.getFullId());
                    if (role.getSuperRoles().stream().anyMatch(value -> !"soc".equals(value.getId()))) {
                        unsupported.add(new UnsupportedFeature(
                                "MOISE-ROLE-INHERITANCE", qualifiedId,
                                "role inheritance is retained as unsupported evidence", span));
                    }
                    return new Role(role.getId(), qualifiedId, span);
                })
                .toList();
    }

    private static List<Group> normalizeGroups(
            OS specification, SourceSpan span, List<UnsupportedFeature> unsupported) {
        List<moise.os.ss.Group> sourceGroups = new ArrayList<>();
        moise.os.ss.Group root = specification.getSS().getRootGrSpec();
        if (root != null) {
            sourceGroups.add(root);
            sourceGroups.addAll(root.getAllSubGroupsTree());
        }
        return sourceGroups.stream()
                .distinct()
                .sorted(Comparator.comparing(moise.os.ss.Group::getFullId))
                .map(group -> {
                    String qualifiedId = groupId(group.getFullId());
                    if (!group.getLinks().isEmpty()) {
                        unsupported.add(new UnsupportedFeature(
                                "MOISE-GROUP-LINK", qualifiedId,
                                "group links are outside the organization IR pilot", span));
                    }
                    if (!group.getCompatibilities().isEmpty()) {
                        unsupported.add(new UnsupportedFeature(
                                "MOISE-GROUP-COMPATIBILITY", qualifiedId,
                                "group compatibility relations are outside the organization IR pilot", span));
                    }
                    List<RoleCardinality> roles = group.getRoles().getAll().stream()
                            .sorted(Comparator.comparing(moise.os.ss.Role::getFullId))
                            .map(role -> new RoleCardinality(
                                    roleId(role.getFullId()), cardinality(group.getRoleCardinality(role))))
                            .toList();
                    String parent = group.getSuperGroup() == null
                            ? null
                            : groupId(group.getSuperGroup().getFullId());
                    return new Group(group.getId(), qualifiedId, parent, roles, span);
                })
                .toList();
    }

    private static List<Scheme> normalizeSchemes(
            OS specification,
            SourceSpan span,
            Map<String, String> missionIds,
            List<UnsupportedFeature> unsupported) {
        return specification.getFS().getSchemes().stream()
                .sorted(Comparator.comparing(moise.os.fs.Scheme::getFullId))
                .map(scheme -> {
                    String schemeQualifiedId = schemeId(scheme.getFullId());
                    List<Goal> goals = scheme.getGoals().stream()
                            .sorted(Comparator.comparing(moise.os.fs.Goal::getFullId))
                            .map(goal -> {
                                String qualifiedId = goalId(scheme.getFullId(), goal.getId());
                                if (goal.hasPlan() || goal.hasArguments() || goal.hasDependence()
                                        || present(goal.getTTF()) || present(goal.getLocation())) {
                                    unsupported.add(new UnsupportedFeature(
                                            "MOISE-GOAL-DETAIL", qualifiedId,
                                            "goal plans, arguments, dependencies, timing, and location are outside the pilot",
                                            span));
                                }
                                return new Goal(goal.getId(), qualifiedId, span);
                            })
                            .toList();
                    List<Mission> missions = scheme.getMissions().stream()
                            .sorted(Comparator.comparing(moise.os.fs.Mission::getFullId))
                            .map(mission -> {
                                String qualifiedId = missionId(scheme.getFullId(), mission.getId());
                                String previous = missionIds.putIfAbsent(mission.getFullId(), qualifiedId);
                                if (previous != null && !previous.equals(qualifiedId)) {
                                    throw new IllegalArgumentException(
                                            "Ambiguous mission identity across schemes: " + mission.getFullId());
                                }
                                if (!mission.getPreferables().isEmpty()) {
                                    unsupported.add(new UnsupportedFeature(
                                            "MOISE-MISSION-PREFERENCE", qualifiedId,
                                            "mission preferences are outside the organization IR pilot", span));
                                }
                                List<String> goalIds = mission.getGoals().stream()
                                        .map(goal -> goalId(scheme.getFullId(), goal.getId()))
                                        .sorted()
                                        .toList();
                                return new Mission(
                                        mission.getId(), qualifiedId,
                                        cardinality(scheme.getMissionCardinality(mission)), goalIds, span);
                            })
                            .toList();
                    return new Scheme(scheme.getId(), schemeQualifiedId, goals, missions, span);
                })
                .toList();
    }

    private static List<Norm> normalizeNorms(
            OS specification,
            SourceSpan span,
            Map<String, String> missionIds,
            List<UnsupportedFeature> unsupported) {
        return specification.getNS().getNorms().stream()
                .sorted(Comparator.comparing(moise.os.ns.Norm::getFullId))
                .map(norm -> {
                    String qualifiedId = normId(norm.getFullId());
                    if (norm.getTimeConstraint() != null || !"true".equals(norm.getCondition())) {
                        unsupported.add(new UnsupportedFeature(
                                "MOISE-NORM-CONDITION", qualifiedId,
                                "norm conditions and time constraints are outside the organization IR pilot", span));
                    }
                    String missionQualifiedId = missionIds.get(norm.getMission().getFullId());
                    if (missionQualifiedId == null) {
                        throw new IllegalArgumentException(
                                "Norm references unknown mission: " + norm.getMission().getFullId());
                    }
                    NormType type = switch (norm.getType()) {
                        case permission -> NormType.PERMISSION;
                        case obligation -> NormType.OBLIGATION;
                    };
                    return new Norm(
                            norm.getId(), qualifiedId, type, roleId(norm.getRole().getFullId()),
                            missionQualifiedId, span);
                })
                .toList();
    }

    private static Cardinality cardinality(moise.os.Cardinality value) {
        return new Cardinality(value.getMin(), value.getMax());
    }

    private static String roleId(String id) {
        return "role:" + id;
    }

    private static String groupId(String id) {
        return "group:" + id;
    }

    private static String schemeId(String id) {
        return "scheme:" + id;
    }

    private static String goalId(String scheme, String id) {
        return schemeId(scheme) + "/goal:" + id;
    }

    private static String missionId(String scheme, String id) {
        return schemeId(scheme) + "/mission:" + id;
    }

    private static String normId(String id) {
        return "norm:" + id;
    }

    private static ImportResult failure(String code, Path source, String message) {
        return new ImportResult(Optional.empty(), List.of(diagnostic(
                code, MasProjectDiagnosticSeverity.ERROR, source, message)));
    }

    private static MasProjectDiagnostic diagnostic(
            String code, MasProjectDiagnosticSeverity severity, Path source, String message) {
        return new MasProjectDiagnostic(code, severity, source, 0, 0, message);
    }

    private static String safeMessage(Throwable error) {
        return error.getMessage() == null || error.getMessage().isBlank()
                ? error.getClass().getSimpleName()
                : error.getMessage();
    }

    private static boolean present(String value) {
        return value != null && !value.isBlank();
    }

    public record ImportResult(
            Optional<OrganizationModel> organization,
            List<MasProjectDiagnostic> diagnostics) {
        public ImportResult {
            organization = Objects.requireNonNull(organization, "organization");
            diagnostics = List.copyOf(Objects.requireNonNull(diagnostics, "diagnostics"));
        }
    }
}
