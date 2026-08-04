package org.tzi.use.plugins.bdi.model.ir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class IrHierarchyTest {
    private static final SourceSpan SPAN = new SourceSpan(Path.of("fixture.asl"), 1, 0, 1, 0);

    @Test
    void termHierarchyPreservesStructuredValuesAndImmutability() {
        VariableTermModel variable = new VariableTermModel("Q", SPAN);
        NumberTermModel number = new NumberTermModel("6", SPAN);
        StringTermModel string = new StringTermModel("ready", SPAN);
        CompoundTermModel compound = new CompoundTermModel("queue", List.of(variable, number), SPAN);
        ListTermModel list = new ListTermModel(List.of(compound), Optional.of(variable), SPAN);
        SetTermModel set = new SetTermModel(List.of(string, number), SPAN);
        ArithmeticTermModel arithmetic = new ArithmeticTermModel("-", Optional.of(number), Optional.of(number), SPAN);

        assertEquals("Q", variable.render());
        assertEquals("6", number.render());
        assertEquals("\"ready\"", string.render());
        assertEquals("queue(Q,6)", compound.render());
        assertEquals("[queue(Q,6)|Q]", list.render());
        assertEquals("{\"ready\",6}", set.render());
        assertEquals("6-6", arithmetic.render());
        assertThrows(UnsupportedOperationException.class, () -> list.elements().clear());
    }

    @Test
    void sourceSpanAndUnsupportedFeatureKeepExplicitEvidence() {
        SourceSpan unknown = SourceSpan.unknown(Path.of("fixture.asl"));
        UnsupportedFeature feature = new UnsupportedFeature(
                UnsupportedFeature.CODE,
                "future-body-type",
                "future_action",
                unknown);

        assertEquals(0, unknown.beginLine());
        assertEquals("ASL-002", feature.code());
        assertEquals("Unsupported AgentSpeak feature: future-body-type (future_action)", feature.message());
        assertThrows(IllegalArgumentException.class, () -> new SourceSpan(Path.of("fixture.asl"), 2, 0, 1, 0));
    }
}
