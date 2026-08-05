package org.tzi.use.plugins.bdi.model.mapping;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.index.BdiIndex;
import org.tzi.use.plugins.bdi.index.BdiIndexBuilder;
import org.tzi.use.plugins.bdi.model.ir.AgentModel;
import org.tzi.use.plugins.bdi.use.UmlClassRef;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;

class MappingStalenessDetectorTest {
    @Test
    void detectsFingerprintAndRemovedTargetsWithoutFlaggingValidBindings() {
        AgentModel agent = new AgentModel(Path.of("agent.asl"), "test", 0, 0, 0);
        BdiIndex index = new BdiIndexBuilder().build(agent);
        UseModelSnapshot uml = new UseModelSnapshot(
                "Model", "model.use", List.of(new UmlClassRef("Agent", false, List.of())),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), "current");
        MappingDocument mapping = MappingDocument.empty("saved")
                .upsert(new MappingBinding(MappingKind.AGENT_CLASS, MappingSourceId.agent(agent), "Agent"))
                .upsert(new MappingBinding(MappingKind.AGENT_OBJECT, MappingSourceId.agent(agent), "missingObject"));

        List<MappingStaleness> findings = new MappingStalenessDetector().detect(List.of(agent), index, mapping, uml);

        assertTrue(findings.stream().anyMatch(finding -> finding.reason() == MappingStalenessReason.USE_FINGERPRINT_CHANGED));
        assertTrue(findings.stream().anyMatch(finding -> finding.reason() == MappingStalenessReason.TARGET_MISSING
                && finding.binding().map(MappingBinding::target).filter("missingObject"::equals).isPresent()));
        assertFalse(findings.stream().anyMatch(finding -> finding.reason() == MappingStalenessReason.TARGET_MISSING
                && finding.binding().map(MappingBinding::target).filter("Agent"::equals).isPresent()));
    }
}
