# Data and Persistence

Status: canonical persistence contract; no database is used
Verification: source-backed; see Git history and DocumentationContractTest

## 1. Persistence model

The USE BDI plugin is a local desktop extension and has no relational or
embedded database. Canonical runtime state is held in immutable Java values.
Optional durable state uses small UTF-8 JSON files selected by the user or
provided by the project. Generated reports and benchmark artifacts are output
files, not an operational source of truth.

## 2. Data ownership

| Data | Owner | Persistence |
| --- | --- | --- |
| UML/OCL model and snapshot | USE host/session | Existing USE specification/command workflow |
| AgentSpeak source | User/project | Original `.asl` files |
| Parsed AST | Jason adapter | Memory only |
| Normalized IR and indexes | Import snapshot | Memory only; golden JSON is test evidence |
| Confirmed mappings | Plugin/user | Optional `.bdimap.json` |
| Enabled rules | Plugin/project | Optional `.bdi-plugin/rules.json` |
| Suppressions | Plugin/project | Optional `.bdi-plugin/suppressions.json` |
| Problems | Validation run | Memory only; recomputed |
| Reports | Export caller | Generated JSON/HTML |
| Benchmark/evaluation results | Test/scripts | Generated under `target/` or tracked evidence |

## 3. Mapping document schema

Current schema version: `0.1.0`.

```json
{
  "schemaVersion": "0.1.0",
  "bdiMetamodelVersion": "0.1.0",
  "useFingerprint": "<model fingerprint>",
  "bindings": [
    {
      "kind": "ACTION_OPERATION",
      "source": "<normalized BDI source ID>",
      "target": "<qualified UML reference>",
      "expression": null,
      "evidence": []
    }
  ]
}
```

Constraints:

- required text fields are non-blank;
- schema version must equal `0.1.0`;
- `kind` must be a current `MappingKind` enum value;
- `kind + source` is unique;
- `expression` is nullable; bounded effects require the `soil:` prefix;
- evidence is an ordered string array;
- save output is deterministic for the in-memory binding order.

Current limitation: the mapping decoder validates required values, duplicate
keys, enum values, and JSON syntax, but does not reject every unknown object
field. Forward schema evolution must add explicit unknown-field policy before
claiming strict closed-schema validation.

## 4. Rule configuration schema

Current schema version: `0.1.0`.

```json
{
  "schemaVersion": "0.1.0",
  "enabledRules": ["ASL-001", "MAP-003", "OCL-001"]
}
```

The codec rejects unknown root fields, malformed values, duplicate IDs, invalid
ID format, and unsupported schema versions. The orchestrator additionally
rejects syntactically valid IDs that are absent from the supplied rule set.
Serialization sorts IDs.

The tracked example enables all 22 standard rules. For an active file-backed
USE model, the GUI loads `<model-directory>/.bdi-plugin/rules.json`; absence
uses the same all-rules default and malformed/unknown-rule input is rejected.

## 5. Suppression schema

Current schema version: `0.1.0`.

```json
{
  "schemaVersion": "0.1.0",
  "suppressions": [
    {
      "ruleId": "REF-001",
      "sourceFingerprint": "<64 hexadecimal SHA-256 characters>",
      "reason": "Reviewed fixture-specific exception"
    }
  ]
}
```

The codec rejects unknown fields, duplicate `(ruleId, sourceFingerprint)`
keys, invalid hashes, missing reasons, malformed JSON, and unsupported schema
versions. Serialization sorts by suppression key.

Source fingerprints currently include normalized absolute paths. Moving the
source file or checkout can intentionally invalidate a suppression.

For an active file-backed USE model, the GUI loads
`<model-directory>/.bdi-plugin/suppressions.json`; absence uses an empty list.

## 6. Report schema

The JSON exporter writes:

- `projectName`, `pluginVersion`, `useVersion`, `timestamp`;
- `issuesCount`, `mappingsCount`;
- nullable `modelHash`, `mappingHash`;
- `notes`;
- `issues[]` with rule ID, severity, status, certainty, message, source, and
  flattened evidence;
- `suppressions[]` with rule ID, source fingerprint, and reason.

The HTML exporter renders equivalent metadata and issue/suppression evidence
with escaping. `ReportData` validates non-negative counts and optional SHA-256
format. Exporters create parent directories and overwrite the requested output
file; callers own path confirmation and backup policy.

## 7. Generated and tracked artifacts

- `target/` content is generated and ignored by Git.
- `docs/project/evidence/` contains reviewed thesis evidence that is tracked.
- `.bdi-plugin/rules.json` and `.bdi-plugin/suppressions.json` are tracked
  examples/configuration inputs.
- A fixed `.bdimap.json` is not tracked for Auction because current source IDs
  would encode one checkout path.
- Backup scripts archive committed content and list missing external artifacts
  rather than fabricating them.

## 8. Versioning and migration policy

There is no database migration tool. For each JSON schema change:

1. Add a new explicit schema version.
2. Decide whether old versions are rejected or migrated.
3. Keep readers fail-fast for unsupported versions.
4. Add round-trip, malformed, duplicate, unknown-field, and compatibility
   fixtures appropriate to that schema.
5. Update requirements, API contracts, operations, risk register, and guides.
6. Never silently reinterpret an old field.

## 9. Retention and backup

User-selected mapping/config/suppression files and generated reports are local
files. The plugin does not implement retention, encryption, cloud sync, or
automatic backup. The thesis backup script uses committed source and a JSON
manifest; complete data/report/slides backup remains open because those
external directories are absent from this checkout.

## 10. Persistence synchronization checklist

- [x] No database or remote store is implied.
- [x] All three current JSON schema versions and fields match codecs.
- [x] Strict/lenient unknown-field behavior is distinguished.
- [x] Generated versus tracked artifacts are explicit.
- [x] Portability and backup limitations are explicit.
