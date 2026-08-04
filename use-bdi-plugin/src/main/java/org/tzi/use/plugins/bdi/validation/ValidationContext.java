package org.tzi.use.plugins.bdi.validation;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.importer.AslDiagnostic;
import org.tzi.use.plugins.bdi.index.ActionCallSite;
import org.tzi.use.plugins.bdi.index.BdiIndex;
import org.tzi.use.plugins.bdi.model.ir.ActionStepModel;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.model.ir.InternalActionStepModel;
import org.tzi.use.plugins.bdi.model.ir.PlanModel;
import org.tzi.use.plugins.bdi.model.ir.PlanStepModel;
import org.tzi.use.plugins.bdi.model.ir.TermModel;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingSourceId;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;

/** All immutable inputs available to static consistency rules. */
public record ValidationContext(
        List<AgentModel> agents,
        List<AslDiagnostic> diagnostics,
        BdiIndex index,
        MappingDocument mapping,
        Optional<UseModelSnapshot> uml) {
    public ValidationContext {
        agents = List.copyOf(Objects.requireNonNull(agents, "agents"));
        diagnostics = List.copyOf(Objects.requireNonNull(diagnostics, "diagnostics"));
        index = Objects.requireNonNull(index, "index");
        mapping = Objects.requireNonNull(mapping, "mapping");
        uml = Objects.requireNonNull(uml, "uml");
    }

    public static ValidationContext from(
            BdiImportSnapshot snapshot,
            MappingDocument mapping,
            Optional<UseModelSnapshot> uml) {
        Objects.requireNonNull(snapshot, "snapshot");
        return new ValidationContext(snapshot.models(), snapshot.diagnostics(), snapshot.index(), mapping, uml);
    }

    public Optional<AgentModel> agentFor(Path source) {
        Path normalized = Objects.requireNonNull(source, "source").toAbsolutePath().normalize();
        return agents.stream().filter(agent -> agent.source().equals(normalized)).findFirst();
    }

    public Optional<PlanModel> planFor(ActionCallSite action) {
        return agentFor(action.sourceSpan().source()).flatMap(agent -> agent.plans().stream()
                .filter(plan -> plan.label().equals(action.planLabel()))
                .findFirst());
    }

    public Optional<TermModel> actionTerm(ActionCallSite action) {
        return planFor(action).flatMap(plan -> {
            int zeroBasedIndex = action.stepIndex() - 1;
            if (zeroBasedIndex < 0 || zeroBasedIndex >= plan.steps().size()) {
                return Optional.empty();
            }
            PlanStepModel step = plan.steps().get(zeroBasedIndex);
            if (step instanceof ActionStepModel external) {
                return Optional.of(external.action());
            }
            if (step instanceof InternalActionStepModel internal) {
                return Optional.of(internal.action());
            }
            return Optional.empty();
        });
    }

    public String agentSource(ActionCallSite action) {
        return agentFor(action.sourceSpan().source())
                .map(MappingSourceId::agent)
                .orElseGet(() -> MappingSourceId.sourcePath(MappingSourceId.action(action))
                        .map(path -> path.toString().replace('\\', '/'))
                        .orElse("unknown"));
    }
}
