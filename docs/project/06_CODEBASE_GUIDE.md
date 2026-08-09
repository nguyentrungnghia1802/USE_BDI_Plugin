# Codebase Guide

Status: canonical repository and extension guide
Last verified: 2026-08-09
Code baseline: `c1b11b41`

## 1. Repository layout

```text
use/
  pom.xml                         Maven reactor and Java baseline
  use-core/                       USE parser, UML/OCL, runtime system
  use-gui/                        USE Swing shell and plugin runtime
  use-bdi-plugin/                 Thesis extension module
  use-assembly/                   Distribution ZIP assembly
  docs/
    project/                      Canonical specs, guides, ADRs, evidence
  manual/                         Upstream USE manual
```

The thesis feature belongs in `use-bdi-plugin` by default. A change to
`use-core` or `use-gui` requires verified source evidence, an ADR, and focused
host regression tests. ADR-0019 is the current example of an accepted,
test-mode-only `use-gui` change.

## 2. Plugin module layout

```text
use-bdi-plugin/
  pom.xml
  .bdi-plugin/
    rules.json
    suppressions.json
  scripts/
  src/main/java/org/tzi/use/plugins/bdi/
    application/
    importer/
    index/
    model/ir/
    model/mapping/
    persistence/
    problems/
    report/
    rules/
    ui/
    use/
    validation/
  src/main/resources/
    useplugin.xml
    META-INF/THIRD-PARTY-NOTICES.txt
  src/test/java/
  src/test/resources/fixtures/
```

## 3. Package responsibilities

| Package | Owns | Must not own |
| --- | --- | --- |
| root plugin package | Plugin/actions and USE integration entry points | Domain rules |
| `application` | Import use cases and snapshots | Swing widgets or Jason AST policy |
| `importer` | Jason parse adapter, normalization, diagnostics | USE model access |
| `model.ir` | Immutable normalized AgentSpeak model | Jason/USE/Swing types |
| `index` | Derived immutable BDI lookups | Parsing or UI |
| `use` | USE projection, fingerprints, OCL/effect adapters | Swing workflow |
| `model.mapping` | Binding/suggestion/staleness domain | File chooser or USE mutation |
| `persistence` | Versioned JSON codecs/repositories | Business orchestration |
| `validation` | Rule SPI, context, issues, config, suppression | Concrete GUI rendering |
| `rules`/catalog | Deterministic consistency policies | Jason AST or direct Swing state |
| `problems` | Issue-to-table presentation model | Rule semantics |
| `ui` | Swing composition and user events | Parser/model semantics |
| `report` | Serialization of supplied report data | Live session discovery |

## 4. Dependency rules

- `use-core` and `use-gui` are `provided` plugin dependencies.
- Jason is a compile/runtime dependency shaded into the plugin.
- JUnit is test-only.
- Domain packages depend on Java and plugin-owned values only.
- Adapters point inward toward domain contracts; domain code never points out
  to Jason or USE implementations.
- UI may compose services but must not duplicate rule logic.

## 5. Naming and identity conventions

- Java packages use `org.tzi.use.plugins.bdi`.
- Rule IDs use uppercase families such as `ASL-001` and `OCL-004`.
- JSON schema versions use semantic strings such as `0.1.0`.
- Source positions are one-based; unknown positions are `0/0` at diagnostic
  boundaries and explicit unknown spans in IR.
- Mapping keys use kind plus normalized source identity.
- UML references are qualified strings from the USE facade.
- Generated output belongs under module `target/` unless it is reviewed thesis
  evidence intentionally tracked under `docs/project/evidence/`.

## 6. Adding AgentSpeak support

1. Add the smallest valid or unsupported `.asl` fixture.
2. Verify Jason 3.3.0 AST behavior inside importer tests.
3. Extend the normalizer to an existing/new plugin-owned IR type.
4. Preserve source span and add `UnsupportedFeature` when semantics remain
   outside the supported subset.
