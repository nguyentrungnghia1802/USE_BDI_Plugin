# Analysis Metamodel Versioning and Compatibility

Status: **NORMATIVE VERSION POLICY**

Current descriptor:

| Field | Value |
|---|---|
| ID | `https://useocl.github.io/bdi/metamodel/analysis/1.0` |
| Version | `1.0.0` |
| Profile name | `JaCaMo Consistency Analysis Profile` |
| Java owner | `AnalysisMetamodelDescriptor.current()` |
| Ecore owner | `use-jacamo-analysis.ecore` root namespace + metadata annotation |

## 1. Identity distinction

The analysis-profile descriptor is not the existing
`BdiMetamodelVersion.CURRENT=0.1.0`. The latter versions the normalized Java
BDI IR/index contract used by `.bdimap.json`; the former versions the broader
specification-only profile spanning evidence, BDI, static MAS, declaration-only
environment, and static organization concepts.

The two values are both retained in snapshots and reports because collapsing
them would reinterpret existing mapping compatibility semantics.

## 2. Semantic version policy

The profile uses `MAJOR.MINOR.PATCH`:

- **MAJOR**: an incompatible semantic change. The major family also changes in
  the namespace URI (`.../analysis/2.0`).
- **MINOR**: a backward-compatible semantic addition, such as an optional
  metaclass/reference outside existing accepted instances or derived metadata
  with no changed rule result.
- **PATCH**: corrections to documentation, annotations, diagrams, or
  conformance metadata that do not change accepted instances or meaning.

Breaking changes include removing/renaming an evidence metaclass, changing
relation meaning or ordering, tightening multiplicity against accepted models,
changing portable source identity/unknown-coordinate meaning, changing
confirmed correspondence semantics, or reinterpreting static evidence as
runtime evidence. They require a new major namespace, ADR, updated matrices,
fixture migration, compatibility review, and a full release gate.

Non-breaking changes include documentation/literature trace, optional concepts
that preserve current evidence, and derived presentation metadata with no
semantic effect. Minor additions still update all matrices and profile tests;
patch-only edits still run artifact, link, and conformance checks.

## 3. Independent version axes

| Axis | Current | Changes when | Relationship to profile |
|---|---|---|---|
| analysis profile | `1.0.0` | bounded domain/profile semantics change | authoritative descriptor in snapshot/report |
| BDI Java IR/index | `0.1.0` | normalized BDI record/index compatibility changes | realizes the BDI subset; kept separate |
| core mapping schema | `0.2.0` | JSON shape/migration semantics change | stores BDI IR version; a profile change alone does not bump it |
| environment mapping schema | `0.1.0` | strict environment JSON shape changes | independent correspondence persistence |
| report shape | additive legacy JSON/HTML contract; no standalone version field | exported fields/meaning change | Task 09 adds descriptor fields under ADR-0045; consumers feature-detect historical reports |
| plugin | Maven/plugin release version | packaged implementation release | may contain the same profile version across multiple patches |
| USE | `7.1.1` baseline | host compatibility changes | host evidence, not profile version |
| parser | captured set (Jason currently `3.3.0`) | adapter/parser versions change | provenance, not profile identity |
| evaluation manifest | `0.1.0` | corpus/configuration schema changes | manifest/tool/corpus hashes remain independent |

A metamodel bump does not automatically bump another axis. Each owner changes
only when its own compatibility contract changes, with cross-axis review.

## 4. Snapshot and report contract

`CurrentAnalysisSnapshotService` invokes validation once and records plugin and
USE versions, BDI IR/index version, the complete analysis descriptor, sorted
unique parser versions, hashes, counts, configuration, suppressions, and
findings. The snapshot rejects a non-current descriptor, an IR version
inconsistent with the index, or parser versions inconsistent with imported
models. This is composition-time metadata validation, not EMF instance
validation and not a second semantic validator.

`CurrentAnalysisReportService` copies the frozen metadata into `ReportData`.
JSON emits `analysisMetamodelId`, `analysisMetamodelVersion`,
`analysisProfileName`, `bdiIrMetamodelVersion`, and `parserVersions`; HTML emits
equivalent rows. Exporters do not read USE, parse input, validate, or mutate.

Historical reports without these additive fields remain historical artifacts;
their profile version is **UNKNOWN**, not guessed from plugin or BDI IR
version. Current evidence must match descriptor identity/major compatibility,
relevant IR/parser/USE provenance, and corpus/hash identity.

## 5. Migration policy

1. Classify a proposed Ecore change before editing.
2. For MINOR/MAJOR changes, update descriptor and Ecore metadata together; a
   major change also updates the namespace family.
3. Update coverage, Java alignment, rule, correspondence, and graphical syntax
   contracts.
4. Decide independently whether mapping/environment/report persistence needs a
   schema migration and record an ADR when it does.
5. Migrate canonical fixtures/evidence without fabricating unsupported data.
6. Rerun metamodel, snapshot, report, ASL, JCM, mapping, organization,
   environment, diagram, headless, evaluation, and root gates.
7. Never transform each runtime IR to/from EMF merely to compare versions.

No automatic migration is needed from “no descriptor” to 1.0.0 because
snapshots are not persisted and reports are immutable exports. Old reports are
explicitly version-unknown; new analysis creates new evidence.

## 6. Evidence compatibility

| Evidence | Compatible when |
|---|---|
| current snapshot/report | exact descriptor ID and compatible major; current implementation requires exact `1.0.0` |
| `.bdimap.json` | BDI IR version, source identities, and schema remain supported |
| `.cartago-map.json` | its schema/provenance/state contract remains supported |
| organization reviewer records | source/target semantics and evidence contract remain unchanged |
| trace/diagram/SVG | producer snapshot/profile matches and closed vocabulary passes |
| evaluation result | manifest/corpus/configuration hashes match and the underlying snapshot uses the expected descriptor |

UNKNOWN compatibility never becomes PASS. Incompatible evidence is regenerated
or explicitly migrated under an ADR.
