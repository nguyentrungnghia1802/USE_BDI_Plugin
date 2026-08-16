package org.tzi.use.plugins.bdi.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.importer.AslDiagnostic;
import org.tzi.use.plugins.bdi.importer.AslDiagnosticSeverity;
import org.tzi.use.plugins.bdi.index.ActionCallSite;
import org.tzi.use.plugins.bdi.index.BdiIndex;
import org.tzi.use.plugins.bdi.index.BdiIndexBuilder;
import org.tzi.use.plugins.bdi.model.ir.ActionStepModel;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.model.ir.ContextLiteral;
import org.tzi.use.plugins.bdi.model.ir.GoalModel;
import org.tzi.use.plugins.bdi.model.ir.InternalActionStepModel;
import org.tzi.use.plugins.bdi.model.ir.LiteralTermModel;
import org.tzi.use.plugins.bdi.model.ir.NumberTermModel;
import org.tzi.use.plugins.bdi.model.ir.PlanModel;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.model.ir.StringTermModel;
import org.tzi.use.plugins.bdi.model.ir.TermModel;
import org.tzi.use.plugins.bdi.model.ir.TestStepModel;
import org.tzi.use.plugins.bdi.model.ir.TriggerModel;
import org.tzi.use.plugins.bdi.model.ir.UnsupportedFeature;
import org.tzi.use.plugins.bdi.model.ir.VariableTermModel;
import org.tzi.use.plugins.bdi.model.mapping.MappingBinding;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingKind;
import org.tzi.use.plugins.bdi.model.mapping.MappingSourceId;
import org.tzi.use.plugins.bdi.use.UmlAttributeRef;
import org.tzi.use.plugins.bdi.use.UmlClassRef;
import org.tzi.use.plugins.bdi.use.UmlConstraintRef;
import org.tzi.use.plugins.bdi.use.UmlObjectRef;
import org.tzi.use.plugins.bdi.use.UmlOperationRef;
import org.tzi.use.plugins.bdi.use.UmlParameterRef;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;

class ValidationOrchestratorTest {
    @Test
    void evaluatesOnlyRulesEnabledByConfiguration() {
        RuleConfiguration configuration = RuleConfiguration.of(List.of("ASL-001", "SIG-003"));
        ValidationOrchestrator orchestrator = new ValidationOrchestrator(configuration);

        List<ConsistencyIssue> issues = orchestrator.evaluate(mutantContext());
        Set<String> issueIds = issues.stream().map(ConsistencyIssue::ruleId).collect(Collectors.toSet());

        assertEquals(configuration, orchestrator.configuration());
        assertEquals(Set.of("ASL-001", "SIG-003"),
                orchestrator.rules().stream().map(ConsistencyRule::id).collect(Collectors.toSet()));
        assertTrue(issueIds.stream().allMatch(configuration::isEnabled));
        assertTrue(issueIds.contains("ASL-001"));
        assertTrue(issueIds.contains("SIG-003"));
    }

    @Test
    void rejectsConfigurationForUnknownRuleIds() {
        RuleConfiguration configuration = RuleConfiguration.of(List.of("NOT-A-RULE"));

        assertThrows(IllegalArgumentException.class, () -> new ValidationOrchestrator(configuration));
    }

    @Test
    void appliesConfiguredSuppressionAfterRuleEvaluation() {
        Path source = Path.of("worker.asl").toAbsolutePath();
        Suppression suppression = new Suppression(
                "ASL-001",
                IssueFingerprint.forSource(new SourceSpan(source, 40, 3, 40, 3)),
                "accepted parser fixture");
        ValidationOrchestrator orchestrator = new ValidationOrchestrator(
                RuleConfiguration.of(List.of("ASL-001")),
                List.of(suppression));

        List<ConsistencyIssue> issues = orchestrator.evaluate(mutantContext());

        ConsistencyIssue issue = issues.stream()
                .filter(value -> value.ruleId().equals("ASL-001"))
                .findFirst()
                .orElseThrow();
        assertEquals(IssueStatus.SUPPRESSED, issue.status());
        assertTrue(issue.evidence().contains("Suppression reason: accepted parser fixture"));
    }

