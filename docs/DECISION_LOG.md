# Decision Log

## ADR-0001 - USE plugin integration baseline

- Status: Accepted
- Date: 2026-08-03
- Scope: Phase 0 technical spike only

### Context

The thesis prototype must extend USE without changing its parser, AST, or GUI
core. The plugin contract, menu integration, session access, view creation, and
dependency-loading behavior therefore had to be established from the checked-out
USE 7.1.1 source before adding Jason.

Baseline used for this spike:

- USE version: `7.1.1` from root `pom.xml`.
- USE commit: `a455e90e7e68c10c53c04b86647c1ce79ff4610c`.
- Branch: `thesis/bdi-plugin`.
- Java: Oracle JDK `21.0.5`; Maven: `3.9.9`.
- Root POM selects Java 21, while `use-core` and `use-gui` explicitly compile
  with source/target 14. The new plugin follows the root Java 21 baseline.

### Verified findings

1. **Discovery and descriptor.** USE resolves the plugin directory as
   `<USE_HOME>/lib/plugins` (`use-core/src/main/java/org/tzi/use/config/Options.java:450`).
   `MainPluginRuntime` scans every `.jar` and registers it
   (`use-gui/src/main/java/org/tzi/use/runtime/MainPluginRuntime.java:70` and
   `:82`). `PluginRegistry` requires a root entry named exactly
   `useplugin.xml` (`use-gui/src/main/java/org/tzi/use/runtime/util/PluginRegistry.java:42`
   and `:77`). Existing shipped plugin JARs were inspected and use this same
   descriptor name.

2. **Lifecycle.** The startup class implements `IPlugin`, whose contract is
   `getName()` plus `run(IPluginRuntime)`
   (`use-gui/src/main/java/org/tzi/use/runtime/IPlugin.java:12` and `:31`). The
   runtime obtains the startup class from the JAR manifest `Main-Class`
   (`use-gui/src/main/java/org/tzi/use/runtime/util/PluginClassLoader.java:59`).
   For a GUI action, startup is lazy: `PluginAction` calls `run(...)` while
   creating the action delegate
   (`use-gui/src/main/java/org/tzi/use/runtime/gui/impl/PluginAction.java:94`).
   No stop/unload lifecycle interface was found.

3. **Menu action.** A descriptor action class implements
   `IPluginActionDelegate`. USE creates the top-level `Plugins` menu and action
   proxies in `MainWindow`
   (`use-gui/src/main/java/org/tzi/use/gui/main/MainWindow.java:451` and `:458`).
   The descriptor's `menu` value is a submenu under `Plugins`, not an arbitrary
   top-level menu. The Phase 0 action is therefore exposed as
   `Plugins > AgentSpeak > Hello BDI Plugin`.

4. **Current session, model, and state.** `IPluginAction` provides both the
   current `Session` and `MainWindow`
   (`use-gui/src/main/java/org/tzi/use/runtime/gui/IPluginAction.java:29` and
   `:36`). The verified access chain is `pluginAction.getSession().system()`,
   then `MSystem.model()` or `MSystem.state()`
   (`use-core/src/main/java/org/tzi/use/main/Session.java:96` and
   `use-core/src/main/java/org/tzi/use/uml/sys/MSystem.java:175`, `:182`). Code
   must call `Session.hasSystem()` before `system()`.

5. **Custom views.** There is no descriptor-level custom-view extension point.
   A plugin action can construct a Swing component implementing `View`, wrap it
   in `ViewFrame`, and call `MainWindow.addNewViewFrame(...)`. Evidence:
   `use-gui/src/main/java/org/tzi/use/gui/views/View.java:29`,
   `use-gui/src/main/java/org/tzi/use/gui/main/ViewFrame.java:39`, and
   `use-gui/src/main/java/org/tzi/use/gui/main/MainWindow.java:783`.

6. **External dependencies.** `PluginClassLoader` builds a `URLClassLoader`
   from registered plugin JAR URLs
   (`use-gui/src/main/java/org/tzi/use/runtime/util/PluginClassLoader.java:34`).
   A separate dependency JAR is still scanned as a plugin and rejected when it
   lacks `useplugin.xml`; it is not a supported dependency declaration
   mechanism. The shipped `validator-7.1.1.jar` was inspected with `jar tf` and
   contains dependency packages such as Guava and SAT4J. Therefore future Jason
   integration will use a shaded/fat plugin JAR, with license notices and
   resource-merge verification. Jason itself is intentionally not added in
   Phase 0.

