package org.tzi.use.plugins.bdi.casestudy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.tzi.use.plugins.bdi.application.AnalysisMetamodelDescriptor;
import org.tzi.use.plugins.bdi.application.AnalysisVersionMetadata;
import org.tzi.use.plugins.bdi.application.BdiImportSnapshot;
import org.tzi.use.plugins.bdi.application.BdiImportService;
import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshot;
import org.tzi.use.plugins.bdi.application.CurrentAnalysisSnapshotService;
import org.tzi.use.plugins.bdi.index.BdiIndex;
import org.tzi.use.plugins.bdi.model.mapping.MappingDocument;
import org.tzi.use.plugins.bdi.model.mapping.MappingFingerprint;
import org.tzi.use.plugins.bdi.use.UseModelSnapshot;
import org.tzi.use.plugins.bdi.use.UseSnapshotOclEvaluator;
import org.tzi.use.plugins.bdi.use.UseUmlModelFacade;
import org.tzi.use.plugins.bdi.validation.ConsistencyRule;
import org.tzi.use.plugins.bdi.validation.RuleConfiguration;
import org.tzi.use.plugins.bdi.validation.RulePhase;
import org.tzi.use.plugins.bdi.validation.ValidationOrchestrator;
import org.tzi.use.uml.sys.MSystem;

class CurrentAnalysisSnapshotServiceTest {
    private static final Instant FIXED_TIME = Instant.parse("2026-08-09T12:00:00Z");

    @Test
    void composesDeterministicAuctionSnapshotWithoutChangingUseState() throws Exception {
        BdiImportSnapshot imported = new BdiImportService().importFiles(List.of(
                AuctionMappingFixtureTest.fixture("fixtures/casestudy/auction/auctioneer.asl"),
                AuctionMappingFixtureTest.fixture("fixtures/casestudy/auction/bidder.asl")));
        MSystem system = AuctionMappingFixtureTest.loadAuctionSystem();
        UseModelSnapshot uml = new UseUmlModelFacade().snapshot(system);
        MappingDocument mapping = AuctionMappingFixtureTest.confirmedMapping(imported, uml);
        CurrentAnalysisSnapshotService service = new CurrentAnalysisSnapshotService(
                new ValidationOrchestrator(),
                "Configuration: standard rules, no suppressions",
                "0.1.0",
                "USE-7.1.1");
        String before = new UseUmlModelFacade().snapshot(system).fingerprint();

        CurrentAnalysisSnapshot first = service.create(
                FIXED_TIME,
                imported,
                Optional.of(uml),
                Optional.of(new UseSnapshotOclEvaluator(system)),
                mapping);
        CurrentAnalysisSnapshot second = service.create(
                FIXED_TIME,
                imported,
                Optional.of(uml),
                Optional.of(new UseSnapshotOclEvaluator(system)),
                mapping);
        String after = new UseUmlModelFacade().snapshot(system).fingerprint();

        assertEquals(FIXED_TIME, first.timestamp());
        assertEquals(first, second);
        assertEquals(imported.fileCount(), first.importedFileCount());
        assertEquals(mapping.bindings().size(), first.mappingCount());
        assertEquals(first.issues().size(), first.issueCount());
        assertEquals(Optional.of(uml.fingerprint()), first.modelHash());
        assertEquals(MappingFingerprint.compute(mapping), first.mappingHash());
        assertEquals(List.of("3.3.0"), first.versions().parserVersions());
        assertEquals(AnalysisMetamodelDescriptor.current(), first.versions().analysisMetamodel());
        assertEquals(before, after, "analysis must not mutate the current USE state");
        assertThrows(UnsupportedOperationException.class, () -> first.issues().clear());
    }

    @Test
    void invokesConfiguredValidationExactlyOnce() {
        AtomicInteger evaluations = new AtomicInteger();
        ConsistencyRule countingRule = new ConsistencyRule() {
            @Override
            public String id() {
                return "TEST-001";
            }

            @Override
            public RulePhase phase() {
                return RulePhase.PARSE;
            }

            @Override
            public List<org.tzi.use.plugins.bdi.validation.ConsistencyIssue> evaluate(
                    org.tzi.use.plugins.bdi.validation.ValidationContext context) {
                evaluations.incrementAndGet();
                return List.of();
            }
        };
        ValidationOrchestrator orchestrator = new ValidationOrchestrator(
                List.of(countingRule), RuleConfiguration.of(List.of("TEST-001")), List.of());
        CurrentAnalysisSnapshotService service = new CurrentAnalysisSnapshotService(
                orchestrator, "test configuration", "0.1.0", "USE-7.1.1");

        service.create(
                FIXED_TIME,
                emptyImport(),
                Optional.empty(),
                Optional.empty(),
                MappingDocument.empty("unknown"));

        assertEquals(1, evaluations.get());
    }