5. Update golden serialization if the canonical IR changes.
6. Add index/rule/UI tests only where downstream behavior changes.
7. Update requirements, subset/limitations, traceability, and decision log when
   the boundary changes.

Never parse AgentSpeak text with ad-hoc regex as a replacement for Jason.

## 7. Adding a mapping kind or schema field

1. Define the domain invariant and replacement identity.
2. Update `MappingKind`/records and suggestion behavior.
3. Decide schema compatibility and unknown-field policy explicitly.
4. Update codec/repository round-trip and rejection tests.
5. Update fingerprints if the field affects semantic identity.
6. Update UI controls, stale detection, rules, data/API docs, and migration
   guidance.

Do not silently reinterpret existing `0.1.0` files.

## 8. Adding a consistency rule

1. Allocate a stable family ID and phase.
2. Implement a pure evaluator over `ValidationContext`.
3. Return source/UML/evidence/suggested-fix information when available.
4. Use `UNKNOWN` certainty for information gaps.
5. Register the rule in `StandardConsistencyRules`.
6. Add focused positive/negative/unknown tests.
7. Add the ID to `rules.json` if enabled by default.
8. Update the rule catalog and traceability test.

## 9. Adding OCL/effect behavior

- Reuse USE compiler/evaluator APIs through the `use` adapter.
- Copy variable bindings and validate receiver/argument binding.
- Keep bounded mutation behind `SnapshotOclEvaluator`.
- Require explicit `soil:` expressions.
- Use variation restoration in `finally`.
- Test state fingerprint before and after every failure/success path.
- Add or supersede an ADR for any broader mutation or host-core behavior.

## 10. Adding a UI action or view

1. Register the action in `useplugin.xml`.
2. Implement `IPluginActionDelegate`.
3. Check `Session.hasSystem()` before `Session.system()`.
4. Compose existing services/adapters.
5. Add a `ViewFrame` through the USE main window.
6. Keep expensive work off the Swing event thread.
7. Cancel/invalidate workers in `detachModel()`.
8. Add unit UI tests and package/menu smoke evidence.

## 11. Files requiring extra care

| File/area | Reason |
| --- | --- |
| root `pom.xml` | Changes Java baseline and all modules |
| `useplugin.xml` | Runtime discovery and visible menu contract |
| plugin `pom.xml` | Shading, host classpath, licenses |
| `MappingSourceId`/`SourceSpan` | Persisted identity and suppression portability |
| JSON codecs | Forward/backward compatibility and silent-field risk |
| `StandardConsistencyRules` | Catalog, config, metrics, and thesis claims |
| `UseSnapshotOclEvaluator` | User-state safety boundary |
| report exporters | Evidence integrity and output escaping |
| case-study fixtures/oracle | Evaluation validity |
| `use-gui` main lifecycle | Host behavior outside the plugin |

## 12. Documentation update markers

| Change | Required documents |
| --- | --- |
| Product behavior/status | `00`, `01`, `03`, `12`, checklist |
| Module/dependency/lifecycle | `02`, `05`, `06`, `09`, decision log |
| JSON field/schema | `01`, `04`, `05`, `07`, `08`, `09` |
| Rule ID/semantics | `01`, rule catalog, `03`, `12`, evidence |
| OCL/state behavior | `01`, `02`, `03`, `09`, `11`, ADR |
| Menu/workflow | `01`, `03`, user/install guides, screenshots |
| Build/release script | `07`, `08`, developer/install guides, release evidence |
| Case-study/oracle/metrics | `01`, `03`, `07`, `09`, evidence, checklist |

## 13. Codebase synchronization checklist

- [x] Reactor and package inventory match the repository.
- [x] Layer rules match current imports and accepted ADRs.
- [x] Extension workflows include tests and documentation triggers.
- [x] High-risk identity/state/schema files are called out.
- [x] Generated artifacts and fixture placement are explicit.