7. **Reload behavior.** Plugin scanning occurs once in `Main.main(...)` before
   GUI creation through `MainPluginRuntime.run(...)`. No reload or unload API was
   found. During development, rebuild/copy the JAR and restart USE.

### Decision

- Keep the thesis extension as the in-repository Maven module
  `use-bdi-plugin`; do not change USE core for the plugin shell.
- Package `use-bdi-plugin-7.1.1.jar` into the distribution through the assembly
  dependency set (`use-assembly/src/assembly/assembly.xml:80`).
- Use `useplugin.xml`, manifest `Main-Class`, `IPlugin`, and
  `IPluginActionDelegate` exactly as implemented by USE 7.1.1.
- Add future views programmatically from an action through `ViewFrame` and
  `MainWindow.addNewViewFrame(...)`.
- Package Jason transitives inside the plugin artifact in Phase 1; do not place
  ordinary dependency JARs beside the plugin and assume they are loaded.

### Validation evidence

- `mvn clean test`: passed for the five-module reactor after adding the plugin.
- `mvn -pl use-bdi-plugin -am package`: passed; one plugin unit test passed.
- `use-bdi-plugin/scripts/smoke.ps1`: built the distribution and printed
  `GUI_SMOKE_OK: Plugins > AgentSpeak > Hello BDI Plugin`.
- `mvn -pl use-bdi-plugin -am verify`: reached the pre-existing `use-gui`
  `ShellIT`, whose fork exits without the Failsafe handshake. This baseline
  integration-test issue is outside the Phase 0 plugin change; `mvn clean test`
  and the dedicated package/GUI smoke are the accepted Phase 0 gates.

### Open documentation issue

`docs/00_PROJECT_CONTEXT.md` was requested by the working protocol but is not
present anywhere in the repository as of this decision. No replacement content
was inferred. Add or restore the authoritative file before Phase 1 planning.

## ADR-0002 - Jason parser boundary and plugin packaging

- Status: Accepted
- Date: 2026-08-03
- Scope: Phase 1 single-file valid-ASL parser slice

### Context

The first importer slice needs a real AgentSpeak parser without coupling the
future BDI domain model and rules to Jason's mutable syntax tree. ADR-0001 also
established that USE does not provide a dependency declaration mechanism for
plugin JARs, so external runtime dependencies must be inside the plugin artifact.

### Verified findings

1. Maven Central artifact `io.github.jason-lang:jason-interpreter:3.3.0` declares
   JADE `4.3`, `javax.json-api` `1.1.4`, and GlassFish JSON `1.1.4` as runtime
   dependencies. Its POM declares LGPLv3. JADE declares LGPL, and the GlassFish
   JSON parent POM declares CDDL 1.1 or GPLv2 with Classpath Exception.
2. Jason 3.3.0 source and bytecode both expose `Agent.initAg()`,
   `Agent.parseAS(File)`, `Agent.getInitialBels()`, `Agent.getInitialGoals()`,
   and `Agent.getPL()`; `PlanLibrary.size()` supplies the plan count. The adapter
   uses these exact public APIs.
3. `Agent.initAg()` registers the agent with Jason's web mind inspector by
   default. The public `Agent.setConsiderToAddMIForThisAgent(false)` must be
   called first so parsing does not start an HTTP server.
4. GlassFish JSON 1.1.4 already contains the `javax.json` API classes. Shading
   the separate `javax.json-api` artifact produced duplicate classes, so that
   transitive artifact is excluded while the GlassFish implementation remains.

Evidence was obtained from the downloaded 3.3.0 artifact, source JAR, POMs,
`javap`, Maven dependency tree, and the packaged plugin JAR. No unverified Jason
API is assumed.

### Decision

- Pin Jason at `3.3.0` through the `jason.version` Maven property.
- Keep all Jason types inside `JasonAslParserAdapter`. Return only the immutable,
  Java-only `AslParseSummary`; a later slice will map the syntax tree into the
  project BDI IR.
- Build the plugin as an unrelocated shaded JAR. Exclude signatures, module
  descriptors, duplicate manifests, and the redundant `javax.json-api` artifact.
