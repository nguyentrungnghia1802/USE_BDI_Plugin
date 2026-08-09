# Auction Experiment Protocol

## Research question

Can the plugin import the paired AgentSpeak/UML-OCL Auction model and detect a
small, labeled set of cross-model mutations through the existing validation
pipeline without mutating the canonical model or USE snapshot?

## Environment

| Item | Value |
|---|---|
| USE | 7.1.1 |
| Java | 21 |
| Jason | 3.3.0 |
| BDI metamodel | 0.1.0 |
| Canonical UML fixture | `fixtures/casestudy/auction/Auction.use` |
| AgentSpeak fixtures | `auctioneer.asl`, `bidder.asl` |
| Confirmed mapping size | 14 bindings |

The exact local Java and Maven versions must be recorded with the command
output for a paper run. The repository baseline recorded Java 21 and Maven
3.9.9; timing and host details remain environment-specific.

## Inputs and oracle

The canonical Auction fixture is compiled with the existing `USECompiler` path
and projected through `UseUmlModelFacade`. AgentSpeak is imported through the
Jason 3.3.0 adapter and normalized into the Java-owned BDI IR. The confirmed
mapping is created by `AuctionMappingFixtureTest` and contains class/object,
operation, parameter, and supported belief links.

The oracle is the labeled manifest at
`docs/project/evidence/auction-ground-truth.json`. Each mutant has one
expected primary rule ID and a required positive detection. A baseline count
of zero for the targeted rule is required before the mutant count is assessed.

## Mutant matrix

| Mutant | Controlled change | Expected rule | Expected delta |
|---|---|---|---:|
| STR-001 | Remove `Bidder` and dependent UML targets | MAP-003 | 9 |
| SIG-001 | Change `Auction::open()` to `open(flag:String)` | SIG-001 | 1 |
| REF-001 | Replace `bidder1` with absent `bidder2` in AgentSpeak | REF-001 | 4 |
| OCL-001 | Change the `open()` precondition from `draft` to `closed` | OCL-001 | 1 |

## Procedure

1. Build and run the focused case-study tests:

   ```powershell
   mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin "-Dtest=AuctionModelFixtureTest,AuctionAgentSpeakFixtureTest,AuctionMappingFixtureTest,AuctionBaselineReportTest,AuctionStructuralMutantTest,AuctionFaultInjectionTest" test
   ```

2. Run `auction-baseline.ps1` and verify the deterministic baseline report
   has 27 total findings and 14 confirmed mappings.
3. Run `auction-structural-mutant.ps1` and verify the nine `MAP-003` stale
   target findings.
4. Run `auction-evidence.ps1` to execute the artifact checks and nested
   baseline/structural scripts.
5. Inspect `auction-metrics.csv` and compute the scoped classification metrics
   using the formulas in `auction-classification-metrics.md`.
6. Preserve the canonical fixtures. Mutants are separate test resources and
   are never applied in place to `Auction.use`.

## Expected outputs

The reproducibility markers are `AUCTION_BASELINE_REPORT_OK`,
`AUCTION_STRUCTURAL_MUTANT_OK`, and `AUCTION_EVIDENCE_OK`. JSON and HTML
reports are generated under `use-bdi-plugin/target/case-study/auction/` and
are intentionally not committed because current source IDs contain normalized
absolute paths.

## Interpretation boundary

This protocol evaluates four targeted mutation instances, not all rules and
not a production workload. A detected mutant means that the expected primary
rule was observed with a positive delta. It does not establish issue-level
precision, recall, or general correctness for the entire rule catalog.
