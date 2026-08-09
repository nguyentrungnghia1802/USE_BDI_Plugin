# USE BDI Plugin Developer Guide

This guide describes the supported extension boundary for the thesis plugin.
The implementation follows the accepted decisions in
`docs/project/DECISION_LOG.md`; do not infer a new USE API from a convenient
call site.

The canonical specification and source-of-truth precedence are indexed in
`docs/project/README.md`.

## Module And Package Boundaries

`use-bdi-plugin` is an in-repository Maven module. Its important boundaries are:

- `importer/` - Jason 3.3.0 adapter and Java-only import result/diagnostics.
- `application/` - import orchestration and active-model project configuration
  discovery/composition.
- `model/ir/` - immutable normalized AgentSpeak IR and source spans.
- `index/` - derived BDI signatures, references, and call sites.
- `model/mapping/` and `persistence/` - mapping domain values and versioned
  `.bdimap.json` persistence.
- `validation/` and `rules/` - immutable issues, rule evaluation, OCL status,
  suppression, and reporting data.
- `use/` - read-only projection of the current USE model and snapshot.
- `ui/` - Swing actions, Explorer, Problems, and Mapping views.
- `src/test/resources/fixtures/` - parser, unsupported-syntax, USE, and Auction
  fixtures. Fixtures are not production runtime input.

`use-core` and `use-gui` are `provided` dependencies. The plugin JAR contains
Jason and its runtime dependencies because USE scans plugin JARs and has no
descriptor-level dependency declaration. The verified runtime tree is:

```text
io.github.jason-lang:jason-interpreter:3.3.0
|- net.sf.ingenias:jade:4.3
`- org.glassfish:javax.json:1.1.4
```

Jason types must stop at `JasonAslParserAdapter` and its normalizer boundary;
domain IR, mappings, rules, and reports must not import Jason classes. The
plugin must not mutate USE core source, the current USE model, or the current
system state during projection and validation.

## Build And Test Commands

Run commands from the repository root:

```powershell
# Fast plugin reactor test
mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test

# Build the shaded plugin and assembled USE distribution
mvn --batch-mode --no-transfer-progress -pl use-assembly -am package

# Package/parser/report/menu smoke
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1

# Auction fault-injection and evidence bundle
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\auction-evidence.ps1

# Clean committed-tree package check
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\clean-clone.ps1
```

`smoke.ps1` uses the extracted `use-gui.jar` as the parent classpath for the
direct GUI probe. This is intentional: Maven exec's isolated classloader does
not reproduce USE's plugin runtime classpath for GUI classes.

The root `mvn clean verify` gate is part of the supported release check. The
integration-mode invalid-specification behavior and portable shell-fixture path
comparison are documented in ADR-0019; do not suppress integration tests or
change normal CLI exit behavior to make the gate pass.

## Adding A Fixture And A Vertical Slice

1. Add the smallest valid input under the appropriate fixture directory.
2. Add a focused JUnit 5 test that imports or compiles the real fixture through
   the existing service boundary.
3. Assert immutable domain output, source identity, and diagnostic/rule
   evidence. Do not assert Jason implementation details outside the adapter.
4. Add a smoke marker only when the check is useful through the assembled
   distribution.
5. Add the result and any limitation to the checklist and decision log.

For a mapping fixture, use `MappingSuggestionService` for candidate generation,
confirm bindings explicitly, and use `MappingFileRepository` for persistence.
Do not check in a checkout-specific mapping JSON while source IDs still contain
absolute paths. For an OCL rule, keep `UseOclEvaluator` status as
`EVALUATED`, `COMPILE_ERROR`, or `EVALUATION_ERROR`; an unavailable bounded
effect remains `UNKNOWN`.

## Adding A UI Action Or View

The verified USE lifecycle is:

1. Register the action in `useplugin.xml` below the `Plugins` menu.
2. Implement the USE `IPluginActionDelegate` contract.
3. Obtain the current session from the action and check `Session.hasSystem()`
   before accessing `Session.system()`.
4. Use the read-only facade for `MSystem.model()`, `MSystem.state()`, objects,
   links, operations, and OCL constraints.
5. For a custom Swing view, wrap the component in USE's `ViewFrame` and add it
   through `MainWindow.addNewViewFrame(...)`.

There is no verified descriptor-level custom-view extension point and no
plugin reload lifecycle. Rebuild the JAR and restart USE after an action/view
change. The exact source evidence for these APIs is recorded in ADR-0001.

## Release And License Checks

The plugin's shaded JAR must contain `useplugin.xml`, the plugin main class,
Jason runtime classes, and `META-INF/THIRD-PARTY-NOTICES.txt`. Run:

```powershell
jar tf .\use-bdi-plugin\target\use-bdi-plugin-7.1.1.jar |
  Select-String 'useplugin.xml|jason/asSemantics/Agent.class|META-INF/THIRD-PARTY-NOTICES.txt'
```

The release-wide scope and exact license evidence are in
`docs/project/THIRD_PARTY_NOTICES.md`. Keep `COPYING` in the assembled USE
distribution.

## Project Configuration Boundary

`ImportBdiAction` passes `MSystem.model().filename()` to
`BdiProjectConfigurationLoader`. The loader uses only the model file's parent
directory and reads `.bdi-plugin/rules.json` plus `suppressions.json` there.
Missing files are defaults; malformed or unknown-rule configuration is a
blocking, user-visible error. Keep discovery outside domain rules and do not
introduce a CWD fallback, because USE can be launched from an unrelated folder.

## Known Limits

- Mapping suggestions are explainable candidates, not semantic proof.
- Absolute source IDs make generated mapping/report artifacts checkout
  specific.
- `ReportMain` is a serializer demonstration; the GUI has no live report export
  action yet.
- The mapping decoder does not currently reject every unknown JSON field.
- A BDI Explorer captures a USE projection and does not subscribe to later
  host model/snapshot changes.
- Current OCL/effect support is deliberately bounded; unsupported or unknown
  results remain visible.
- The House Building import is exploratory and optional.
- The full backup item still needs external slide/data artifacts that are not
  present in this checkout.