- Package `META-INF/THIRD-PARTY-NOTICES.txt` in the plugin JAR for Jason, JADE,
  and GlassFish JSON. The complete release-wide license audit remains a separate
  release checklist item.
- Generate the reported parser version from the same Maven property used by the
  dependency, avoiding a second hard-coded version in Java.
- Defer syntax diagnostics, source locations, unsupported-construct policy,
  multi-file import, and partial-success behavior to explicit later slices.

### Validation evidence

- `mvn -pl use-bdi-plugin -am test`: passed with three plugin tests.
- `mvn -pl use-bdi-plugin clean package`: passed; the valid fixture produced one
  belief, one goal, and one plan, without starting the mind inspector.
- Packaged JAR inspection found `useplugin.xml`, Jason's `Agent.class`, and the
  parser adapter, with the plugin `Main-Class` preserved.
- `use-bdi-plugin/scripts/smoke.ps1` parsed the fixture using the shaded JAR
  copied into the assembled distribution, verified the third-party notice, then
  passed the USE GUI menu smoke.

## ADR-0003 - AgentSpeak syntax diagnostic boundary

- Status: Accepted
- Date: 2026-08-04
- Scope: Phase 1 single-file syntax-error slice

### Context

Invalid AgentSpeak input must produce evidence suitable for the future Problems
view and reports without leaking Jason parser types into domain or presentation
code. The existing adapter wrapped every parser failure in a generic exception,
which lost structured source position and severity.

### Verified findings

1. Jason 3.3.0's generated `ParseException` exposes `currentToken`. Its source
   documents that `currentToken` is the last successfully consumed token and
   `currentToken.next` is the first error token.
2. Jason's generated `Token` exposes one-based `beginLine` and `beginColumn`.
   Parsing the checked-in invalid fixture directly with Jason reports `;` at
   line 3, column 8, matching those fields and the parser message.
3. The rule catalog already reserves `ASL-001` for a Jason parse error and
   assigns Error severity. A second parse-error identifier would fragment
   reporting.
4. Jason can also construct message-only `ParseException` instances with no
   token. Position therefore needs an explicit unknown representation rather
   than guessed values.

### Decision

- Catch `ParseException` specifically inside `JasonAslParserAdapter`; keep all
  Jason exception/token access in that adapter.
- Expose immutable Java-only `AslDiagnostic` with code, severity, source,
  one-based line/column, and message. Use `0/0` when Jason provides no error
  token and expose `hasSourcePosition()` to distinguish that case.
- Attach the diagnostic to `AslParseException` while preserving generic
  missing-file and non-syntax failures without a diagnostic. A later multi-file
  importer can collect these diagnostics into a partial-success result.
- Use catalog ID `ASL-001` and `ERROR` severity. Do not mark the future rule SPI,
  IR `SourceSpan`, or unsupported-feature diagnostic as complete in this slice.

### Validation evidence

- `mvn -pl use-bdi-plugin test`: passed with four tests.
- `JasonAslParserAdapterTest` verifies exact code, severity, normalized source,
  line 3, column 8, message evidence, and separation from missing-file errors.
- `use-bdi-plugin/scripts/smoke.ps1` passed the distribution/fat-JAR diagnostic
  check and the existing USE GUI menu smoke.

## ADR-0004 - Multi-file importer result and interim failure behavior

- Status: Superseded by ADR-0006
- Date: 2026-08-04
- Scope: Phase 1 multi-file valid-import slice

### Context

The technical design calls for an `AslImporter` that accepts multiple paths,
while the existing API only parsed one file at a time. The new orchestration
must remain independent of Jason types and must not silently drop an invalid
file before the project defines a complete partial-success policy.

### Decision

- Introduce `AslImporter.importFiles(List<Path>)` and immutable Java-only
  `AslImportResult` containing ordered `AslParseSummary` values.
- Validate and copy the complete input list before parsing. Parse each file with
  a fresh Jason agent state inside the adapter and preserve input order.
- Derive aggregate belief, goal, and plan counts from per-file summaries rather
  than storing duplicate totals that could become inconsistent.
- Use fail-fast behavior as an explicit interim boundary: the first
  `AslParseException`, including `ASL-001`, is propagated and no partial result
  is returned. Do not mark partial-success policy complete until a result model
  can carry successes and diagnostics together.

