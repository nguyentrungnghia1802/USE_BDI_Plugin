package org.tzi.use.plugins.bdi.use;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.tzi.use.parser.use.USECompiler;
import org.tzi.use.plugins.bdi.model.ir.NumberTermModel;
import org.tzi.use.plugins.bdi.model.ir.SourceSpan;
import org.tzi.use.plugins.bdi.model.ir.StringTermModel;
import org.tzi.use.plugins.bdi.model.ir.VariableTermModel;
import org.tzi.use.plugins.bdi.validation.BoundedEffectResult;
import org.tzi.use.plugins.bdi.validation.BoundedEffectStatus;
import org.tzi.use.plugins.bdi.validation.OclSnapshotStatus;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.sys.MSystem;

class UseSnapshotOclEvaluatorTest {
    @Test
    void bindsReceiverAndArgumentsThenRestoresBoundedSoilVariation() throws Exception {
        MSystem system = loadFixture();
        system.state().createObject(system.model().getClass("Queue"), "queue1");
        UseSnapshotOclEvaluator evaluator = new UseSnapshotOclEvaluator(system);
        UmlOperationRef enqueue = new UseUmlModelFacade().snapshot(system).operations().stream()
                .filter(operation -> operation.reference().startsWith("Queue::enqueue("))
                .findFirst()
                .orElseThrow();
        SourceSpan span = SourceSpan.unknown(Path.of("fixture.asl"));

        assertEquals(OclSnapshotStatus.PASS, evaluator.evaluatePreconditions(
                enqueue, "queue1", List.of(new StringTermModel("ticket", span))).get(0).status());
        assertEquals(OclSnapshotStatus.FAIL, evaluator.evaluatePreconditions(
                enqueue, "queue1", List.of(new StringTermModel("", span))).get(0).status());
        assertEquals(OclSnapshotStatus.UNKNOWN, evaluator.evaluatePreconditions(
                enqueue, "queue1", List.of(new VariableTermModel("Item", span))).get(0).status());
        assertEquals(OclSnapshotStatus.UNKNOWN,
                evaluator.evaluateExpression("not valid ocl", "broken expression").status());

        String before = new UseUmlModelFacade().snapshot(system).fingerprint();
        BoundedEffectResult result = evaluator.simulateSoilEffect("soil: queue1.size := -1");
        String after = new UseUmlModelFacade().snapshot(system).fingerprint();

        assertEquals(BoundedEffectStatus.INVARIANT_VIOLATED, result.status());
        assertEquals(before, after, "variation must restore the user's system state");
    }

    private static MSystem loadFixture() throws Exception {
        Path fixture = fixture("fixtures/use/QueueModel.use");
        StringWriter errors = new StringWriter();
        MModel model = USECompiler.compileSpecification(
                Files.newInputStream(fixture), fixture.toString(), new PrintWriter(errors), new ModelFactory());
        assertNotNull(model, errors::toString);
        model.setFilename(fixture.toString());
        return new MSystem(model);
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = UseSnapshotOclEvaluatorTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}