    @Test
    void rejectsInconsistentCountsHashesAndComposition() {
        BdiImportSnapshot imported = emptyImport();
        MappingDocument mapping = MappingDocument.empty("unknown");
        AnalysisVersionMetadata versions = new AnalysisVersionMetadata(
                "0.1.0", "USE-7.1.1", imported.index().metamodelVersion(), List.of());

        IllegalArgumentException countError = assertThrows(IllegalArgumentException.class, () ->
                new CurrentAnalysisSnapshot(
                        FIXED_TIME, imported, Optional.empty(), mapping, "test", List.of(), List.of(),
                        1, 0, 0, Optional.empty(), MappingFingerprint.compute(mapping), versions));
        IllegalArgumentException hashError = assertThrows(IllegalArgumentException.class, () ->
                new CurrentAnalysisSnapshot(
                        FIXED_TIME, imported, Optional.empty(), mapping, "test", List.of(), List.of(),
                        0, 0, 0, Optional.empty(), "0".repeat(64), versions));
        AnalysisVersionMetadata wrongProfile = new AnalysisVersionMetadata(
                "0.1.0", "USE-7.1.1", imported.index().metamodelVersion(),
                new AnalysisMetamodelDescriptor("urn:test:old-profile", "0.9.0", "Old profile"),
                List.of());
        IllegalArgumentException profileError = assertThrows(IllegalArgumentException.class, () ->
                new CurrentAnalysisSnapshot(
                        FIXED_TIME, imported, Optional.empty(), mapping, "test", List.of(), List.of(),
                        0, 0, 0, Optional.empty(), MappingFingerprint.compute(mapping), wrongProfile));
        CurrentAnalysisSnapshotService service = new CurrentAnalysisSnapshotService(
                new ValidationOrchestrator(), "test", "0.1.0", "USE-7.1.1");
        IllegalArgumentException compositionError = assertThrows(IllegalArgumentException.class, () ->
                service.create(
                        FIXED_TIME,
                        imported,
                        Optional.empty(),
                        Optional.of(unknownEvaluator()),
                        mapping));

        assertTrue(countError.getMessage().contains("importedFileCount"));
        assertTrue(hashError.getMessage().contains("mappingHash"));
        assertTrue(profileError.getMessage().contains("Analysis metamodel descriptor"));
        assertNotNull(compositionError.getMessage());
    }

    @Test
    void aggregateContainsOnlyPluginOwnedImmutableBoundaries() {
        List<String> forbidden = List.of("jason.", "MSystem", "java.awt", "javax.swing");

        for (RecordComponent component : CurrentAnalysisSnapshot.class.getRecordComponents()) {
            assertTrue(forbidden.stream().noneMatch(component.getGenericType().getTypeName()::contains),
                    () -> "Forbidden type in snapshot: " + component.getGenericType());
        }
        assertTrue(Arrays.stream(CurrentAnalysisSnapshot.class.getRecordComponents()).count() > 0);
    }

    private static BdiImportSnapshot emptyImport() {
        return new BdiImportSnapshot(List.of(), List.of(), BdiIndex.empty());
    }

    private static org.tzi.use.plugins.bdi.validation.SnapshotOclEvaluator unknownEvaluator() {
        return new org.tzi.use.plugins.bdi.validation.SnapshotOclEvaluator() {
            @Override
            public List<org.tzi.use.plugins.bdi.validation.OclSnapshotResult> evaluatePreconditions(
                    org.tzi.use.plugins.bdi.use.UmlOperationRef operation,
                    String receiverObject,
                    List<org.tzi.use.plugins.bdi.model.ir.TermModel> arguments) {
                return List.of();
            }

            @Override
            public org.tzi.use.plugins.bdi.validation.OclSnapshotResult evaluateExpression(
                    String expression,
                    String subject) {
                return new org.tzi.use.plugins.bdi.validation.OclSnapshotResult(
                        subject,
                        org.tzi.use.plugins.bdi.validation.OclSnapshotStatus.UNKNOWN,
                        List.of("test"));
            }

            @Override
            public org.tzi.use.plugins.bdi.validation.BoundedEffectResult simulateSoilEffect(String source) {
                return new org.tzi.use.plugins.bdi.validation.BoundedEffectResult(
                        org.tzi.use.plugins.bdi.validation.BoundedEffectStatus.UNKNOWN,
                        List.of("test"));
            }
        };
    }
}