### Consequences

- Callers can import any number of valid files without depending on Jason AST.
- Empty input deterministically produces an empty immutable result.
- One invalid file prevents publication of earlier summaries for now. A later
  ADR may supersede this behavior when partial success is implemented.

### Validation evidence

- `mvn -pl use-bdi-plugin test`: passed with seven tests.
- `JasonAslImporterTest` verifies input order, immutable summaries, aggregate
  counts, and propagation of the invalid file's structured diagnostic.
- `use-bdi-plugin/scripts/smoke.ps1` passed multi-file import through the shaded
  distribution JAR, the existing diagnostic check, and the USE GUI menu smoke.

## ADR-0005 - Prototype files belong to fixture or case-study boundaries

- Status: Accepted
- Date: 2026-08-04
- Scope: Repository baseline cleanup

### Context

The checklist requires prototype artifacts to stay out of the repository root,
but the technical notes named several SmartQueue files that were not present in
the checked-out worktree. The only actual root prototype was
`Smart_manager_agent.asl`.

### Verified findings

- Root inventory before the change contained `Smart_manager_agent.asl` and no
  `SmartQueue.use`, `.cmd`, `.clt`, or additional prototype artifact matching
  those notes.
- The AgentSpeak file is valid Jason 3.3.0 input and parses to nine beliefs, one
  initial goal, and five plans.

### Decision

- Move the actual prototype to
  `use-bdi-plugin/src/test/resources/fixtures/smartqueue/` while preserving its
  filename and Git history.
- Protect both the fixture path and root absence with a parser unit test and
  the repository smoke script.
- Do not create placeholder files for artifacts absent from this checkout.
  Restore or migrate those artifacts only when they are supplied.

### Validation evidence

- `mvn -pl use-bdi-plugin test`: passed with eight tests, including the migrated
  prototype parse.
- `use-bdi-plugin/scripts/smoke.ps1` checks the root/fixture boundary before
  running the package, parser, diagnostic, third-party-notice, and GUI gates;
  it passed on 2026-08-04.

## ADR-0006 - Multi-file partial-success import result

- Status: Accepted
- Date: 2026-08-04
- Scope: Phase 1 importer diagnostics

### Context

ADR-0004 intentionally used fail-fast behavior while the result model could
only expose successful summaries. That behavior loses successful files when a
later source is invalid and leaves callers without one report for the complete
selection.

### Decision

- Supersede the interim fail-fast behavior. `AslImporter.importFiles` attempts
  every validated source and returns one `AslImportResult`.
- Keep successful `AslParseSummary` values in input order. Failed files do not
  create summaries; they create immutable `AslDiagnostic` values in encounter
  order.
- Preserve parser syntax diagnostics as `ASL-001`. Convert missing-file and
  other `AslParseException` failures without a diagnostic into
  `ASL-IMPORT-001` with normalized source and position `0/0`.
- Keep input validation strict: a null input list or null source is rejected
  before parsing rather than silently converted into a diagnostic.
- Keep the result Java-only and derive aggregate counts from successful
  summaries. Expose `hasErrors()` for callers that need a simple gate.

### Consequences

- UI/reporting code can show successful imports and actionable file errors in a
  single result without losing later valid files.
- Empty input still returns an empty result with no diagnostics.
- Unsupported AgentSpeak syntax remains a future diagnostic/catalog slice and
  is not silently ignored by this policy.

### Validation evidence

- `mvn -pl use-bdi-plugin test` verifies valid-invalid-valid ordering, immutable
  diagnostics, missing-file conversion, and the existing parser contracts.
- `use-bdi-plugin/scripts/smoke.ps1` verifies the partial result and `ASL-001`
  location through the shaded plugin JAR in the assembled distribution.

## ADR-0007 - Source locations at the Jason adapter boundary

- Status: Accepted
- Date: 2026-08-04
- Scope: Phase 1 source-location extraction

### Context

The Phase 1 importer must preserve source evidence for the future BDI tree and
Problems/report views. The full normalized IR and its `SourceSpan` model are not
implemented yet, so this slice needs a small Java-only boundary without leaking
Jason AST types.

### Verified findings