    @Test
    void evaluatesEveryFirstSliceRuleWithTraceableEvidence() {
        ValidationContext context = mutantContext();
        ValidationOrchestrator orchestrator = new ValidationOrchestrator();

        List<ConsistencyIssue> issues = orchestrator.evaluate(context);
        Set<String> ruleIds = orchestrator.rules().stream().map(ConsistencyRule::id).collect(Collectors.toSet());

        assertEquals(22, orchestrator.rules().size());
        assertTrue(ruleIds.containsAll(Set.of(
                "ASL-001", "ASL-002", "BDI-001", "BDI-002", "BDI-003", "BDI-004",
                "REF-001", "REF-002", "MAP-001", "MAP-002", "MAP-003", "SIG-001",
                "SIG-002", "SIG-003", "OWN-001", "BEL-001", "MSG-001",
                "OCL-001", "OCL-002", "CTX-001", "OCL-003", "OCL-004")));
        assertTrue(issues.stream().allMatch(issue -> issue.status() == IssueStatus.OPEN));
        assertTrue(issues.stream().allMatch(issue -> !issue.evidence().isEmpty()));
        assertTrue(issues.stream().anyMatch(issue -> issue.ruleId().equals("SIG-003")
                && issue.certainty() == IssueCertainty.UNKNOWN));
    }

    @Test
    void preservesFailUnknownAndNoFindingAcrossSnapshotSemanticRules() {
        Set<String> findingIds = new ValidationOrchestrator().evaluate(
                        snapshotSemanticContext(snapshotEvaluator(true)))
                .stream().map(ConsistencyIssue::ruleId).collect(Collectors.toSet());

        assertTrue(findingIds.containsAll(Set.of("OCL-001", "OCL-002", "CTX-001", "OCL-003")));

        Set<String> cleanIds = new ValidationOrchestrator().evaluate(
                        snapshotSemanticContext(snapshotEvaluator(false)))
                .stream().map(ConsistencyIssue::ruleId).collect(Collectors.toSet());
        assertTrue(cleanIds.stream().noneMatch(
                Set.of("OCL-001", "OCL-002", "CTX-001", "OCL-003", "OCL-004")::contains));
    }

    private static ValidationContext snapshotSemanticContext(SnapshotOclEvaluator evaluator) {
        Path source = Path.of("snapshot-semantics.asl").toAbsolutePath();
        LiteralTermModel ready = new LiteralTermModel("ready", List.of(), false, List.of(), span(source, 1));
        ActionStepModel step = new ActionStepModel(
                new LiteralTermModel("work", List.of(new StringTermModel("item", span(source, 4))),
                        false, List.of(), span(source, 4)),
                span(source, 4));
        PlanModel plan = new PlanModel(
                "work_plan",
                achievementTrigger(new LiteralTermModel("work_goal", List.of(), false, List.of(), span(source, 2)),
                        source, 2),
                Optional.of(new ContextLiteral(ready, span(source, 3))),
                List.of(step),
                span(source, 2));
        AgentModel agent = new AgentModel(
                source, "test", 1, 0, 1,
                List.of(new org.tzi.use.plugins.bdi.model.ir.BeliefModel(ready, span(source, 1))),
                List.of(), List.of(plan), List.of());
        BdiIndex index = new BdiIndexBuilder().build(agent);
        UmlOperationRef operation = new UmlOperationRef(
                "Worker", "work", List.of(new UmlParameterRef("item", "String")), Optional.empty(),
                List.of(new UmlConstraintRef("Worker", Optional.of("work"), "pre", "ready", "self.ready")),
                List.of(), false, false);
        UseModelSnapshot uml = new UseModelSnapshot(
                "SnapshotSemantics", "snapshot-semantics.use",
                List.of(new UmlClassRef("Worker", false, List.of())),
                List.of(new UmlAttributeRef("Worker", "ready", "Boolean", false, Optional.empty(), Optional.empty())),
                List.of(), List.of(operation), List.of(),
                List.of(new UmlObjectRef("worker1", "Worker", true, Map.of("ready", "true"))),
                List.of(), "snapshot-semantics-fingerprint");
        ActionCallSite action = index.allActionCallSites().get(0);
        MappingDocument mapping = MappingDocument.empty(uml.fingerprint())
                .upsert(new MappingBinding(MappingKind.AGENT_CLASS, MappingSourceId.agent(agent), "Worker"))
                .upsert(new MappingBinding(MappingKind.AGENT_OBJECT, MappingSourceId.agent(agent), "worker1"))
                .upsert(new MappingBinding(MappingKind.BELIEF_ATTRIBUTE, "ready/0", "Worker::ready"))
                .upsert(new MappingBinding(
                        MappingKind.ACTION_OPERATION,
                        MappingSourceId.action(action),
                        operation.reference(),
                        Optional.of("soil: worker1.ready := false"),
                        List.of("bounded test effect")));
        return new ValidationContext(List.of(agent), List.of(), index, mapping, Optional.of(uml), Optional.of(evaluator));
    }

