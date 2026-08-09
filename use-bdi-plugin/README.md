# USE BDI Plugin

This module contains the verified USE plugin shell for the thesis project. It
adds `Plugins > AgentSpeak > Hello BDI Plugin` and includes the first importer
slice: a Jason 3.3.0 adapter that parses multiple AgentSpeak files into ordered,
immutable, Jason-independent summaries. File selection, the full BDI IR, and
consistency checking are not implemented yet.

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
no checkout-specific JSON is committed. Mutants and reports remain open.

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
