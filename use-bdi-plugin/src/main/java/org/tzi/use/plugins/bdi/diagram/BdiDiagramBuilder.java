package org.tzi.use.plugins.bdi.diagram;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshot;
import org.tzi.use.plugins.bdi.index.ActionCallSite;
import org.tzi.use.plugins.bdi.index.AgentObjectReference;
import org.tzi.use.plugins.bdi.index.PredicateSignature;
import org.tzi.use.plugins.bdi.model.ir.AchieveGoalStepModel;
import org.tzi.use.plugins.bdi.model.ir.ActionStepModel;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.model.ir.BeliefModel;
import org.tzi.use.plugins.bdi.model.ir.BeliefUpdateStepModel;
import org.tzi.use.plugins.bdi.model.ir.CompoundTermModel;
import org.tzi.use.plugins.bdi.model.ir.ConstraintStepModel;
import org.tzi.use.plugins.bdi.model.ir.ContextBinary;
import org.tzi.use.plugins.bdi.model.ir.ContextExpr;
import org.tzi.use.plugins.bdi.model.ir.ContextLiteral;
import org.tzi.use.plugins.bdi.model.ir.ContextUnary;
import org.tzi.use.plugins.bdi.model.ir.ContextUnsupported;
import org.tzi.use.plugins.bdi.model.ir.GoalModel;
import org.tzi.use.plugins.bdi.model.ir.InternalActionStepModel;
import org.tzi.use.plugins.bdi.model.ir.LiteralTermModel;
import org.tzi.use.plugins.bdi.model.ir.PlanModel;
import org.tzi.use.plugins.bdi.model.ir.PlanStepModel;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.model.ir.TermModel;
import org.tzi.use.plugins.bdi.model.ir.TestStepModel;
import org.tzi.use.plugins.bdi.model.ir.TriggerModel;
import org.tzi.use.plugins.bdi.model.ir.UnsupportedStepModel;
import org.tzi.use.plugins.bdi.model.mapping.MappingBinding;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;
import org.tzi.use.plugins.bdi.model.mapping.MappingSourceId;
import org.tzi.use.plugins.bdi.model.source.ProjectSourceId;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;

/** Projects one immutable analysis snapshot into the renderer-neutral BDI diagram. */
public final class BdiDiagramBuilder {
    public DiagramModel build(CurrentAnalysisSnapshot snapshot, Path projectRoot) {
        Objects.requireNonNull(snapshot, "snapshot");
        Path root = Objects.requireNonNull(projectRoot, "projectRoot").toAbsolutePath().normalize();
        Projection projection = new Projection(snapshot, root);
        snapshot.bdiImport().models().forEach(projection::addAgentSource);
        projection.addSupportingPlanEdges();
        return projection.freeze();
    }

    private static final class Projection {
        private final CurrentAnalysisSnapshot snapshot;
        private final Path root;
        private final Map<String, DiagramNode> nodes = new LinkedHashMap<>();
        private final Map<String, DiagramEdge> edges = new LinkedHashMap<>();
        private final List<DiagramGroup> groups = new ArrayList<>();
        private final Map<PlanModel, String> planNodeByPlan = new IdentityHashMap<>();
        private final List<GoalNode> goals = new ArrayList<>();

        private Projection(CurrentAnalysisSnapshot snapshot, Path root) {
            this.snapshot = snapshot;
            this.root = root;
        }

        private void addAgentSource(AgentModel model) {
            ProjectSourceId fileSource = ProjectSourceId.fromPath(root, model.source());
            DiagramNode agent = addNode(new DiagramNode(
                    DiagramNodeType.AGENT,
                    DiagramSelectionRef.source(fileSource),
                    model.source().getFileName().toString(),
                    Optional.of(fileSource),
                    Optional.empty(),
                    Map.of("kind", "AGENT_SOURCE")));
            Set<String> groupNodeIds = new LinkedHashSet<>();
            groupNodeIds.add(agent.id());

            addAgentMappings(model, agent, groupNodeIds);
            for (int index = 0; index < model.beliefs().size(); index++) {
                addInitialBelief(agent, model.beliefs().get(index), index + 1, groupNodeIds);
            }
            for (int index = 0; index < model.goals().size(); index++) {
                addInitialGoal(agent, model.goals().get(index), index + 1, groupNodeIds);
            }
            for (int index = 0; index < model.plans().size(); index++) {
                addPlan(agent, model.plans().get(index), index + 1, groupNodeIds);
            }

            groups.add(new DiagramGroup(
                    DiagramSelectionRef.of("agent-group", fileSource.canonical()),
                    model.source().getFileName().toString(),
                    List.copyOf(groupNodeIds),
                    Map.of("source", fileSource.projectPath())));
        }

