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

- Status: Accepted
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
