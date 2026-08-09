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

## ADR-0011 - BDI index and explorer UI vertical slice

- Status: Accepted
- Date: 2026-08-04
- Scope: Section 4 BDI index/metamodel and first four Section 5 UI checklist items

### Context

The normalized IR is now materialized, but the next ten checklist tasks need a
small end-to-end path from imported `.asl` files to searchable BDI structure in
USE. The implementation must remain plugin-first and must not modify USE core
or invent semantic resolution that belongs to the future USE adapter.

### Verified constraints

- USE's actual custom-view path is a Swing component implementing `View`,
  wrapped in `ViewFrame`, and registered with
  `MainWindow.addNewViewFrame(...)`. There is no descriptor-level custom-view
  extension point; this is established in ADR-0001 and the checked-out USE
  7.1.1 source.
- The current IR contains source-spanned literal, compound, variable, list,
  set, arithmetic, context, plan-step, and unsupported-feature nodes. The
  index therefore consumes IR only and does not depend on Jason AST classes.
- Jason's `PlanLibrary` rejects duplicate explicit labels while parsing. A
  duplicate-label detector can still validate normalized models from multiple
  import sources or future adapters, so its unit test constructs duplicate
  explicit labels at the IR boundary rather than claiming Jason accepts an
  invalid source file.

### Decision

- Add immutable `BdiIndex` values built by `BdiIndexBuilder`. A
  `PredicateSignature` is the exact functor/arity pair; it preserves Jason's
  spelling, including the leading dot on internal actions such as `.send` and
  `.print`.
- Index a goal to plans whose trigger is `ACHIEVE + ADD` with the same
  functor/arity. Context truth is intentionally not part of static support;
  applicability belongs to a later snapshot/rule phase.
- Index external and internal action steps as `ActionCallSite` values with
  one-based plan-step indexes, optional signatures for dynamic terms, and
  source spans. Index predicate occurrences separately by kind so beliefs,
  goals, triggers, contexts, actions, tests, and belief updates remain
  traceable.
- Use a conservative syntactic reference policy. The first argument of
  `.send` is an `AGENT` reference and named literal/compound terms inside
  predicate or action arguments are `OBJECT` references. Dynamic variables are
  retained with a `dynamic` flag. These are unresolved symbols, not claims
  about USE classes or objects.
- Detect duplicate non-blank plan labels within each imported source and
  retain all source spans. Do not treat Jason-generated `p__N[...]` labels as
  source labels; the IR normalizer already removes them.
- Record normalized metamodel version `0.1.0` in `BdiMetamodelVersion` and in
  every `BdiIndex` snapshot.
- Add `BdiImportService` for independent full-tree parsing with partial success,
  `BdiImportWorker` for `SwingWorker` background execution, and
  `BdiExplorerView` for file/belief/goal/plan/step tree plus detail/source
  excerpt. Add the multi-select `.asl` chooser as
  `Plugins > AgentSpeak > Import AgentSpeak...` and keep the existing Hello
  action as the plugin loading smoke signal.

### Consequences and limits

- Index results are deterministic, immutable, and usable by later mapping and
  rule services without importing Jason or Swing into the domain index.
- Failed files remain diagnostics and do not silently disappear; successful
  files still produce a tree and index.
- The view is an overlay and does not mutate the current USE model/session/state.
- Problems table, filtering/grouping, re-import/watch behavior, USE adapter,
  mapping, and consistency rules remain separate tasks. Object/agent
  references are intentionally unresolved until the USE adapter exists.

### Validation evidence

- `mvn -pl use-bdi-plugin -am test`: passed with 26 tests, including index
  lookup/immutability, Smart Queue agent/object references, partial import,
  SwingWorker completion, chooser configuration, and tree/source-detail UI.
- `powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1`:
  passed the assembly, shaded-plugin `BDI_INDEX_SMOKE_OK`, third-party notices,
  and GUI menu smoke for both AgentSpeak actions. Windows left the temporary
  smoke directory locked during cleanup, matching the existing environment
  limitation recorded by earlier smoke slices.

## ADR-0012 - Problems, re-import, and read-only USE adapter slice

- Status: Accepted
- Date: 2026-08-04
- Scope: The next ten checklist items: three Section 5 UI items and seven Section 6 USE adapter items

### Context

The BDI explorer already retained normalized models, diagnostics, and indexes,
but there was no single Problems presentation, changed-source workflow, or
verified bridge to the live USE model/state. The implementation must remain
plugin-first, preserve partial import evidence, and avoid modifying USE core or
guessing APIs.