    private static SnapshotOclEvaluator snapshotEvaluator(boolean findings) {
        return new SnapshotOclEvaluator() {
            @Override
            public List<OclSnapshotResult> evaluatePreconditions(
                    UmlOperationRef operation,
                    String receiverObject,
                    List<TermModel> arguments) {
                return findings
                        ? List.of(
                                new OclSnapshotResult("pre-fail", OclSnapshotStatus.FAIL, List.of("false")),
                                new OclSnapshotResult("pre-unknown", OclSnapshotStatus.UNKNOWN, List.of("unknown")))
                        : List.of(new OclSnapshotResult("pre-pass", OclSnapshotStatus.PASS, List.of("true")));
            }

            @Override
            public OclSnapshotResult evaluateExpression(String expression, String subject) {
                return new OclSnapshotResult(
                        subject,
                        findings ? OclSnapshotStatus.FAIL : OclSnapshotStatus.PASS,
                        List.of(expression));
            }

            @Override
            public BoundedEffectResult simulateSoilEffect(String source) {
                return new BoundedEffectResult(
                        findings ? BoundedEffectStatus.INVARIANT_VIOLATED : BoundedEffectStatus.PASS,
                        List.of(source));
            }
        };
    }

    private static ValidationContext mutantContext() {
        Path worker = Path.of("worker.asl").toAbsolutePath();
        Path unmapped = Path.of("unmapped.asl").toAbsolutePath();
        AgentModel workerAgent = new AgentModel(
                worker,
                "test",
                1,
                1,
                2,
                List.of(new org.tzi.use.plugins.bdi.model.ir.BeliefModel(literal("ready", 1), span(worker, 1))),
                List.of(new GoalModel(literal("orphan", 2), span(worker, 2))),
                List.of(
                        new PlanModel(
                                "main",
                                achievementTrigger(literal("serve", 3), worker, 3),
                                Optional.empty(),
                                List.of(
                                        action("arity", List.of(new StringTermModel("one", span(worker, 10))), worker, 10),
                                        action("typed", List.of(new NumberTermModel("7", span(worker, 9))), worker, 9),
                                        action("unknown", List.of(new VariableTermModel("Value", span(worker, 12))), worker, 12),
                                        action("unmapped", List.of(), worker, 13),
                                        new TestStepModel(new ContextLiteral(literal("missing_test", 14), span(worker, 14)), span(worker, 14)),
                                        new InternalActionStepModel(
                                                literal(".send", List.of(literal("missing_agent", 15),
                                                        new StringTermModel("payload", span(worker, 15))), 15),
                                                span(worker, 15)),
                                        action("object_ref", List.of(literal("missing_object", 16)), worker, 16)),
                                span(worker, 3)),
                        new PlanModel(
                                "main",
                                new TriggerModel(
                                        TriggerModel.TriggerOperator.ADD,
                                        TriggerModel.TriggerType.ACHIEVE,
                                        new VariableTermModel("Trigger", span(worker, 20)),
                                        span(worker, 20)),
                                Optional.empty(),
                                List.of(),
                                span(worker, 20))),
                List.of(new UnsupportedFeature(
                        UnsupportedFeature.CODE,
                        "future-action",
                        "future_step",
                        span(worker, 30))));
        AgentModel unmappedAgent = new AgentModel(
                unmapped,
                "test",
                0,
                0,
                0,
                List.of(),
                List.of(),
                List.of(),
                List.of());
        BdiIndex index = new BdiIndexBuilder().build(List.of(workerAgent, unmappedAgent));
        UseModelSnapshot uml = umlSnapshot();
        MappingDocument mapping = mappingFor(workerAgent, index, uml);
        return new ValidationContext(
                List.of(workerAgent, unmappedAgent),
                List.of(new AslDiagnostic(
                        AslDiagnostic.SYNTAX_ERROR_CODE,
                        AslDiagnosticSeverity.ERROR,
                        worker,
                        40,
                        3,
                        "Unexpected token")),
                index,
                mapping,
                Optional.of(uml));
    }