        private void addAgentMappings(AgentModel model, DiagramNode agent, Set<String> groupNodeIds) {
            String source = MappingSourceId.agent(model);
            boolean classMapped = addMappingIfPresent(
                    agent, MappingKind.AGENT_CLASS, source, DiagramNodeType.UML_CLASS, groupNodeIds);
            boolean objectMapped = addMappingIfPresent(
                    agent, MappingKind.AGENT_OBJECT, source, DiagramNodeType.UML_OBJECT, groupNodeIds);
            if (!classMapped && !objectMapped) {
                addGap(agent, "AGENT_CLASS_OR_OBJECT", "Missing agent mapping", groupNodeIds);
            }
        }

        private void addInitialBelief(
                DiagramNode agent,
                BeliefModel belief,
                int beliefIndex,
                Set<String> groupNodeIds) {
            DiagramNode node = addSourceNode(
                    DiagramNodeType.BELIEF,
                    belief.literal().render(),
                    belief.sourceSpan(),
                    "belief-" + beliefIndex,
                    Map.of("kind", "INITIAL_BELIEF"));
            groupNodeIds.add(node.id());
            addEdge(DiagramEdgeType.HAS_BELIEF, agent, node, "initial-belief", Optional.empty(), Map.of());
            PredicateSignature signature = signature(belief.literal());
            addMappingOrGap(
                    node,
                    MappingKind.BELIEF_ATTRIBUTE,
                    MappingSourceId.belief(signature),
                    DiagramNodeType.UML_ATTRIBUTE,
                    "Missing belief mapping",
                    groupNodeIds);
        }

        private void addInitialGoal(
                DiagramNode agent,
                GoalModel goal,
                int goalIndex,
                Set<String> groupNodeIds) {
            DiagramNode node = addSourceNode(
                    DiagramNodeType.GOAL,
                    goal.literal().render(),
                    goal.sourceSpan(),
                    "goal-" + goalIndex,
                    Map.of("kind", "INITIAL_GOAL"));
            groupNodeIds.add(node.id());
            addEdge(DiagramEdgeType.PURSUES_GOAL, agent, node, "initial-goal", Optional.empty(), Map.of());
            goals.add(new GoalNode(signature(goal.literal()), node));
        }

        private void addPlan(
                DiagramNode agent,
                PlanModel plan,
                int planIndex,
                Set<String> groupNodeIds) {
            DiagramNode planNode = addSourceNode(
                    DiagramNodeType.PLAN,
                    planLabel(plan),
                    plan.sourceSpan(),
                    "plan-" + planIndex,
                    Map.of("kind", "PLAN"));
            groupNodeIds.add(planNode.id());
            planNodeByPlan.put(plan, planNode.id());
            addEdge(DiagramEdgeType.OWNS, agent, planNode, "plan", Optional.empty(), Map.of());

            TriggerModel trigger = plan.trigger();
            DiagramNode triggerNode = addSourceNode(
                    DiagramNodeType.TRIGGER,
                    trigger.operator() + " " + trigger.type() + " " + trigger.term().render(),
                    trigger.sourceSpan(),
                    "plan-" + planIndex + "-trigger",
                    Map.of("kind", "TRIGGER"));
            groupNodeIds.add(triggerNode.id());
            addEdge(DiagramEdgeType.TRIGGERED_BY, planNode, triggerNode, "trigger", Optional.empty(), Map.of());

            plan.context().ifPresent(context -> {
                DiagramNode contextNode = addSourceNode(
                        DiagramNodeType.CONTEXT,
                        renderContext(context),
                        context.sourceSpan(),
                        "plan-" + planIndex + "-context",
                        Map.of("kind", "PLAN_CONTEXT"));
                groupNodeIds.add(contextNode.id());
                addEdge(DiagramEdgeType.REQUIRES_CONTEXT,
                        planNode, contextNode, "context", Optional.empty(), Map.of());
            });

            for (int index = 0; index < plan.steps().size(); index++) {
                addStep(agent, planNode, plan, planIndex, index + 1, plan.steps().get(index), groupNodeIds);
            }
        }