### Verified API evidence

- `use-core/src/main/java/org/tzi/use/uml/sys/MSystem.java` exposes
  `model()`, `state()`, and `varBindings()`.
- `use-core/src/main/java/org/tzi/use/uml/mm/MModel.java` exposes
  `classes()`, `associations()`, `classInvariants()`, and `filename()`;
  `MClass`/`MClassifier` expose direct attributes and operations;
  `MOperation` exposes `paramList()`, `preConditions()`, and
  `postConditions()`.
- `use-core/src/main/java/org/tzi/use/uml/sys/MSystemState.java` exposes
  `allObjects()` and `allLinks()`; `MObject.state(...)` and
  `MObjectState.attributeValueMap()` provide current object values.
- `use-core/src/main/java/org/tzi/use/parser/ocl/OCLCompiler.java` has the
  verified overload `compileExpression(MModel, MSystemState, String, String,
  PrintWriter, VarBindings)`. `Evaluator` has the verified overload
  `eval(Expression, MSystemState, VarBindings)`.

### Decision

- Represent Problems as immutable rows with code, severity, normalized source,
  optional line/column, message, and group. Map `ASL-002` to WARNING rather
  than dropping recognized-but-unsupported syntax.
- Keep source change detection in a UI-independent `BdiSourceTracker` using
  normalized paths and file existence/size/last-modified stamps. Re-import the
  complete selected set after a change; this preserves unchanged successful
  models in the snapshot. Guard asynchronous callbacks with an import generation
  token.
- Keep USE integration read-only and return plugin-owned immutable `Uml*Ref`
  records. Include classes, direct attributes, association ends, operations and
  parameters, pre/postconditions, class invariants, current objects and links.
- Return explicit OCL statuses and compiler diagnostics. Do not convert a
  compile/evaluation failure into a boolean result or silently suppress it.
- Compute the model/state identity from a length-delimited canonical projection
  and SHA-256, with deterministic sorting of unordered USE collections.

### Consequences and limits

- The Problems tab is useful for import/index evidence but is not yet the
  consistency-rule engine; mapping and rule phases remain separate.
- The adapter can enumerate the current state and evaluate an expression, but it
  intentionally does not resolve BDI references, mutate USE state, or perform
  whole-project OCL/consistency orchestration.
- `docs/00_PROJECT_CONTEXT.md` is still absent and remains a documentation
  blocker recorded from the baseline.

### Validation evidence

- `mvn -pl use-bdi-plugin test`: passed with 32 tests, including Problems
  filtering/grouping, source stamp/re-import, USE fixture projection, stable
  fingerprint, pre/post extraction, and OCL compile/evaluation statuses.
- `mvn -pl use-bdi-plugin -am -DskipTests compile`: passed for `use-core`,
  `use-gui`, and `use-bdi-plugin`. A reactor test attempt was separately blocked
  in `use-gui` by a Windows file lock on its existing `target/test-classes`
  fixture; the plugin-only test gate passed afterward.

## ADR-0013 - Conservative mapping domain, suggestions, editor, and persistence

- Status: Accepted
- Date: 2026-08-04
- Scope: First ten Section 7 mapping checklist items

### Context

The repository now has normalized AgentSpeak IR/index values and a read-only
USE projection, but no way to represent, suggest, manually confirm, or persist
cross-model links. The mapping implementation must stay plugin-first and must
not make the domain depend on Swing, Jason AST classes, or USE core objects.

### Verified implementation evidence

- `use-bdi-plugin/src/main/java/org/tzi/use/plugins/bdi/model/ir/AgentModel.java`
  and `index/BdiIndex.java` are Java-only immutable inputs. The USE-side
  `use/UseModelSnapshot.java` and `Uml*Ref` records are also plugin-owned
  immutable projections established by ADR-0012.
- `model/mapping/MappingBinding.java` and `MappingDocument.java` define the
  binding schema and replacement identity. A binding key is the mapping kind
  plus source identity; `MappingDocument.upsert(...)` replaces that source's
  target without mutating an existing document.
- `model/mapping/MappingSuggestionService.java` generates candidates using
  normalized names, signature/arity, and explicit reason strings. Source IDs
  include normalized paths and plan-step/receiver locations; these are stable
  within the imported source snapshot and are not semantic claims.