    private static MappingDocument mappingFor(AgentModel agent, BdiIndex index, UseModelSnapshot uml) {
        MappingDocument mapping = MappingDocument.empty(uml.fingerprint())
                .upsert(new MappingBinding(MappingKind.AGENT_CLASS, MappingSourceId.agent(agent), "Worker"))
                .upsert(new MappingBinding(MappingKind.BELIEF_ATTRIBUTE, "ready/0", "Queue::removed"));
        mapping = mapping.upsert(new MappingBinding(
                MappingKind.ACTION_OPERATION,
                action(index, "arity").map(MappingSourceId::action).orElseThrow(),
                operation(uml, "arity").reference()));
        mapping = mapping.upsert(new MappingBinding(
                MappingKind.ACTION_OPERATION,
                action(index, "typed").map(MappingSourceId::action).orElseThrow(),
                operation(uml, "typed").reference()));
        return mapping.upsert(new MappingBinding(
                MappingKind.ACTION_OPERATION,
                action(index, "unknown").map(MappingSourceId::action).orElseThrow(),
                operation(uml, "unknown").reference()));
    }

    private static Optional<ActionCallSite> action(BdiIndex index, String functor) {
        return index.allActionCallSites().stream()
                .filter(site -> site.signature().map(signature -> signature.functor().equals(functor)).orElse(false))
                .findFirst();
    }

    private static UmlOperationRef operation(UseModelSnapshot uml, String name) {
        return uml.operations().stream().filter(operation -> operation.name().equals(name)).findFirst().orElseThrow();
    }

    private static UseModelSnapshot umlSnapshot() {
        return new UseModelSnapshot(
                "Validation",
                "validation.use",
                List.of(new UmlClassRef("Worker", false, List.of()), new UmlClassRef("Queue", false, List.of())),
                List.of(new UmlAttributeRef("Queue", "ready", "Boolean", false, Optional.empty(), Optional.empty())),
                List.of(),
                List.of(
                        new UmlOperationRef("Queue", "arity", List.of(
                                new UmlParameterRef("first", "String"), new UmlParameterRef("second", "String")),
                                Optional.empty(), List.of(), List.of(), false, false),
                        new UmlOperationRef("Queue", "typed", List.of(new UmlParameterRef("value", "String")),
                                Optional.empty(), List.of(), List.of(), false, false),
                        new UmlOperationRef("Queue", "unknown", List.of(new UmlParameterRef("value", "String")),
                                Optional.empty(), List.of(), List.of(), false, false)),
                List.of(),
                List.of(),
                List.of(),
                "validation-fingerprint");
    }

    private static TriggerModel achievementTrigger(LiteralTermModel literal, Path source, int line) {
        return new TriggerModel(
                TriggerModel.TriggerOperator.ADD,
                TriggerModel.TriggerType.ACHIEVE,
                literal,
                span(source, line));
    }

    private static ActionStepModel action(String functor, List<TermModel> arguments, Path source, int line) {
        return new ActionStepModel(literal(functor, arguments, line), span(source, line));
    }

    private static LiteralTermModel literal(String functor, int line) {
        return literal(functor, List.of(), line);
    }

    private static LiteralTermModel literal(String functor, List<TermModel> arguments, int line) {
        Path source = Path.of("worker.asl").toAbsolutePath();
        return new LiteralTermModel(functor, arguments, false, List.of(), span(source, line));
    }

    private static SourceSpan span(Path source, int line) {
        return new SourceSpan(source, line, 1, line, 20);
    }
}
