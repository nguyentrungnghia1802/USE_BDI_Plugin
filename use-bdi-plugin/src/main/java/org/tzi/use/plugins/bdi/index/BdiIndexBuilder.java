package org.tzi.use.plugins.bdi.index;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.tzi.use.plugins.bdi.index.ActionCallSite.ActionKind;
import org.tzi.use.plugins.bdi.index.AgentObjectReference.ReferenceKind;
import org.tzi.use.plugins.bdi.index.PredicateReference.PredicateReferenceKind;
import org.tzi.use.plugins.bdi.model.ir.AchieveGoalStepModel;
import org.tzi.use.plugins.bdi.model.ir.ActionStepModel;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.model.ir.ArithmeticTermModel;
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
import org.tzi.use.plugins.bdi.model.ir.ListTermModel;
import org.tzi.use.plugins.bdi.model.ir.LiteralTermModel;
import org.tzi.use.plugins.bdi.model.ir.NumberTermModel;
import org.tzi.use.plugins.bdi.model.ir.PlanModel;
import org.tzi.use.plugins.bdi.model.ir.PlanStepModel;
import org.tzi.use.plugins.bdi.model.ir.SetTermModel;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.model.ir.StringTermModel;
import org.tzi.use.plugins.bdi.model.ir.TermModel;
import org.tzi.use.plugins.bdi.model.ir.TestStepModel;
import org.tzi.use.plugins.bdi.model.ir.TriggerModel;
import org.tzi.use.plugins.bdi.model.ir.UnsupportedTermModel;
import org.tzi.use.plugins.bdi.model.ir.VariableTermModel;

/** Builds all Phase 2 lookup indexes from the Jason-independent IR. */
public final class BdiIndexBuilder {
    public BdiIndex build(AgentModel model) {
        return build(List.of(Objects.requireNonNull(model, "model")));
    }

    public BdiIndex build(List<AgentModel> models) {
        List<AgentModel> immutableModels = List.copyOf(Objects.requireNonNull(models, "models"));
        MutableIndex index = new MutableIndex(immutableModels);
        immutableModels.forEach(index::visitModel);
        return index.freeze();
    }

    private static final class MutableIndex {
        private final List<AgentModel> models;
        private final Map<PredicateSignature, List<PlanModel>> supportingPlans = new LinkedHashMap<>();
        private final Map<PredicateSignature, List<ActionCallSite>> actionCallSites = new LinkedHashMap<>();
        private final List<ActionCallSite> allActionCallSites = new ArrayList<>();
        private final Map<PredicateSignature, List<PredicateReference>> predicateReferences = new LinkedHashMap<>();
        private final List<PredicateReference> allPredicateReferences = new ArrayList<>();
        private final Map<String, List<AgentObjectReference>> agentReferences = new LinkedHashMap<>();
        private final Map<String, List<AgentObjectReference>> objectReferences = new LinkedHashMap<>();
        private final List<DuplicatePlanLabel> duplicatePlanLabels = new ArrayList<>();

        private MutableIndex(List<AgentModel> models) {
            this.models = models;
        }

        private void visitModel(AgentModel model) {
            Map<String, List<SourceSpan>> labels = new LinkedHashMap<>();
            for (BeliefModel belief : model.beliefs()) {
                addLiteralReference(
                        belief.literal(),
                        PredicateReferenceKind.INITIAL_BELIEF,
                        "",
                        belief.sourceSpan());
            }
            for (GoalModel goal : model.goals()) {
                addLiteralReference(
                        goal.literal(),
                        PredicateReferenceKind.INITIAL_GOAL,
                        "",
                        goal.sourceSpan());
            }
            for (PlanModel plan : model.plans()) {
                if (!plan.label().isBlank()) {
                    labels.computeIfAbsent(plan.label(), ignored -> new ArrayList<>())
                            .add(plan.sourceSpan());
                }
                visitPlan(plan);
            }
            labels.forEach((label, occurrences) -> {
                if (occurrences.size() > 1) {
                    duplicatePlanLabels.add(new DuplicatePlanLabel(model.source(), label, occurrences));
                }
            });
        }