- `persistence/MappingFileRepository.java` and `MappingJsonCodec.java` write
  and read deterministic UTF-8 JSON with schema/metamodel/fingerprint metadata,
  optional expressions, evidence, duplicate-key checks, and malformed-input
  errors. No new JSON dependency was introduced.
- `ui/MappingEditorPanel.java` provides Add/update, Apply, Remove, Save, and
  Load controls. `BdiExplorerView.java` mounts it as the `Mapping` tab; Swing
  does not enter the mapping domain classes.
- `ImportBdiAction.java` uses the verified `Session.hasSystem()` /
  `Session.system()` chain to snapshot the current USE model before opening the
  explorer. After import, `BdiExplorerView` passes that immutable snapshot and
  the imported BDI index to `MappingSuggestionService`; a no-model fallback
  keeps the manual editor usable without a loaded `.use` model.

### Decision

- Use a generic versioned binding document with six initial kinds:
  Agent->Class, Agent->Object, Action->Operation, Parameter,
  Receiver->Object, and Belief->Attribute.
- Keep suggestions deterministic and explainable. Exact normalized names and
  matching operation arity receive the strongest score; weaker name/token
  matches are presented as candidates and require manual confirmation.
- Treat `.send` receiver links and initial-belief attribute links as the first
  conservative policies. Do not infer runtime ownership, execute OCL, or mutate
  the current USE state in this slice.
- Keep persistence and editor concerns outside the immutable domain records and
  use `kind + source` replacement semantics so the editor can correct a prior
  target without duplicate bindings.

### Consequences and limits

- The first ten mapping checklist items are implemented and testable without
  USE core changes. The same document can carry a USE fingerprint for a later
  stale-mapping check.
- Mapping candidates are suggestions, not proof of semantic consistency. Stale
  mapping detection, richer expressions, semantic resolution, and consistency
  rules remain open tasks.

### Validation evidence

- `mvn -pl use-bdi-plugin -am test` passed with 41 tests, including
  AgentSpeak/USE suggestion scoring, BDI-to-editor wiring,
  parameter/receiver/belief candidates, immutable binding replacement,
  malformed/duplicate JSON rejection, and Swing apply/save/load.
- `mvn -pl use-bdi-plugin package` and the packaged distribution smoke script
  passed after this slice. No USE core source was changed.

## ADR-0014 - Static consistency rules and stale mapping policy

- Status: Accepted
- Date: 2026-08-04
- Scope: Section 7 stale mapping plus the first nine Section 8 checklist rows

### Context

The mapping slice had confirmed, versioned bindings but no verified way to
detect obsolete links or evaluate the documented static rule catalog. The next
vertical slice must remain plugin-first, preserve source evidence, and avoid
making claims that require executing OCL or mutating the active USE system.

### Verified implementation evidence

- `model/mapping/MappingSourceId.java` supplies the same stable source IDs to
  `MappingSuggestionService`, persistence bindings, and validation. This avoids
  duplicating an unverified key convention in multiple layers.
- `MappingStalenessDetector` checks the document metamodel version, USE
  fingerprint, retained BDI sources, and immutable `UseModelSnapshot` targets.
  It emits `SOURCE_MISSING` or `TARGET_MISSING` for obsolete bindings and keeps
  version/fingerprint mismatch as review evidence.
- `validation/ConsistencyRule.java` is the actual SPI with `id`, `phase`, and
  `evaluate(ValidationContext)`. `ValidationOrchestrator` sorts phases and
  issues deterministically; it receives only import snapshot values, mapping
  values, and an optional immutable USE projection.
- `StandardConsistencyRules` implements exactly `ASL-001/002`,
  `BDI-001/002/003/004`, `REF-001/002`, `MAP-001/002/003`,
  `SIG-001/002/003`, and `OWN-001`. It reports evidence, source span, UML ref,
  suggested fix, severity, status, and certainty in `ConsistencyIssue`.
- `BdiExplorerView` refreshes Problems after import and after a mapping-editor
  document change. The flow only reads the snapshot; it does not compile OCL,
  invoke USE operations, or change the system state.

### Decision

- Report only missing BDI source and missing USE target as `MAP-003` errors.
  A fingerprint or BDI metamodel mismatch is a stale-review signal because a
  changed state fingerprint alone is insufficient evidence that a binding is
  invalid.
- Keep unresolved named object references and test predicates conservative:
  `REF-001`/`REF-002` use POTENTIAL certainty where current static information
  cannot prove runtime absence.