        private void addStep(
                DiagramNode agent,
                DiagramNode planNode,
                PlanModel plan,
                int planIndex,
                int stepIndex,
                PlanStepModel step,
                Set<String> groupNodeIds) {
            DiagramNode stepNode = addSourceNode(
                    stepType(step),
                    stepLabel(step),
                    step.sourceSpan(),
                    "plan-" + planIndex + "-step-" + stepIndex,
                    Map.of("kind", stepKind(step), "order", Integer.toString(stepIndex)));
            groupNodeIds.add(stepNode.id());
            addEdge(
                    DiagramEdgeType.EXECUTES,
                    planNode,
                    stepNode,
                    "step-" + stepIndex,
                    Optional.of(Integer.toString(stepIndex)),
                    Map.of("order", Integer.toString(stepIndex)));

            if (step instanceof AchieveGoalStepModel goal) {
                goals.add(new GoalNode(signature(goal.goal()), stepNode));
            } else if (step instanceof BeliefUpdateStepModel update) {
                addMappingOrGap(
                        stepNode,
                        MappingKind.BELIEF_ATTRIBUTE,
                        MappingSourceId.belief(signature(update.belief())),
                        DiagramNodeType.UML_ATTRIBUTE,
                        "Missing belief mapping",
                        groupNodeIds);
            } else if (isMessage(step)) {
                addEdge(DiagramEdgeType.SENDS_MESSAGE,
                        agent, stepNode, "message-" + stepIndex, Optional.empty(), Map.of());
                addMessageMappingOrGap(stepNode, step, groupNodeIds);
            } else if (step instanceof ActionStepModel) {
                addActionMappingOrGap(stepNode, plan, stepIndex, groupNodeIds);
            } else if (step instanceof InternalActionStepModel) {
                addActionMappingIfPresent(stepNode, plan, stepIndex, groupNodeIds);
            }
        }

        private void addActionMappingOrGap(
                DiagramNode actionNode,
                PlanModel plan,
                int stepIndex,
                Set<String> groupNodeIds) {
            Optional<ActionCallSite> action = actionCall(plan, stepIndex);
            if (action.isPresent()) {
                addMappingOrGap(
                        actionNode,
                        MappingKind.ACTION_OPERATION,
                        MappingSourceId.action(action.orElseThrow()),
                        DiagramNodeType.UML_OPERATION,
                        "Missing action mapping",
                        groupNodeIds);
            } else {
                addGap(actionNode, MappingKind.ACTION_OPERATION.name(), "Missing action mapping", groupNodeIds);
            }
        }

        private void addActionMappingIfPresent(
                DiagramNode actionNode,
                PlanModel plan,
                int stepIndex,
                Set<String> groupNodeIds) {
            actionCall(plan, stepIndex).ifPresent(action -> addMappingIfPresent(
                    actionNode,
                    MappingKind.ACTION_OPERATION,
                    MappingSourceId.action(action),
                    DiagramNodeType.UML_OPERATION,
                    groupNodeIds));
        }

        private void addMessageMappingOrGap(
                DiagramNode messageNode,
                PlanStepModel step,
                Set<String> groupNodeIds) {
            Optional<AgentObjectReference> receiver = snapshot.bdiImport().index()
                    .agentReferencesByName().values().stream()
                    .flatMap(List::stream)
                    .filter(reference -> reference.kind() == AgentObjectReference.ReferenceKind.AGENT)
                    .filter(reference -> "send receiver".equals(reference.origin()))
                    .filter(reference -> contains(step.sourceSpan(), reference.sourceSpan()))
                    .findFirst();
            if (receiver.isPresent()) {
                addMappingOrGap(
                        messageNode,
                        MappingKind.RECEIVER_OBJECT,
                        MappingSourceId.receiver(receiver.orElseThrow()),
                        DiagramNodeType.UML_OBJECT,
                        "Missing message receiver mapping",
                        groupNodeIds);
            } else {
                addGap(messageNode,
                        MappingKind.RECEIVER_OBJECT.name(),
                        "Unknown message receiver mapping",
                        groupNodeIds);
            }
        }

        private Optional<ActionCallSite> actionCall(PlanModel plan, int stepIndex) {
            return snapshot.bdiImport().index().allActionCallSites().stream()
                    .filter(action -> action.stepIndex() == stepIndex)
                    .filter(action -> MappingSourceId.stablePlanLabel(action.planLabel())
                            .equals(MappingSourceId.stablePlanLabel(plan.label())))
                    .filter(action -> action.sourceSpan().source().equals(plan.sourceSpan().source()))
                    .findFirst();
        }

        private void addSupportingPlanEdges() {
            for (GoalNode goal : goals) {
                for (PlanModel plan : snapshot.bdiImport().index().supportingPlans(goal.signature())) {
                    String planNodeId = planNodeByPlan.get(plan);
                    if (planNodeId != null) {
                        addEdge(
                                DiagramEdgeType.SUPPORTED_BY,
                                goal.node(),
                                nodes.get(planNodeId),
                                "supporting-plan",
                                Optional.empty(),
                                Map.of("signature", goal.signature().toString()));
                    }
                }
            }
        }

