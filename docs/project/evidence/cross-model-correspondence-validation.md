# Cross-Model Correspondence Validation

Date: 2026-08-17

Task: 07 — BDI/MAS ↔ UML/OCL Correspondence Model

Result: **PASS**

## Delivered contract

- [`CROSS_MODEL_CORRESPONDENCE.md`](../metamodel/CROSS_MODEL_CORRESPONDENCE.md)
  separates the JaCaMo domain profile, cross-model relations, and rule
  semantics; defines multiplicity, identity, provenance, evidence, review and
  currentness states; and bounds “synchronization” to revalidation.
- [`correspondence-diagram.mmd`](../metamodel/correspondence-diagram.mmd)
  provides the metamodel-level source/correspondence/USE view without mixing in
  runtime or Java IR ownership.
- [`CORRESPONDENCE_RULE_MATRIX.md`](../metamodel/CORRESPONDENCE_RULE_MATRIX.md)
  traces all six core kinds plus environment and organization families to
  persistence, suggestion, confirmation, staleness, and consuming rules.
- `CrossModelCorrespondenceContractTest` locks the closed kinds, exact schema
  versions, implementation state enums, document coverage, persistence
  separation, and diagram family edges.

## Source-backed decisions

- Core BDI candidates are `MappingSuggestion` values; persisted
  `MappingBinding` records are confirmed by contract. Core currentness has no
  invented enum: supported stale conditions are `MappingStalenessReason`
  findings and no findings means current for those checks.
- The core mapping decoder's residual unknown-field permissiveness is recorded
  explicitly. It was not falsely described as strict and was not changed
  without an ADR.
- Environment records keep their strict separate schema with explicit
  `CANDIDATE|CONFIRMED` and `CURRENT|STALE|UNKNOWN` states. Only
  confirmed-current records reach semantic validation.
- Organization records remain reviewer supplied and non-persisted. Candidate,
  unreviewed bounds, and absent runtime enactment remain UNKNOWN evidence.
- No suggestion was auto-confirmed, no schemas were merged, and no
  bidirectional/runtime synchronization claim was introduced.

## Gate results

The first focused command did not reach plugin tests because PowerShell parsed
an unquoted comma-separated selector and then the corrected selector caused
Surefire to reject the absence of those plugin-only names in `use-core`. The
final focused command quoted the selector and used
`-Dsurefire.failIfNoSpecifiedTests=false`, which is the required multi-module
selector behavior. This was an invocation correction, not a product failure.

```text
Focused correspondence/mapping/persistence/rule/trace suite:
Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

Full plugin reactor:
Tests run: 219, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

Root clean verify: NOT REQUIRED
Reason: documentation plus plugin-owned contract test only; no production
mapping class, JSON schema, or dependency changed.
```

## Final gate

```text
Correspondence spec: PASS
Diagram: PASS
Rule matrix: PASS
Confirmation semantics: PASS
Staleness semantics: PASS
Persistence compatibility: PASS
Focused tests: PASS (42/42)
Reactor: PASS (219/219 plugin tests)
Root verify if required: NOT REQUIRED
git diff --check: PASS
Open failures: 0
Result: PASS
```