- Infer signature types only for literal String, Integer, Real, and Boolean
  terms. Unknown values yield `SIG-003` WARNING, not a guessed mismatch.
- Preserve manual mapping confirmation as the UI boundary. Rule evaluation is
  rerun automatically after confirmation but cannot write to USE.

### Consequences and limits

- The next ten prioritized checklist entries are complete without USE core
  changes. Rule/UI tests cover the 15 static rule IDs, stale source/target
  detection, USE projection use, and Problems refresh after mapping apply.
- `BEL-001`, `MSG-001`, OCL/CTX rules, suppression, reporting, and runtime
  behavior remain open. `docs/00_PROJECT_CONTEXT.md` is still absent from the
  repository and remains the existing documentation blocker.

### Validation evidence

- `mvn -pl use-bdi-plugin -am test` passed with 44 plugin tests on the full
  reactor, including `ValidationOrchestratorTest`,
  `MappingStalenessDetectorTest`, and the mapping-to-Problems UI regression.

## ADR-0015 - Evidence-preserving HTML report export

- Status: Accepted
- Date: 2026-08-09
- Scope: Section 10 HTML/CSV report and issue evidence/source export

### Context

The repository already had a small JSON/HTML metadata report, but the HTML
output did not contain the consistency findings that support thesis evidence.
The next reporting slice must remain deterministic, safe for arbitrary source
messages, and independent from rule execution or USE state mutation.

### Decision

- Extend plugin-owned `ReportData` with an immutable list of
  `ConsistencyIssue` records while preserving the existing summary constructor.
- Export the same normalized issue fields in HTML and JSON: rule ID, severity,
  status, certainty, source location, message, and evidence. Missing source
  spans are rendered as `unknown` rather than invented coordinates.
- Escape all HTML cell values, including messages and evidence, and keep the
  report UTF-8. The exporter serializes supplied results only; it does not
  invoke the validator or access the active USE session.

### Consequences and limits

- The `HTML or CSV report` checklist item and issue evidence/source reporting
  now have executable coverage without changing USE core.
- Model/mapping hashes, rule configuration, suppression sections, and a
  report UI action remain open reporting work. The existing `ReportMain` demo
  still supplies an empty issue list until the application report pipeline is
  connected to a live validation snapshot.

### Validation evidence

- `mvn -pl use-bdi-plugin test` passed with 47 tests, including HTML source and
  evidence rendering, HTML escaping, JSON issue serialization, and the existing
  OCL/validation regression suite.

## ADR-0016 - Canonical model and mapping hashes in reports

- Status: Accepted
- Date: 2026-08-09
- Scope: Section 10 model/mapping hash reporting

### Context

The report exporter now preserves issue evidence, but a report still needs a
reproducible identity for the USE input and confirmed mapping document. The
identity must be independent of Swing/Jason objects, stable across equivalent
binding orderings, and must not cause the exporter to read or mutate the active
USE session.

### Verified implementation evidence

- `use/UseModelFingerprint.java` is the existing SHA-256 implementation over
  the immutable `UseModelSnapshot` projection. It uses length-delimited fields
  and the snapshot's already normalized/sorted collections.
- `model/mapping/MappingFingerprint.java` computes SHA-256 over mapping schema,
  BDI metamodel, USE fingerprint, and bindings sorted by
  `MappingBinding.key()`. Binding fields and evidence use explicit markers and
  counts in the canonical stream.
- `report/ReportData.java` stores optional model/mapping hashes and rejects
  values that are not 64-character hexadecimal digests while retaining the
  existing constructor for callers without source models.
- `report/ReportExporter.java` and `report/HtmlReportExporter.java` serialize
  the supplied hashes as JSON metadata and HTML metadata rows. They do not
  recompute hashes, access `Session`, or execute validation.

### Decision

- Reuse `UseModelFingerprint` for the model identity and use
  `MappingFingerprint` for the mapping identity; both produce lowercase
  SHA-256 hex strings.
- Make binding order non-semantic by sorting on the persisted replacement key.
  Include schema/metamodel/USE metadata, binding target, expression, and
  evidence so a meaningful source change changes the identity.
- Keep hash computation outside exporters. The future application pipeline
  passes the computed values into `ReportData`; a summary-only caller exports
  `null` for hashes rather than an invented placeholder.

### Consequences and limits

- JSON/HTML report consumers can compare model and mapping identities without
  depending on USE core or Jason types.
