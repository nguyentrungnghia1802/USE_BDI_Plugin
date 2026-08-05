package org.tzi.use.plugins.bdi.model.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.index.ActionCallSite;
import org.tzi.use.plugins.bdi.index.BdiIndex;
import org.tzi.use.plugins.bdi.index.PredicateSignature;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.use.UmlAttributeRef;
import org.tzi.use.plugins.bdi.use.UmlClassRef;
import org.tzi.use.plugins.bdi.use.UmlObjectRef;
import org.tzi.use.plugins.bdi.use.UmlOperationRef;
import org.tzi.use.plugins.bdi.use.UmlParameterRef;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;

class MappingSuggestionServiceTest {
    private final MappingSuggestionService service = new MappingSuggestionService();

    @Test
    void scoresAgentBeliefActionAndReceiverCandidatesWithEvidence() throws Exception {
        BdiImportSnapshot imported = new BdiImportService().importFiles(List.of(fixture(
                "fixtures/smartqueue/Smart_manager_agent.asl")));
        BdiIndex index = imported.index();
        UseModelSnapshot uml = umlSnapshot();

        List<MappingSuggestion> agentClasses = service.suggestAgentClasses(imported.models(), uml);
        List<MappingSuggestion> agentObjects = service.suggestAgentObjects(imported.models(), uml);
        List<MappingSuggestion> actions = service.suggestActionOperations(index, uml);
        List<MappingSuggestion> beliefs = service.suggestBeliefAttributes(index, uml);
        List<MappingSuggestion> receivers = service.suggestReceiverBindings(index, uml);

        assertTrue(agentClasses.stream().anyMatch(candidate ->
                candidate.target().equals("ManagerAgent") && candidate.score() > 0.4));
        assertTrue(agentObjects.stream().anyMatch(candidate ->
                candidate.target().equals("manager_agent") && candidate.score() >= 0.75));
        assertTrue(actions.stream().anyMatch(candidate ->
                candidate.target().startsWith("CounterAgent::print") && candidate.score() >= 0.75));
        assertTrue(beliefs.stream().anyMatch(candidate ->
                candidate.source().equals("queue_length/2")
                        && candidate.target().equals("Queue::queueLength")
                        && candidate.score() == 1.0));
        assertTrue(receivers.stream().anyMatch(candidate ->
                candidate.target().equals("counter_agent") && candidate.score() == 1.0));
        assertTrue(actions.get(0).reasons().size() >= 2);
    }

    @Test
    void createsPositionalParameterBindingsForAnActionOperationPair() {
        Path source = Path.of("QueueAgent.asl").toAbsolutePath();
        SourceSpan span = new SourceSpan(source, 1, 1, 1, 10);
        ActionCallSite action = new ActionCallSite(
                "enqueue-plan",
                1,
                ActionCallSite.ActionKind.EXTERNAL_ACTION,
                "enqueue(item)",
                Optional.of(new PredicateSignature("enqueue", 1)),
                span);
        UmlOperationRef operation = new UmlOperationRef(
                "Queue",
                "enqueue",
                List.of(new UmlParameterRef("item", "String")),
                Optional.empty(),
                List.of(),
                List.of(),
                false,
                false);

        List<MappingSuggestion> parameters = service.suggestParameterBindings(action, operation);

        assertEquals(1, parameters.size());
        assertEquals(MappingKind.PARAMETER, parameters.get(0).kind());
        assertEquals(1.0, parameters.get(0).score());
        assertEquals("argument[0]", parameters.get(0).expression().orElseThrow());
    }

    private static UseModelSnapshot umlSnapshot() {
        return new UseModelSnapshot(
                "SmartQueue",
                "SmartQueue.use",
                List.of(new UmlClassRef("ManagerAgent", false, List.of()), new UmlClassRef("CounterAgent", false, List.of())),
                List.of(
                        new UmlAttributeRef("Queue", "queueLength", "Integer", false, Optional.empty(), Optional.empty()),
                        new UmlAttributeRef("Counter", "counterStatus", "String", false, Optional.empty(), Optional.empty())),
                List.of(),
                List.of(new UmlOperationRef(
                        "CounterAgent",
                        "print",
                        List.of(new UmlParameterRef("message", "String")),
                        Optional.empty(),
                        List.of(),
                        List.of(),
                        false,
                        false)),
                List.of(),
                List.of(
                        new UmlObjectRef("manager_agent", "ManagerAgent", true, Map.of()),
                        new UmlObjectRef("counter_agent", "CounterAgent", true, Map.of()),
                        new UmlObjectRef("staff_agent", "StaffAgent", true, Map.of())),
                List.of(),
                "use-fingerprint");
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = MappingSuggestionServiceTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}