- Jason 3.3.0 exposes `Term.getSrcInfo()` and `SourceInfo.getSrcFile()`,
  `getBeginSrcLine()`, and `getEndSrcLine()`.
- The Jason parser source attaches `SourceInfo` to parsed literals and plans;
  plans use a begin/end line range while literals use their declaration line.
- `SourceInfo` has no column fields. Declaration columns therefore cannot be
  reported from this API without inventing data; parser diagnostics remain the
  separate source of one-based columns for syntax errors.

### Decision

- Add Java-only `AslSourceElement` and `AslSourceLocation` values for initial
  beliefs, initial goals, and plans. Each value carries normalized source path,
  a stable text subject, and begin/end line values.
- Add an immutable `sourceLocations` list to `AslParseSummary`; order it by
  begin line, end line, and element kind so callers get deterministic source
  order across the separate Jason collections.
- Represent unavailable declaration positions as `0/0` and retain the location
  entry rather than silently dropping source evidence.
- Keep Jason classes confined to `JasonAslParserAdapter`. Do not mark the future
  normalized IR `SourceSpan`, column extraction, or golden IR serialization as
  complete in this slice.

### Consequences

- The future UI and IR transformer can link the first importer-level summaries
  to source lines without depending on Jason types.
- Line ranges are available for top-level declarations; token-level columns and
  nested-step spans require a later adapter/IR slice.
- Existing five-argument `AslParseSummary` construction remains source
  compatible through an empty-location overload.

### Validation evidence

- `mvn -pl use-bdi-plugin test`: passed with nine tests, including the minimal
  fixture line-range and immutable-list assertions.
- `use-bdi-plugin/scripts/smoke.ps1`: packaged smoke passed source locations at
  lines `1`, `2`, and `4-5` through the shaded plugin JAR, alongside the partial
  import, diagnostic, and GUI menu gates.

## ADR-0008 - Parser version metadata in import reports

- Status: Accepted
- Date: 2026-08-04
- Scope: Phase 1 parser-version reporting slice

### Context

`AslParseSummary` already records the Jason parser version for each successful
file, but the importer had no report-level metadata object. The next slice must
make that version visible without duplicating summaries/diagnostics or claiming
the full JSON/HTML report contract.

### Decision

- Add immutable Java-only `AslImportReport` around `AslImportResult`.
- Derive `parserVersions` from successful summaries, remove duplicates while
  preserving encounter order, and expose the list as an immutable value.
- Report no parser version for empty or all-failed imports; do not infer a
  version for a file that did not produce a parse summary.
- Keep `AslImportResult.toReport()` as the construction boundary. Do not add
  plugin/USE versions, model hashes, persistence, or JSON/HTML serialization in
  this importer slice.

### Consequences

- A future report exporter can include parser-version evidence without reading
  Jason classes or duplicating importer state.
- The list supports a future mixed-parser report while the current Jason adapter
  produces only `3.3.0`.
- Formal report export and release metadata remain separate checklist tasks.

### Validation evidence

- `mvn -pl use-bdi-plugin test`: passed with eleven tests, including repeated,
  distinct, and empty parser-version report cases.
- `use-bdi-plugin/scripts/smoke.ps1`: packaged smoke found parser version
  `3.3.0` through the shaded plugin JAR and passed the existing partial-import,
  source-location, diagnostic, and GUI menu gates.

## ADR-0009 - Metadata-only AgentModel root

- Status: Accepted
- Date: 2026-08-04
- Scope: Phase 1 first IR vertical slice

### Context

The Jason adapter now exposes immutable Java-only `AslParseSummary` values, but
the project still had no IR root connecting successful imports to later BDI
nodes. The next slice must establish that boundary without introducing Jason
AST types, inventing source semantics, or prematurely defining the child IR
models tracked separately in the completion checklist.

### Decision

- Add immutable `AgentModel` as a metadata-only root with normalized source
  path, parser version, and non-negative belief, goal, and plan counts.
- Use the source path as the available file identity. Do not synthesize an
  AgentSpeak agent name because the current importer summary has no declared
  agent identity field.
- Add `AslAgentModelNormalizer` as the importer-to-IR mapping boundary. It
  accepts one summary or an import result, preserves successful-file order, and
  returns no model for failed files because those remain diagnostics.