- The current `ReportMain` demonstration has no live model/mapping pipeline and
  therefore emits null hashes. Rule configuration and suppression export remain
  open checklist items.

### Validation evidence

- `mvn -pl use-bdi-plugin clean test` passed with 49 tests, including stable
  mapping-fingerprint ordering, changed-binding detection, and JSON/HTML hash
  serialization. No USE core source was modified.

## ADR-0017 - Versioned rule configuration and fail-fast selection

- Status: Accepted
- Date: 2026-08-09
- Scope: Section 10 rule configuration

### Context

The static consistency catalog is implemented and the orchestrator currently
executes every registered rule. The project design already reserves
`.bdi-plugin/rules.json`, but there was no verified schema or way to select a
subset without changing rule code. Configuration must remain outside the
normalized IR and must not make rule execution depend on Swing or USE state.

### Verified implementation evidence

- `validation/RuleConfiguration.java` is an immutable schema `0.1.0` value with
  a sorted enabled-rule ID set. It validates rule-ID shape and duplicate IDs.
- `persistence/RuleConfigurationRepository.java` saves/loads UTF-8 JSON. The
  codec reuses the existing dependency-free JSON parser and rejects unknown
  fields, malformed arrays, duplicate IDs, and unsupported schema versions.
- `validation/ValidationOrchestrator.java` validates configured IDs against the
  actual supplied `ConsistencyRule` list before filtering. The no-argument
  path constructs the full 22-rule standard configuration, preserving prior
  behavior; unknown IDs fail rather than being silently ignored.
- `.bdi-plugin/rules.json` records the all-enabled default configuration for
  reproducible project setup.

### Decision

- Use an explicit `enabledRules` array in versioned `rules.json`. An empty
  array means no rules are selected; omission is not treated as implicit
  enablement.
- Inject `RuleConfiguration` into `ValidationOrchestrator` at the application
  boundary. Reject configuration IDs that do not exist in the supplied rule
  set, while allowing the repository to parse future valid-shaped IDs before a
  matching rule implementation is installed.
- Keep project-file discovery separate from the domain and orchestrator. The
  current explorer keeps the all-standard default because a verified project
  context/config lookup API has not been established.

### Consequences and limits

- Reproducible experiments can disable selected checks without editing Java
  code, and reports can later record the loaded configuration as metadata.
- Configuration loading is available as an explicit repository service; UI
  auto-discovery and report inclusion of the configuration remain follow-up
  work. No USE core source or current USE state is changed.

### Validation evidence

- `mvn -pl use-bdi-plugin test` passed with 53 tests, including configuration
  round-trip, malformed-file rejection, rule filtering, and unknown-ID
  rejection. No USE core source was modified.

## ADR-0018 - Source-fingerprint suppression and report transparency

- Status: Accepted
- Date: 2026-08-09
- Scope: Section 10 suppression export and application

### Context

The rule catalog and report now expose deterministic issues, but the existing
`suppressions.json` was only a placeholder and there was no verified way to
apply a suppression without losing the original evidence. The thesis report
must show which suppressions were configured and why, while suppression must
not hide a finding silently or mutate the USE model.

### Verified implementation evidence

- `validation/IssueFingerprint.java` hashes a length-delimited canonical source
  identity containing normalized path and begin/end positions. It does not
  read file contents or depend on Jason/USE objects.
- `validation/Suppression.java` validates rule IDs, SHA-256 fingerprints, and
  non-blank reasons. `validation/SuppressionService.java` matches rule ID plus
  source fingerprint, changes only `OPEN` issues to `SUPPRESSED`, and adds the
  reason to evidence.
- `persistence/SuppressionRepository.java` and its dependency-free codec read
  and write schema `0.1.0`, reject unknown fields and duplicate keys, and sort
  entries deterministically. The tracked project file contains an explicit
  empty list.
- `ValidationOrchestrator` applies suppressions after deterministic rule
  ordering. `ReportData`, JSON/HTML exporters, and `ReportMain` carry/export
  the configured suppression entries.

### Decision

- Persist suppression entries as `ruleId`, `sourceFingerprint`, and `reason`
  under a versioned `suppressions` array. A rule ID alone is never sufficient
  to suppress an issue.
- Define source fingerprint from normalized absolute path and source span
  positions for this slice. Require an exact match; do not use a broad pattern
  or message substring that could suppress unrelated findings.