        private void visitPlan(PlanModel plan) {
            TriggerModel trigger = plan.trigger();
            addTermReference(
                    trigger.term(),
                    PredicateReferenceKind.PLAN_TRIGGER,
                    plan.label());
            if (trigger.term() instanceof LiteralTermModel literal
                    && trigger.type() == TriggerModel.TriggerType.ACHIEVE
                    && trigger.operator() == TriggerModel.TriggerOperator.ADD) {
                supportingPlans.computeIfAbsent(signature(literal), ignored -> new ArrayList<>()).add(plan);
            }

            plan.context().ifPresent(context -> visitContext(context, plan.label()));
            for (int index = 0; index < plan.steps().size(); index++) {
                visitStep(plan, index + 1, plan.steps().get(index));
            }
        }

        private void visitStep(PlanModel plan, int stepIndex, PlanStepModel step) {
            String label = plan.label();
            if (step instanceof ActionStepModel action) {
                addTermReference(action.action(), PredicateReferenceKind.ACTION, label);
                addActionCallSite(plan, stepIndex, ActionKind.EXTERNAL_ACTION, action.action());
                indexActionArguments(action.action(), "action argument");
                return;
            }
            if (step instanceof InternalActionStepModel action) {
                addTermReference(action.action(), PredicateReferenceKind.INTERNAL_ACTION, label);
                addActionCallSite(plan, stepIndex, ActionKind.INTERNAL_ACTION, action.action());
                indexInternalActionObjects(action.action());
                return;
            }
            if (step instanceof AchieveGoalStepModel goal) {
                addLiteralReference(goal.goal(), PredicateReferenceKind.ACHIEVE_GOAL, label, goal.sourceSpan());
                return;
            }
            if (step instanceof TestStepModel test) {
                visitContext(test.condition(), label, PredicateReferenceKind.TEST);
                return;
            }
            if (step instanceof ConstraintStepModel constraint) {
                visitContext(constraint.condition(), label, PredicateReferenceKind.CONSTRAINT);
                return;
            }
            if (step instanceof BeliefUpdateStepModel update) {
                addLiteralReference(update.belief(), PredicateReferenceKind.BELIEF_UPDATE, label, update.sourceSpan());
            }
        }

        private void addActionCallSite(
                PlanModel plan,
                int stepIndex,
                ActionKind kind,
                TermModel action) {
            Optional<PredicateSignature> signature = signatureOf(action);
            ActionCallSite callSite = new ActionCallSite(
                    plan.label(),
                    stepIndex,
                    kind,
                    action.render(),
                    signature,
                    action.sourceSpan());
            allActionCallSites.add(callSite);
            signature.ifPresent(key -> actionCallSites.computeIfAbsent(key, ignored -> new ArrayList<>()).add(callSite));
        }

        private void visitContext(ContextExpr context, String planLabel) {
            visitContext(context, planLabel, PredicateReferenceKind.PLAN_CONTEXT);
        }

        private void visitContext(ContextExpr context, String planLabel, PredicateReferenceKind kind) {
            if (context instanceof ContextLiteral literal) {
                addLiteralReference(literal.literal(), kind, planLabel, literal.sourceSpan());
            } else if (context instanceof ContextUnary unary) {
                visitContext(unary.operand(), planLabel, kind);
            } else if (context instanceof ContextBinary binary) {
                visitContext(binary.left(), planLabel, kind);
                visitContext(binary.right(), planLabel, kind);
            } else if (context instanceof ContextUnsupported) {
                // The IR retains the ASL-002 evidence; there is no predicate to index.
            }
        }

        private void addLiteralReference(
                LiteralTermModel literal,
                PredicateReferenceKind kind,
                String planLabel,
                SourceSpan sourceSpan) {
            addPredicateReference(new PredicateReference(
                    signature(literal),
                    kind,
                    planLabel,
                    literal.render(),
                    sourceSpan));
            indexObjectTerms(literal.arguments(), "predicate argument");
            indexObjectTerms(literal.annotations(), "predicate annotation");
        }

        private void addTermReference(TermModel term, PredicateReferenceKind kind, String planLabel) {
            signatureOf(term).ifPresent(signature -> addPredicateReference(new PredicateReference(
                    signature,
                    kind,
                    planLabel,
                    term.render(),
                    term.sourceSpan())));
        }

        private void addPredicateReference(PredicateReference reference) {
            allPredicateReferences.add(reference);
            predicateReferences.computeIfAbsent(reference.signature(), ignored -> new ArrayList<>()).add(reference);
        }

