package org.tzi.use.plugins.bdi.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
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
import org.tzi.use.plugins.bdi.use.UmlOperationRef;
import org.tzi.use.plugins.bdi.use.UmlParameterRef;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;

class ValidationOrchestratorTest {
    @Test
    void evaluatesEveryFirstSliceRuleWithTraceableEvidence() {
        ValidationContext context = mutantContext();
        ValidationOrchestrator orchestrator = new ValidationOrchestrator();

        List<ConsistencyIssue> issues = orchestrator.evaluate(context);
        Set<String> ids = issues.stream().map(ConsistencyIssue::ruleId).collect(Collectors.toSet());

        assertEquals(15, orchestrator.rules().size());
        assertTrue(ids.containsAll(Set.of(
                "ASL-001", "ASL-002", "BDI-001", "BDI-002", "BDI-003", "BDI-004",
                "REF-001", "REF-002", "MAP-001", "MAP-002", "MAP-003", "SIG-001",
                "SIG-002", "SIG-003", "OWN-001")));
        assertTrue(issues.stream().allMatch(issue -> issue.status() == IssueStatus.OPEN));
        assertTrue(issues.stream().allMatch(issue -> !issue.evidence().isEmpty()));
        assertTrue(issues.stream().anyMatch(issue -> issue.ruleId().equals("SIG-003")
                && issue.certainty() == IssueCertainty.UNKNOWN));
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