- Preserve the original issue message/evidence and add a structured reason
  evidence line when status changes from `OPEN` to `SUPPRESSED`. Do not alter
  `RESOLVED` issues.
- Always export the configured suppression entries in JSON/HTML, including an
  empty array/table when none are configured. `ReportMain` loads the project
  file explicitly; UI auto-discovery remains outside this slice.

### Consequences and limits

- Suppressed findings remain auditable through status, reason evidence, and
  report configuration; current USE state and normalized inputs remain
  immutable.
- Absolute-path fingerprints are checkout-location sensitive. A verified
  project-root abstraction should be introduced before claiming portable
  suppression files or UI auto-discovery.

### Validation evidence

- `mvn -pl use-bdi-plugin test` passed with 58 tests, including suppression
  persistence, malformed/duplicate rejection, matching, orchestrator status,
  and JSON/HTML report serialization. No USE core source was modified.

## Unsupported fixture evidence - 2026-08-09

- `use-bdi-plugin/src/test/resources/fixtures/asl/unsupported/relational-context.asl`
  is accepted by Jason 3.3.0 and uses `Counter > 0` in a plan context.
- Bytecode/source inspection confirmed that Jason `RelExpr` implements
  `LogicalFormula` through `Structure`, which is also a `Literal` subtype.
  The normalizer therefore checks `RelExpr` before `Literal` and emits a
  `ContextUnsupported` node plus `UnsupportedFeature` code `ASL-002`.
- `UnsupportedFixtureTest` imports the real fixture through `BdiImportService`,
  asserts no parser diagnostic, checks the line-4 source span, and verifies
  `BdiProblemCollector` produces a WARNING in the `Unsupported feature` group.
  The test also asserts no `ASL-001` row is created, protecting the distinction
  between valid-but-unsupported syntax and invalid AgentSpeak.
- This is an implementation of the existing unsupported-syntax policy in
  ADR-0010/ADR-0012, not a new USE-core or parser architecture decision.

## Golden IR evidence - 2026-08-09

- `AgentModelJsonSerializerTest` compares the minimal model and the
  unsupported relational-context model with checked-in expected JSON. Each
  result is serialized twice to protect deterministic output.
- `unsupported-relational-context-agent-model.json` records relative source
  paths and the `ContextUnsupported`/`ASL-002` evidence, including its source
  span and subject. This extends the existing IR golden contract without
  changing the domain model or USE core.
- `mvn -pl use-bdi-plugin -Dtest=AgentModelJsonSerializerTest test` passed with
  2 tests. The golden corpus remains intentionally small until the Auction
  case-study fixtures exist.

## Performance benchmark evidence - 2026-08-09

- `BdiPerformanceBenchmarkTest` is a test-only measurement harness for the
  existing `BdiImportService` path. It includes Jason parsing, normalized IR
  materialization, and `BdiIndexBuilder` work; it does not access or mutate
  USE core state.
- The harness uses the tracked Smart Queue fixture, warms up twice, measures
  seven iterations, verifies the expected 9 beliefs, 1 goal, 5 plans and
  non-empty indexes on each iteration, then writes a JSON artifact under
  `use-bdi-plugin/target/performance/`.
- The 2026-08-09 run recorded minimum `3.5921 ms`, median `4.8261 ms`, and p95
  `10.1224 ms` with Jason `3.3.0` and BDI metamodel `0.1.0`. The values are a
  local baseline rather than a performance requirement.
- No new ADR was needed: this slice adds measurement around already accepted
  importer/IR/index boundaries and changes neither USE core nor runtime state.

## Clean-clone reproducibility evidence - 2026-08-09

- `use-bdi-plugin/scripts/clean-clone.ps1` uses `git clone --no-local
  --no-checkout` followed by detached checkout of the source `HEAD`. This
  deliberately tests the committed tree rather than the current worktree and
  preserves unrelated user changes in the original checkout.
- The reproducibility command is `mvn --batch-mode --no-transfer-progress -pl
  use-assembly -am package`. The script verifies the plugin shaded JAR, Jason
  runtime class, third-party notices, distribution ZIP entry, and clean Git
  status before and after the build.
- Temporary cleanup is allowed only for the generated `use-bdi-clean-clone-*`
  path below the system temp directory; `-KeepClone` supports investigation.
  No USE core or plugin runtime behavior is changed by this smoke slice.
- This implements the existing reproducibility checklist boundary and does
  not create a new architectural decision. The known root `mvn clean verify`
  Failsafe limitation remains separately documented.
