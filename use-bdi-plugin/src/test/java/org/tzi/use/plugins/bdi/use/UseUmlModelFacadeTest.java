package org.tzi.use.plugins.bdi.use;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.tzi.use.parser.use.USECompiler;
import org.tzi.use.uml.mm.MAssociation;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.sys.MSystem;

class UseUmlModelFacadeTest {
    @Test
    void projectsModelStateOperationsConstraintsAndStableFingerprint() throws Exception {
        MSystem system = loadFixture();
        UseUmlModelFacade facade = new UseUmlModelFacade();

        UseModelSnapshot empty = facade.snapshot(system);
        assertEquals(5, empty.classes().size());
        assertEquals(6, empty.attributes().size());
        assertEquals(3, empty.associations().size());
        assertEquals(7, empty.operations().size());
        assertEquals(4, empty.classInvariants().size());
        assertEquals(0, empty.objects().size());
        int totalPreconditions = empty.operations().stream()
                .mapToInt(op -> op.preconditions().size()).sum();
        assertTrue(totalPreconditions >= 1, "at least one precondition expected");
        int totalPostconditions = empty.operations().stream()
                .mapToInt(op -> op.postconditions().size()).sum();
        assertTrue(totalPostconditions >= 1, "at least one postcondition expected");
        assertEquals(empty.fingerprint(), facade.snapshot(system).fingerprint());

        var queue = system.state().createObject(system.model().getClass("Queue"), "queue1");
        var customer = system.state().createObject(system.model().getClass("Customer"), "customer1");
        MAssociation association = system.model().getAssociation("QueueCustomers");
        assertNotNull(association);
        system.state().createLink(association, List.of(queue, customer), null);

        UseModelSnapshot populated = facade.snapshot(system);
        assertEquals(2, populated.objects().size());
        assertEquals(1, populated.links().size());
        assertEquals(List.of("queue1", "customer1"), populated.links().get(0).objectNames());
        assertNotEquals(empty.fingerprint(), populated.fingerprint());
    }

    @Test
    void returnsExplicitOclCompileAndEvaluationStatuses() throws Exception {
        MSystem system = loadFixture();
        UseUmlModelFacade facade = new UseUmlModelFacade();

        OclEvaluationResult evaluated = facade.evaluateOcl(system, "1 + 1 = 2");
        assertEquals(OclEvaluationStatus.EVALUATED, evaluated.status());
        assertEquals("true", evaluated.value().orElseThrow());

        OclEvaluationResult compileError = facade.evaluateOcl(system, "not valid ocl");
        assertEquals(OclEvaluationStatus.COMPILE_ERROR, compileError.status());
        assertTrue(!compileError.diagnostics().isEmpty());
    }

    private static MSystem loadFixture() throws Exception {
        Path fixture = fixture("fixtures/use/QueueModel.use");
        StringWriter errors = new StringWriter();
        MModel model = USECompiler.compileSpecification(
                Files.newInputStream(fixture),
                fixture.toString(),
                new PrintWriter(errors),
                new ModelFactory());
        assertNotNull(model, errors::toString);
        model.setFilename(fixture.toString());
        return new MSystem(model);
    }

    private static Path fixture(String name) throws URISyntaxException {
        URL resource = UseUmlModelFacadeTest.class.getClassLoader().getResource(name);
        if (resource == null) {
            throw new IllegalStateException("Missing test fixture: " + name);
        }
        return Path.of(resource.toURI());
    }
}