        private void indexInternalActionObjects(TermModel action) {
            if (action instanceof LiteralTermModel literal && literal.functor().equals(".send")
                    && !literal.arguments().isEmpty()) {
                TermModel receiver = literal.arguments().get(0);
                addAgentReference(receiver, "send receiver");
                indexObjectTerms(literal.arguments().subList(1, literal.arguments().size()), "send argument");
                indexObjectTerms(literal.annotations(), "internal-action annotation");
                return;
            }
            indexActionArguments(action, "internal-action argument");
        }

        private void indexActionArguments(TermModel action, String origin) {
            if (action instanceof LiteralTermModel literal) {
                indexObjectTerms(literal.arguments(), origin);
                indexObjectTerms(literal.annotations(), origin);
            } else if (action instanceof CompoundTermModel compound) {
                indexObjectTerms(compound.arguments(), origin);
            } else {
                indexObjectTerms(action, origin);
            }
        }

        private void addAgentReference(TermModel receiver, String origin) {
            boolean dynamic = receiver instanceof VariableTermModel;
            AgentObjectReference reference = new AgentObjectReference(
                    ReferenceKind.AGENT,
                    receiver.render(),
                    receiver.render(),
                    dynamic,
                    origin,
                    receiver.sourceSpan());
            agentReferences.computeIfAbsent(reference.name(), ignored -> new ArrayList<>()).add(reference);
        }

        private void indexObjectTerms(TermModel term, String origin) {
            if (term == null) {
                return;
            }
            if (term instanceof LiteralTermModel literal) {
                addObjectReference(literal, origin);
                literal.arguments().forEach(argument -> indexObjectTerms(argument, origin));
                literal.annotations().forEach(annotation -> indexObjectTerms(annotation, origin));
            } else if (term instanceof CompoundTermModel compound) {
                addObjectReference(compound, origin);
                compound.arguments().forEach(argument -> indexObjectTerms(argument, origin));
            } else if (term instanceof ListTermModel list) {
                list.elements().forEach(element -> indexObjectTerms(element, origin));
                list.tail().ifPresent(tail -> indexObjectTerms(tail, origin));
            } else if (term instanceof SetTermModel set) {
                set.elements().forEach(element -> indexObjectTerms(element, origin));
            } else if (term instanceof ArithmeticTermModel arithmetic) {
                arithmetic.left().ifPresent(left -> indexObjectTerms(left, origin));
                arithmetic.right().ifPresent(right -> indexObjectTerms(right, origin));
            } else if (term instanceof StringTermModel || term instanceof NumberTermModel
                    || term instanceof VariableTermModel || term instanceof UnsupportedTermModel) {
                // Strings, numbers, variables, and ASL-002 placeholders are not named objects.
            }
        }

        private void indexObjectTerms(List<TermModel> terms, String origin) {
            terms.forEach(term -> indexObjectTerms(term, origin));
        }

        private void addObjectReference(TermModel term, String origin) {
            String rendered = term.render();
            String name = term instanceof LiteralTermModel literal && literal.arguments().isEmpty()
                    ? literal.functor()
                    : rendered;
            AgentObjectReference reference = new AgentObjectReference(
                    ReferenceKind.OBJECT,
                    name,
                    rendered,
                    false,
                    origin,
                    term.sourceSpan());
            objectReferences.computeIfAbsent(reference.name(), ignored -> new ArrayList<>()).add(reference);
        }

        private static Optional<PredicateSignature> signatureOf(TermModel term) {
            if (term instanceof LiteralTermModel literal) {
                return Optional.of(signature(literal));
            }
            if (term instanceof CompoundTermModel compound) {
                return Optional.of(new PredicateSignature(compound.functor(), compound.arguments().size()));
            }
            return Optional.empty();
        }

        private static PredicateSignature signature(LiteralTermModel literal) {
            return new PredicateSignature(literal.functor(), literal.arguments().size());
        }

        private BdiIndex freeze() {
            return new BdiIndex(
                    BdiMetamodelVersion.CURRENT,
                    models,
                    supportingPlans,
                    actionCallSites,
                    allActionCallSites,
                    predicateReferences,
                    allPredicateReferences,
                    agentReferences,
                    objectReferences,
                    duplicatePlanLabels);
        }
    }
}
