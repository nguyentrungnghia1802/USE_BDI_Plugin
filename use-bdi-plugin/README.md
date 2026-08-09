# USE BDI Plugin

This module contains the verified USE plugin for the thesis project. It adds
`Plugins > AgentSpeak > Hello BDI Plugin` and includes the Jason 3.3.0
importer, normalized BDI IR, mapping editor, 22 consistency rules, bounded
snapshot OCL support, report exporters, and the Auction evaluation bundle.
Current gaps are the live GUI report/configuration composition, portable source
identity, optional House Building coverage, and the final tag/full backup gate.
The canonical status is maintained in `docs/project/README.md`.

## Parser test

From the repository root:

```powershell
mvn -pl use-bdi-plugin test
```

The parser fixture tests verify one initial belief, one initial goal, one plan,
the pinned parser version, and a structured `ASL-001` diagnostic for invalid
syntax. Diagnostics carry severity, source file, message, and a one-based line
and column when Jason provides an error token. The adapter disables Jason's
optional web mind inspector before initializing the parser so an offline import
does not open a background HTTP server. Multi-file imports preserve input order,
return immutable per-file summaries, continue after per-file failures, and return
immutable diagnostics. Summaries also expose top-level initial-belief,
initial-goal, and plan locations as normalized source paths with Jason's verified
begin/end line range. Syntax failures use `ASL-001`; file/import failures
without a parser location use `ASL-IMPORT-001` with position `0/0`.
`AslImportResult.toReport()` exposes the distinct parser versions used by
successful files without duplicating summaries or diagnostics; JSON/HTML export
is intentionally a later reporting slice.

The Smart Queue prototype is kept as a test fixture at
`src/test/resources/fixtures/smartqueue/Smart_manager_agent.asl`; the smoke
script checks that it is not reintroduced at repository root.

## Performance baseline

Run the small import/IR/index baseline from the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\performance.ps1
```

The benchmark warms up twice, measures seven imports of the Smart Queue
fixture, verifies the materialized IR and BDI index on every iteration, and
writes `use-bdi-plugin/target/performance/bdi-import-index.json`. The test
reports minimum, median, p95, and all measured durations in nanoseconds. It is
an environment comparison baseline, not a hard timing gate; the fixture is
small and is not yet the Auction case study workload.

## Clean-clone reproducibility

Run the clean-clone build from the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\clean-clone.ps1
```

The script clones the exact current `HEAD` into a generated temporary
directory, runs `mvn --batch-mode --no-transfer-progress -pl use-assembly -am
package`, verifies the shaded plugin JAR and the plugin entry in the USE ZIP,
and checks that the clone remains clean after the build. Use `-KeepClone` only
when inspecting a failed temporary clone. The script never uses the current
working tree's unstaged files.

## Auction case-study fixture

The first Auction UML/OCL fixture is kept at
`src/test/resources/fixtures/casestudy/auction/Auction.use`. It is compiled
and projected through the verified USE adapter by
`AuctionModelFixtureTest`.

The paired Jason 3.3.0 AgentSpeak fixtures are kept beside it as
`auctioneer.asl` and `bidder.asl`. `AuctionAgentSpeakFixtureTest` imports both
files through `BdiImportService`, verifies materialized beliefs/goals/plans,
and checks the indexed `open`, `placeBid`, `close`, `submitBid`, and `.print`
call sites. The plans have explicit labels so the stable source IDs remain
unique when multiple plans use the same step position.

`AuctionMappingFixtureTest` selects the exact-name/arity candidates, confirms
class/object, action/operation, positional-parameter, and supported belief
bindings, round-trips them through `MappingFileRepository`, and verifies no
stale or `MAP-*` issue remains. The mapping is created at test runtime because
the current source-ID contract intentionally stores normalized absolute paths;
no checkout-specific JSON is committed.

Generate the current Auction baseline report with:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\auction-baseline.ps1
```

The script writes `target/case-study/auction/auction-baseline.json` and `.html`.
The test-backed baseline contains 14 confirmed mappings and 27 reproducible
findings across `REF-001`, `BEL-001`, `OCL-002`, `OCL-004`, `OWN-001`,
`SIG-002`, and `SIG-003`; these are the pre-mutant comparison point, not a
claim that every cross-model rule is already semantically clean.

Run the first structural mutant slice with:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\auction-structural-mutant.ps1
```

The fixture `structural-remove-bidder.use` removes the `Bidder` UML structure
and its dependent operations/associations while remaining a valid USE model.
The test keeps the confirmed baseline mapping, then verifies nine missing
targets and nine `MAP-003` findings. This is mutation evidence for stale
mapping detection; the ground-truth manifest and aggregate metrics remain
are tracked separately in `docs/project/evidence/` and validated by the
Auction evidence test.

Run the complete Auction evaluation/evidence bundle with:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\auction-evidence.ps1
```

The command runs signature, reference, and OCL fault-injection tests, checks
the ground-truth/metrics/diagram/mapping artifacts, regenerates the baseline
report, and reruns the structural mutant smoke. It ends with
`AUCTION_EVIDENCE_OK` when all evidence gates pass.

## Build and automated smoke

From the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1
```

The script builds the reactor through `package`, imports a valid-invalid-valid
fixture sequence using the shaded plugin JAR from the distribution, checks the
partial result and syntax diagnostic, starts the GUI briefly, and verifies the
menu hierarchy.

## Manual GUI run

```powershell
mvn -pl use-assembly -am clean package
Expand-Archive .\use-assembly\target\use-7.1.1.zip .\use-assembly\target\bdi-dist
java -jar .\use-assembly\target\bdi-dist\use-7.1.1\lib\use-gui.jar -nr -H=.\use-assembly\target\bdi-dist\use-7.1.1
```

Open `Plugins > AgentSpeak > Hello BDI Plugin`. The action is enabled even when
no UML/OCL model is loaded.

## Project guides and release evidence

- End-user GUI walkthrough: `docs/project/USER_GUIDE.md`
- Maintainer and extension guide: `docs/project/DEVELOPER_GUIDE.md`
- Install and distribution guide: `docs/project/PLUGIN_INSTALL_GUIDE.md`
- Third-party scope and notices: `docs/project/THIRD_PARTY_NOTICES.md`
- Package and clean-clone evidence: `docs/project/evidence/release-package.md`

Create a committed-source backup and manifest with:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\backup-thesis-artifacts.ps1
```
