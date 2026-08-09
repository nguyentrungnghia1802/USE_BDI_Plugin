package org.tzi.use.plugins.bdi.use;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.tzi.use.parser.use.USECompiler;
import org.tzi.use.uml.mm.MModel;
import org.tzi.use.uml.mm.ModelFactory;
import org.tzi.use.uml.sys.MSystem;

class LiveUseSnapshotProviderTest {
    @Test
    void capturesCurrentStateWithoutMutatingIt() throws Exception {
        MSystem system = loadFixture();
        LiveUseSnapshotProvider provider = new LiveUseSnapshotProvider(() -> system);

        UseSnapshotContext empty = provider.capture();
        assertEquals(0, empty.snapshot().objects().size());
        assertEquals(empty.snapshot().fingerprint(), provider.capture().snapshot().fingerprint());

        system.state().createObject(system.model().getClass("Queue"), "queue1");
        UseSnapshotContext populated = provider.capture();

        assertEquals(1, populated.snapshot().objects().size());
        assertNotEquals(empty.snapshot().fingerprint(), populated.snapshot().fingerprint());
        assertEquals(populated.snapshot().fingerprint(), provider.capture().snapshot().fingerprint());
    }

    private static MSystem loadFixture() throws Exception {
        URL resource = LiveUseSnapshotProviderTest.class.getClassLoader()
                .getResource("fixtures/use/QueueModel.use");
        assertNotNull(resource);
        Path fixture = Path.of(resource.toURI());
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
}