        private void addMappingOrGap(
                DiagramNode sourceNode,
                MappingKind kind,
                String source,
                DiagramNodeType targetType,
                String gapLabel,
                Set<String> groupNodeIds) {
            if (!addMappingIfPresent(sourceNode, kind, source, targetType, groupNodeIds)) {
                addGap(sourceNode, kind.name(), gapLabel, groupNodeIds);
            }
        }

        private boolean addMappingIfPresent(
                DiagramNode sourceNode,
                MappingKind kind,
                String source,
                DiagramNodeType targetType,
                Set<String> groupNodeIds) {
            Optional<MappingBinding> binding = snapshot.mapping().find(kind, source);
            if (binding.isEmpty()) {
                return false;
            }
            MappingBinding confirmed = binding.orElseThrow();
            String targetState = targetState(kind, confirmed.target());
            DiagramNode target = addNode(new DiagramNode(
                    targetType,
                    DiagramSelectionRef.of("uml", confirmed.target()),
                    confirmed.target(),
                    Optional.empty(),
                    Optional.empty(),
                    Map.of(
                            "mappingKind", kind.name(),
                            "mappingStatus", "CONFIRMED",
                            "targetState", targetState)));
            addEdge(
                    DiagramEdgeType.MAPS_TO,
                    sourceNode,
                    target,
                    "mapping-" + kind + "-" + confirmed.target(),
                    Optional.of(kind.name()),
                    Map.of("mappingStatus", "CONFIRMED", "targetState", targetState));
            return true;
        }

        private void addGap(
                DiagramNode sourceNode,
                String mappingKind,
                String label,
                Set<String> groupNodeIds) {
            DiagramNode gap = addNode(new DiagramNode(
                    DiagramNodeType.GAP,
                    DiagramSelectionRef.of("mapping-gap", sourceNode.id() + "#" + mappingKind),
                    label,
                    sourceNode.source(),
                    Optional.empty(),
                    Map.of("mappingKind", mappingKind, "mappingStatus", "MISSING")));
            groupNodeIds.add(gap.id());
            addEdge(
                    DiagramEdgeType.MISSING_MAPPING,
                    sourceNode,
                    gap,
                    "missing-" + mappingKind,
                    Optional.of(mappingKind),
                    Map.of("mappingStatus", "MISSING"));
        }

        private String targetState(MappingKind kind, String target) {
            Optional<UseModelSnapshot> useModel = snapshot.useModel();
            if (useModel.isEmpty()) {
                return "UNKNOWN";
            }
            UseModelSnapshot model = useModel.orElseThrow();
            boolean current = switch (kind) {
                case AGENT_CLASS -> model.classes().stream().anyMatch(value -> value.reference().equals(target));
                case AGENT_OBJECT, RECEIVER_OBJECT -> model.objects().stream()
                        .anyMatch(value -> value.reference().equals(target));
                case ACTION_OPERATION -> model.operations().stream()
                        .anyMatch(value -> value.reference().equals(target));
                case BELIEF_ATTRIBUTE -> model.attributes().stream()
                        .anyMatch(value -> value.reference().equals(target));
                case PARAMETER -> model.operations().stream()
                        .anyMatch(value -> target.startsWith(value.reference() + "#parameter:"));
            };
            return current ? "CURRENT" : "STALE";
        }

        private DiagramNode addSourceNode(
                DiagramNodeType type,
                String label,
                SourceSpan span,
                String semanticKey,
                Map<String, String> attributes) {
            ProjectSourceId source = portable(span);
            return addNode(new DiagramNode(
                    type,
                    DiagramSelectionRef.of("bdi-element", source.canonical() + "#" + semanticKey),
                    label,
                    Optional.of(source),
                    Optional.empty(),
                    attributes));
        }

        private DiagramNode addNode(DiagramNode node) {
            DiagramNode existing = nodes.putIfAbsent(node.id(), node);
            if (existing != null && !existing.equals(node)) {
                throw new IllegalArgumentException("Conflicting diagram node identity: " + node.id());
            }
            return existing == null ? node : existing;
        }

        private void addEdge(
                DiagramEdgeType type,
                DiagramNode source,
                DiagramNode target,
                String relationKey,
                Optional<String> label,
                Map<String, String> attributes) {
            DiagramEdge edge = new DiagramEdge(
                    type,
                    source.id(),
                    target.id(),
                    DiagramSelectionRef.of("bdi-relation", source.id() + "#" + relationKey + "#" + target.id()),
                    label,
                    attributes);
            DiagramEdge existing = edges.putIfAbsent(edge.id(), edge);
            if (existing != null && !existing.equals(edge)) {
                throw new IllegalArgumentException("Conflicting diagram edge identity: " + edge.id());
            }
        }

