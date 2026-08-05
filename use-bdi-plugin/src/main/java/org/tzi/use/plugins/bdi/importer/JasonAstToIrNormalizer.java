package org.tzi.use.plugins.bdi.importer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
import org.tzi.use.plugins.bdi.model.ir.LiteralTermModel;
import org.tzi.use.plugins.bdi.model.ir.ListTermModel;
import org.tzi.use.plugins.bdi.model.ir.NumberTermModel;
import org.tzi.use.plugins.bdi.model.ir.PlanModel;
import org.tzi.use.plugins.bdi.model.ir.PlanStepModel;
import org.tzi.use.plugins.bdi.model.ir.SetTermModel;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.model.ir.StringTermModel;
import org.tzi.use.plugins.bdi.model.ir.TermModel;
import org.tzi.use.plugins.bdi.model.ir.TestStepModel;
import org.tzi.use.plugins.bdi.model.ir.TriggerModel;
import org.tzi.use.plugins.bdi.model.ir.UnsupportedFeature;
import org.tzi.use.plugins.bdi.model.ir.UnsupportedStepModel;
import org.tzi.use.plugins.bdi.model.ir.UnsupportedTermModel;
import org.tzi.use.plugins.bdi.model.ir.VariableTermModel;

import jason.asSemantics.Agent;
import jason.asSyntax.ArithExpr;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.LogExpr;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Plan;
import jason.asSyntax.PlanBody;
import jason.asSyntax.SetTerm;
import jason.asSyntax.SourceInfo;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.Trigger;
import jason.asSyntax.VarTerm;

/** Package-private Jason boundary that materializes the normalized IR tree. */
final class JasonAstToIrNormalizer {
    private final Path source;
    private final String parserVersion;
    private final List<UnsupportedFeature> unsupportedFeatures = new ArrayList<>();

    JasonAstToIrNormalizer(Path source, String parserVersion) {
        this.source = Objects.requireNonNull(source, "source").toAbsolutePath().normalize();
        this.parserVersion = Objects.requireNonNull(parserVersion, "parserVersion");
    }

    AgentModel normalize(Agent agent) {
        Objects.requireNonNull(agent, "agent");
        List<BeliefModel> beliefs = agent.getInitialBels().stream()
                .map(this::belief)
                .toList();
        List<GoalModel> goals = agent.getInitialGoals().stream()
                .map(this::goal)
                .toList();
        List<PlanModel> plans = agent.getPL().getPlans().stream()
                .map(this::plan)
                .toList();
        return new AgentModel(
                source,
                parserVersion,
                beliefs.size(),
                goals.size(),
                plans.size(),
                beliefs,
                goals,
                plans,
                unsupportedFeatures);
    }

    private BeliefModel belief(Literal literal) {
        return new BeliefModel(literal(literal), span(literal));
    }

    private GoalModel goal(Literal literal) {
        return new GoalModel(literal(literal), span(literal));
    }

    private PlanModel plan(Plan plan) {
        TriggerModel trigger = trigger(plan.getTrigger());
        Optional<ContextExpr> context = Optional.ofNullable(plan.getContext()).map(this::context);
        List<PlanStepModel> steps = body(plan.getBody());
        String label = sourceLabel(plan);
        return new PlanModel(label, trigger, context, steps, span(plan));
    }

    private String sourceLabel(Plan plan) {
        if (plan.getLabel() == null) {
            return "";
        }
        String label = plan.getLabel().toString();
        int bracket = label.indexOf('[');
        String prefix = bracket < 0 ? label : label.substring(0, bracket);
        if (prefix.startsWith("p__") && prefix.substring(3).chars().allMatch(Character::isDigit)) {
            return "";
        }
        return label;
    }

    private TriggerModel trigger(Trigger trigger) {
        if (trigger == null) {
            return new TriggerModel(
                    TriggerModel.TriggerOperator.GOAL_STATE,
                    TriggerModel.TriggerType.BELIEF,
                    unsupportedTerm("trigger", "<null>"),
                    SourceSpan.unknown(source));
        }
        TriggerModel.TriggerOperator operator = switch (trigger.getOperator()) {
            case add -> TriggerModel.TriggerOperator.ADD;
            case del -> TriggerModel.TriggerOperator.DELETE;
            case goalState -> TriggerModel.TriggerOperator.GOAL_STATE;
            case null -> {
                unsupported("trigger-operator", trigger.toString(), span(trigger));
                yield TriggerModel.TriggerOperator.GOAL_STATE;
            }
        };
        TriggerModel.TriggerType type = switch (trigger.getType()) {
            case belief -> TriggerModel.TriggerType.BELIEF;
            case achieve -> TriggerModel.TriggerType.ACHIEVE;
            case test -> TriggerModel.TriggerType.TEST;
            case signal -> TriggerModel.TriggerType.SIGNAL;
            case null -> {
                unsupported("trigger-type", trigger.toString(), span(trigger));
                yield TriggerModel.TriggerType.BELIEF;
            }
        };
        TermModel term = trigger.getLiteral() == null
                ? unsupportedTerm("trigger-literal", trigger.toString())
                : literal(trigger.getLiteral());
        return new TriggerModel(operator, type, term, span(trigger));
    }

