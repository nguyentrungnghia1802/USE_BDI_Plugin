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