        private ProjectSourceId portable(SourceSpan span) {
            return ProjectSourceId.from(root, span);
        }

        private DiagramModel freeze() {
            return new DiagramModel(List.copyOf(nodes.values()), List.copyOf(edges.values()), List.copyOf(groups));
        }
    }

    private static DiagramNodeType stepType(PlanStepModel step) {
        if (step instanceof AchieveGoalStepModel) {
            return DiagramNodeType.GOAL;
        }
        if (step instanceof BeliefUpdateStepModel) {
            return DiagramNodeType.BELIEF;
        }
        if (step instanceof TestStepModel || step instanceof ConstraintStepModel) {
            return DiagramNodeType.CONTEXT;
        }
        if (isMessage(step)) {
            return DiagramNodeType.MESSAGE;
        }
        return DiagramNodeType.ACTION;
    }

    private static String stepKind(PlanStepModel step) {
        if (step instanceof ActionStepModel) {
            return "EXTERNAL_ACTION";
        }
        if (step instanceof InternalActionStepModel) {
            return isMessage(step) ? "MESSAGE" : "INTERNAL_ACTION";
        }
        if (step instanceof AchieveGoalStepModel) {
            return "ACHIEVE_GOAL";
        }
        if (step instanceof BeliefUpdateStepModel) {
            return "BELIEF_UPDATE";
        }
        if (step instanceof TestStepModel) {
            return "TEST";
        }
        if (step instanceof ConstraintStepModel) {
            return "CONSTRAINT";
        }
        return "UNSUPPORTED";
    }

    private static String stepLabel(PlanStepModel step) {
        if (step instanceof ActionStepModel action) {
            return action.action().render();
        }
        if (step instanceof InternalActionStepModel action) {
            return action.action().render();
        }
        if (step instanceof AchieveGoalStepModel goal) {
            return "!" + goal.goal().render();
        }
        if (step instanceof BeliefUpdateStepModel update) {
            return update.operator() + " " + update.belief().render();
        }
        if (step instanceof TestStepModel test) {
            return "?" + renderContext(test.condition());
        }
        if (step instanceof ConstraintStepModel constraint) {
            return renderContext(constraint.condition());
        }
        if (step instanceof UnsupportedStepModel unsupported) {
            return unsupported.feature().message();
        }
        throw new IllegalArgumentException("Unknown plan step: " + step.getClass().getName());
    }

    private static boolean isMessage(PlanStepModel step) {
        if (!(step instanceof InternalActionStepModel internal)) {
            return false;
        }
        return functor(internal.action()).map(".send"::equals).orElse(false);
    }

    private static Optional<String> functor(TermModel term) {
        if (term instanceof LiteralTermModel literal) {
            return Optional.of(literal.functor());
        }
        if (term instanceof CompoundTermModel compound) {
            return Optional.of(compound.functor());
        }
        return Optional.empty();
    }

    private static String renderContext(ContextExpr context) {
        if (context instanceof ContextLiteral literal) {
            return literal.literal().render();
        }
        if (context instanceof ContextUnary unary) {
            return unary.operator() + " " + renderContext(unary.operand());
        }
        if (context instanceof ContextBinary binary) {
            return "(" + renderContext(binary.left()) + " " + binary.operator() + " "
                    + renderContext(binary.right()) + ")";
        }
        if (context instanceof ContextUnsupported unsupported) {
            return unsupported.feature().message();
        }
        throw new IllegalArgumentException("Unknown context expression: " + context.getClass().getName());
    }

    private static String planLabel(PlanModel plan) {
        String stable = MappingSourceId.stablePlanLabel(plan.label());
        return stable.isBlank() ? plan.trigger().term().render() : stable;
    }

    private static PredicateSignature signature(LiteralTermModel literal) {
        return new PredicateSignature(literal.functor(), literal.arguments().size());
    }

    private static boolean contains(SourceSpan outer, SourceSpan inner) {
        if (!outer.source().equals(inner.source())) {
            return false;
        }
        if (outer.beginLine() == 0 || inner.beginLine() == 0) {
            return outer.equals(inner);
        }
        return inner.beginLine() >= outer.beginLine() && inner.endLine() <= outer.endLine();
    }

    private record GoalNode(PredicateSignature signature, DiagramNode node) {
    }
}