- Keep the root free of Jason classes. Child models, `SourceSpan`, unsupported
  feature representation, source-location-to-IR mapping, and serialization
  remain separate slices.

### Consequences

- Import/report and future UI code can identify each successful source and show
  its first structural counts through a stable IR value.
- The root is intentionally not a semantic BDI tree; no child-model checklist
  item is marked complete by this ADR.
- The normalizer currently consumes importer DTOs. A later IR consolidation can
  move shared source evidence types without changing the Jason adapter contract.

### Validation evidence

- `mvn -pl use-bdi-plugin test`: passed with thirteen tests, including two
  `AgentModel` normalization tests.
- `mvn -pl use-bdi-plugin clean package`: passed and shaded Jason `3.3.0`.
- `use-bdi-plugin/scripts/smoke.ps1`: passed `AGENT_MODEL_SMOKE_OK` through the
  assembled distribution and also passed the existing USE GUI menu smoke.

## ADR-0010 - Jason AST to normalized IR tree

- Status: Accepted
- Date: 2026-08-04
- Scope: Phase 1 IR completion slice

### Context

The metadata-only `AgentModel` root was not sufficient for the planned BDI
tree, mapping, or consistency layers. The next coherent boundary is to map the
verified Jason 3.3.0 AST into immutable domain nodes while keeping Jason out of
the domain package and preserving evidence for constructs not normalized yet.

### Verified findings

- Jason 3.3.0 exposes the exact parser-side accessors used here:
  `Agent.getInitialBels()`, `Agent.getInitialGoals()`, `Agent.getPL()`,
  `Plan.getTrigger()`, `Plan.getContext()`, `Plan.getBody()`,
  `PlanBody.getBodyType()`, `getBodyTerm()`, `getBodyNext()`,
  `Trigger.getOperator()`, `getType()`, `getLiteral()`, and
  `Term.getSrcInfo()`.
- `PlanBody.BodyType` includes action, internalAction, achieve, achieveNF,
  test, constraint, add/delete belief variants, and delAddBel; these are mapped
  explicitly. Unknown/null branches create `UnsupportedFeature` evidence.
- Jason generates process-dependent labels such as `p__13[source(self),...]`
  for unlabeled plans. Those labels are not source identity and are omitted.

### Decision

- Add immutable Java-only IR records for beliefs, goals, plans, triggers,
  context expressions, plan steps, terms, source spans, and unsupported
  features. Use sealed interfaces for `TermModel`, `ContextExpr`, and
  `PlanStepModel` so new variants are explicit at compile time.
- Keep Jason-dependent traversal in package-private
  `JasonAstToIrNormalizer` under the importer boundary. Expose only
  `JasonAslParserAdapter.parseModel(Path)` to callers.
- Materialize source spans from Jason line ranges with `0/0` columns when
  unavailable. Do not synthesize columns or process-dependent plan labels.
- Represent unnormalized constructs as `ASL-002` `UnsupportedFeature` values in
  `AgentModel`; do not silently discard terms or body nodes. The future
  Problems/rule integration remains a separate analysis task.
- Serialize with `AgentModelJsonSerializer`, using an optional source root for
  portable relative paths. Golden tests compare canonical JSON, not absolute
  checkout paths.

### Consequences

- The importer now produces a usable BDI tree for the minimal and migrated Smart
  Queue fixtures without a Jason type crossing into `model/ir`.
- The tree supports later indexing and mapping work, while unsupported syntax,
  full diagnostics orchestration, and analysis rules remain explicit follow-up
  tasks.
- The current root keeps summary counts alongside materialized lists so partial
  or summary-only callers remain source-compatible; `isMaterialized()` exposes
  whether all child lists are present.

### Validation evidence

- `mvn -pl use-bdi-plugin test`: passed with eighteen tests, including full
  minimal-tree, Smart Queue-tree, hierarchy, source-span, and golden JSON tests.
- `mvn -pl use-bdi-plugin clean package`: passed and shaded Jason `3.3.0`.
- `use-bdi-plugin/scripts/smoke.ps1`: first attempt hit a transient Windows
  output-directory error while compiling unchanged `use-core`; the immediate
  rerun passed the complete assembly and printed `IR_TREE_SMOKE_OK`, importer
  diagnostics, third-party notices, and GUI menu smoke. Cleanup warned that the
  temporary directory was still locked by Windows.