    private List<PlanStepModel> body(PlanBody head) {
        List<PlanStepModel> steps = new ArrayList<>();
        Set<PlanBody> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (PlanBody current = head; current != null; current = current.getBodyNext()) {
            if (!seen.add(current)) {
                steps.add(unsupportedStep("cyclic-plan-body", current.toString(), span(current)));
                break;
            }
            if (!current.isEmptyBody()) {
                steps.add(step(current));
            }
        }
        return List.copyOf(steps);
    }

    private PlanStepModel step(PlanBody body) {
        SourceSpan sourceSpan = span(body);
        Term term = body.getBodyTerm();
        if (body.getBodyType() == null) {
            return unsupportedStep("plan-step-type", body.toString(), sourceSpan);
        }
        return switch (body.getBodyType()) {
            case action -> actionStep(term, sourceSpan);
            case internalAction -> internalActionStep(term, sourceSpan);
            case achieve -> achieveStep(term, false, sourceSpan);
            case achieveNF -> achieveStep(term, true, sourceSpan);
            case test -> new TestStepModel(contextFromTerm(term, "test"), sourceSpan);
            case constraint -> new ConstraintStepModel(contextFromTerm(term, "constraint"), sourceSpan);
            case addBel -> beliefUpdate(term, BeliefUpdateStepModel.UpdateOperator.ADD,
                    BeliefUpdateStepModel.FocusPolicy.DEFAULT, sourceSpan);
            case addBelNewFocus -> beliefUpdate(term, BeliefUpdateStepModel.UpdateOperator.ADD,
                    BeliefUpdateStepModel.FocusPolicy.NEW_FOCUS, sourceSpan);
            case addBelBegin -> beliefUpdate(term, BeliefUpdateStepModel.UpdateOperator.ADD,
                    BeliefUpdateStepModel.FocusPolicy.BEGIN_FOCUS, sourceSpan);
            case addBelEnd -> beliefUpdate(term, BeliefUpdateStepModel.UpdateOperator.ADD,
                    BeliefUpdateStepModel.FocusPolicy.END_FOCUS, sourceSpan);
            case delBel -> beliefUpdate(term, BeliefUpdateStepModel.UpdateOperator.DELETE,
                    BeliefUpdateStepModel.FocusPolicy.DEFAULT, sourceSpan);
            case delBelNewFocus -> beliefUpdate(term, BeliefUpdateStepModel.UpdateOperator.DELETE,
                    BeliefUpdateStepModel.FocusPolicy.NEW_FOCUS, sourceSpan);
            case delAddBel -> beliefUpdate(term, BeliefUpdateStepModel.UpdateOperator.DELETE_AND_ADD,
                    BeliefUpdateStepModel.FocusPolicy.DEFAULT, sourceSpan);
            case none -> unsupportedStep("empty-plan-step", body.toString(), sourceSpan);
        };
    }

    private PlanStepModel actionStep(Term term, SourceSpan sourceSpan) {
        return term == null
                ? unsupportedStep("action-term", "<null>", sourceSpan)
                : new ActionStepModel(term(term), sourceSpan);
    }

    private PlanStepModel internalActionStep(Term term, SourceSpan sourceSpan) {
        return term == null
                ? unsupportedStep("internal-action-term", "<null>", sourceSpan)
                : new InternalActionStepModel(term(term), sourceSpan);
    }

    private PlanStepModel achieveStep(Term term, boolean newFocus, SourceSpan sourceSpan) {
        if (term instanceof Literal literal) {
            return new AchieveGoalStepModel(literal(literal), newFocus, sourceSpan);
        }
        return unsupportedStep("achievement-term", subject(term), sourceSpan);
    }

    private PlanStepModel beliefUpdate(
            Term term,
            BeliefUpdateStepModel.UpdateOperator operator,
            BeliefUpdateStepModel.FocusPolicy focusPolicy,
            SourceSpan sourceSpan) {
        if (term instanceof Literal literal) {
            return new BeliefUpdateStepModel(operator, focusPolicy, literal(literal), sourceSpan);
        }
        return unsupportedStep("belief-update-term", subject(term), sourceSpan);
    }

    private ContextExpr contextFromTerm(Term term, String kind) {
        if (term instanceof LogicalFormula formula) {
            return context(formula);
        }
        return contextUnsupported(kind + "-term", subject(term), span(term));
    }

