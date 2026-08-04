package org.tzi.use.plugins.bdi.model.ir;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * File-level root of the Java-only BDI intermediate representation.
 *
 * <p>The metadata counts remain available for summary-only imports, while a
 * parser-backed import can materialize the child IR lists. The model never
 * invents an AgentSpeak agent name that is not declared by the source.</p>
 */
public record AgentModel(
        Path source,
        String parserVersion,
        int beliefCount,
        int goalCount,
        int planCount,
        List<BeliefModel> beliefs,
        List<GoalModel> goals,
        List<PlanModel> plans,
        List<UnsupportedFeature> unsupportedFeatures) {
    public AgentModel {
        source = Objects.requireNonNull(source, "source").toAbsolutePath().normalize();
        parserVersion = Objects.requireNonNull(parserVersion, "parserVersion");
        requireNonNegative("beliefCount", beliefCount);
        requireNonNegative("goalCount", goalCount);
        requireNonNegative("planCount", planCount);
        beliefs = List.copyOf(Objects.requireNonNull(beliefs, "beliefs"));
        goals = List.copyOf(Objects.requireNonNull(goals, "goals"));
        plans = List.copyOf(Objects.requireNonNull(plans, "plans"));
        unsupportedFeatures = List.copyOf(Objects.requireNonNull(unsupportedFeatures, "unsupportedFeatures"));
    }

    public AgentModel(
            Path source,
            String parserVersion,
            int beliefCount,
            int goalCount,
            int planCount) {
        this(source, parserVersion, beliefCount, goalCount, planCount,
                List.of(), List.of(), List.of(), List.of());
    }

    public int elementCount() {
        return beliefCount + goalCount + planCount;
    }

    public boolean isMaterialized() {
        return beliefCount == beliefs.size()
                && goalCount == goals.size()
                && planCount == plans.size();
    }

    private static void requireNonNegative(String name, int value) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
    }
}
