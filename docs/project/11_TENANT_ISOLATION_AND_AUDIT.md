# State Isolation and Audit Guarantees

Status: canonical safety and evidence matrix; multi-tenancy is not applicable
Last verified: 2026-08-09
Code baseline: `c1b11b41`

## 1. Scope

The reference project uses tenant isolation. This project is a local desktop
plugin with no users, roles, server, database, or tenant resources. Its
equivalent critical boundary is isolation between:

- untrusted/unsupported AgentSpeak input and normalized domain state;
- plugin analysis and the current mutable USE session;
- one import generation and a later import generation;
- user-confirmed mappings and generated suggestions;
- open findings and explicitly reasoned suppressions;
- current source/evidence and generated report output.

## 2. State isolation guarantees

| Boundary | Guarantee | Evidence/control |
| --- | --- | --- |
| Jason AST -> domain | Concrete Jason types do not escape adapter/normalizer | package tests and architecture rule |
| Invalid source -> other files | One file failure does not abort successful files | partial-success tests |
| Unsupported syntax -> analysis | Unsupported data remains explicit | `ASL-002`, unsupported IR golden |
| Old worker -> new UI state | Stale callbacks are ignored by generation token | worker/explorer tests |
| USE model -> plugin | Projection is immutable plugin-owned data | facade records/tests |
| OCL bindings -> session variables | Evaluation uses copied bindings | snapshot evaluator tests |
| Bounded effect -> current snapshot | Disposable variation is restored in `finally` | effect/fingerprint tests |
| Suggestion -> mapping | User confirmation is required | mapping editor/domain contract |
| Failed mapping load -> current mapping | Exception occurs before UI document replacement | repository/editor flow |
| Suppression -> issue | Exact rule/source fingerprint and reason required | suppression service/tests |
| Export -> analysis | Exporter serializes supplied immutable data only | report tests |

## 3. Allowed state changes

| Operation | Allowed persistent effect |
| --- | --- |
| Import `.asl` | Replace in-memory import snapshot and UI projection |
| Re-import | Replace with newest generation result |
| Apply/edit/remove mapping | Replace immutable in-memory mapping document |
| Save mapping | Write user-selected `.bdimap.json` |
| Load mapping | Replace active document only after successful validation |
| Save rule/suppression config through repository | Write explicitly selected path |
| Evaluate rules/OCL | No persistent USE state change |
| Simulate bounded `soil:` effect | No persistent USE state change after variation closes |
| Export report | Write caller-selected JSON/HTML output |
| Run tests/scripts | Write generated artifacts under `target/`/selected backup path |

## 4. Trust boundaries

- `.asl`, `.use`, mapping/config/suppression JSON, issue messages, and evidence
  text are input data and may be malformed.
- Jason and USE compiler/evaluator results are authoritative only within their
  respective syntax/runtime contracts.
- Mapping suggestions and confidence-like scores are advisory.
- HTML report content must be escaped.
- File paths are local and may reveal checkout structure in mappings/reports;
  review before sharing artifacts externally.
- No plugin workflow should start a web listener. The Jason optional mind
  inspector is disabled for parser import.

## 5. Audit/evidence event matrix

There is no persistent event log. Reproducibility is provided by explicit
artifacts and metadata:

| Event | Required evidence |
| --- | --- |
| Import success/failure | source path, summary, parser version, diagnostics |
| Unsupported construct | code/kind, subject, source span |
| Mapping confirmation | kind/source/target, optional expression/evidence in document |
| Mapping staleness | source/target/fingerprint reason |
| Rule finding | rule ID, severity/status/certainty, source/UML/evidence/fix |
| Suppression | rule ID, source fingerprint, non-empty reason |
| OCL/effect outcome | PASS/FAIL/UNKNOWN or bounded status plus evidence |
| Report | versions, timestamp, counts, hashes when supplied, issues/suppressions |
| Case-study run | fixtures, oracle, commands, markers, generated outputs |
| Release package | commit, package contents, clean-clone marker, notices |

## 6. Privacy and sharing

The repository fixtures are research samples. The plugin has no telemetry or
remote upload. User-selected local files may contain private model names,
paths, beliefs, or evidence. Before sharing a report:

- review absolute paths and source excerpts;
- remove data only through an explicit redaction/export design, not by
  silently dropping evidence;
- retain version/hash/oracle context needed for research reproducibility;
- do not claim the current exporter provides anonymization.

## 7. Safety failure response

Any persistent unexpected change to the current USE state is release-blocking:

1. Stop the affected analysis path.
2. Preserve input model, commands, mapping, and before/after fingerprints.
3. Add a focused failing regression test.
4. Review adapter and variation cleanup.
5. Add/supersede an ADR if the state policy changes.
6. Re-run root verify and manual GUI acceptance before release.

## 8. Isolation synchronization checklist

- [x] Multi-tenancy and authorization are explicitly not applicable.
- [x] Actual local trust/state boundaries are enumerated.
- [x] Persistent writes and read-only operations are distinguished.
- [x] Audit claims match available artifacts rather than a nonexistent log.
- [x] Privacy/path disclosure and state-corruption response are documented.