    private ContextExpr context(LogicalFormula formula) {
        SourceSpan sourceSpan = span(formula);
        if (formula instanceof Literal literal) {
            return new ContextLiteral(literal(literal), sourceSpan);
        }
        if (formula instanceof LogExpr expression) {
            String operator = expression.getOp() == null ? "UNKNOWN" : expression.getOp().name().toUpperCase();
            if (expression.getOp() == LogExpr.LogicalOp.not) {
                return new ContextUnary(operator, context(expression.getLHS()), sourceSpan);
            }
            if (expression.getLHS() == null || expression.getRHS() == null) {
                return contextUnsupported("logical-expression", expression.toString(), sourceSpan);
            }
            return new ContextBinary(operator, context(expression.getLHS()), context(expression.getRHS()), sourceSpan);
        }
        return contextUnsupported("logical-formula", formula.toString(), sourceSpan);
    }

    private ContextExpr contextUnsupported(String kind, String subject, SourceSpan sourceSpan) {
        return new ContextUnsupported(unsupported(kind, subject, sourceSpan));
    }

    private TermModel term(Term value) {
        if (value == null) {
            return unsupportedTerm("term", "<null>");
        }
        SourceSpan sourceSpan = span(value);
        if (value instanceof VarTerm variable) {
            return new VariableTermModel(variable.toString(), sourceSpan);
        }
        if (value instanceof StringTerm string) {
            return new StringTermModel(string.getString(), sourceSpan);
        }
        if (value instanceof ArithExpr arithmetic) {
            return new ArithmeticTermModel(
                    arithmetic.getOp() == null ? "UNKNOWN" : arithmetic.getOp().toString(),
                    optionalTerm(arithmetic.getLHS()),
                    optionalTerm(arithmetic.getRHS()),
                    sourceSpan);
        }
        if (value instanceof NumberTerm) {
            return new NumberTermModel(value.toString(), sourceSpan);
        }
        if (value instanceof ListTerm list) {
            List<TermModel> elements = new ArrayList<>();
            for (Term element : list) {
                elements.add(term(element));
            }
            Optional<TermModel> tail = list.isTail() && list.getTail() != null
                    ? Optional.of(term(list.getTail()))
                    : Optional.empty();
            return new ListTermModel(elements, tail, sourceSpan);
        }
        if (value instanceof SetTerm set) {
            List<TermModel> elements = new ArrayList<>();
            for (Term element : set) {
                elements.add(term(element));
            }
            return new SetTermModel(elements, sourceSpan);
        }
        if (value instanceof Literal literal) {
            return literal(literal);
        }
        if (value instanceof Structure structure) {
            return new CompoundTermModel(structure.getFunctor(), mapTerms(structure.getTerms()), sourceSpan);
        }
        return unsupportedTerm("term", value.toString());
    }

    private LiteralTermModel literal(Literal literal) {
        return new LiteralTermModel(
                literal.getFunctor(),
                mapTerms(literal.getTerms()),
                literal.negated(),
                literal.hasAnnot() ? mapTerms(literal.getAnnots()) : List.of(),
                span(literal));
    }

    private List<TermModel> mapTerms(List<Term> terms) {
        if (terms == null || terms.isEmpty()) {
            return List.of();
        }
        return terms.stream().map(this::term).toList();
    }

    private Optional<TermModel> optionalTerm(Term value) {
        return value == null ? Optional.empty() : Optional.of(term(value));
    }

    private UnsupportedTermModel unsupportedTerm(String kind, String subject) {
        return new UnsupportedTermModel(unsupported(kind, subject, SourceSpan.unknown(source)));
    }

    private PlanStepModel unsupportedStep(String kind, String subject, SourceSpan sourceSpan) {
        return new UnsupportedStepModel(unsupported(kind, subject, sourceSpan));
    }

    private UnsupportedFeature unsupported(String kind, String subject, SourceSpan sourceSpan) {
        UnsupportedFeature feature = new UnsupportedFeature(
                UnsupportedFeature.CODE,
                kind,
                subject == null ? "<null>" : subject,
                sourceSpan);
        unsupportedFeatures.add(feature);
        return feature;
    }

    private SourceSpan span(Term term) {
        return term == null ? SourceSpan.unknown(source) : span(term.getSrcInfo());
    }

    private SourceSpan span(SourceInfo sourceInfo) {
        if (sourceInfo == null
                || sourceInfo.getBeginSrcLine() <= SourceSpan.UNKNOWN_POSITION
                || sourceInfo.getEndSrcLine() <= SourceSpan.UNKNOWN_POSITION) {
            return SourceSpan.unknown(source);
        }
        return new SourceSpan(
                source,
                sourceInfo.getBeginSrcLine(),
                SourceSpan.UNKNOWN_POSITION,
                sourceInfo.getEndSrcLine(),
                SourceSpan.UNKNOWN_POSITION);
    }

    private String subject(Term term) {
        return term == null ? "<null>" : term.toString();
    }
}
