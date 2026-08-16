# Task 05 Static-Semantics Validation

Status: **PASS**

## Deliverables and source audit

- [Static semantics](../metamodel/STATIC_SEMANTICS.md) audits every standard,
  environment, and organization rule with category, executable phase/catalog,
  profile/correspondence/USE inputs, prerequisite, algorithm, outcome,
  severity/certainty, trace, fix, limitation, tests, and evidence.
- [Rule-to-metamodel matrix](../metamodel/RULE_TO_METAMODEL_MATRIX.md) traces all
  29 IDs to profile, correspondence, USE, runtime-evidence, and formalization
  categories.
- The authoritative [rule catalog](../08_CONSISTENCY_RULE_CATALOG.md) now links
  both artifacts and preserves the executable registry/catalog boundaries.

The audit read `StandardConsistencyRules`, `BdiIndexBuilder`,
`ValidationOrchestrator`, environment staleness/consistency services,
organization consistency validation, immutable result types, and focused tests.
It corrected documentation drift without changing production semantics:

- `BDI-002` is `ERROR/CONFIRMED`, and current goal support is an aggregate
  exact functor/arity lookup over literal `ADD + ACHIEVE` triggers;
- `BDI-004` detects decreasing known source lines in stored step order, not a
  duplicate step index;
- `BEL-001` checks missing belief bindings, not target compatibility;
- environment mappings are persisted and GUI-editable; only confirmed/current
  mappings enter static environment consistency evaluation.

The specification distinguishes bounded `NO_FINDING`, explicit confirmed /
potential / unknown findings, and `NOT_EVALUATED`. An absent optional service
or unsupported prerequisite is never reclassified as PASS.

## Registry and document parity

A source/document parity script extracted registrations and extension constants
and required a corresponding row in both Task 05 artifacts:

```text
RULE_PARITY_OK standard=22 environment=4 organization=3
STATIC_SEMANTICS_LINKS_OK documents=4 broken=0
SEMANTICS_FORMAT_OK
```

`RuleCatalogCompletenessTest` now also enforces all 29 documented rows, all
eight allowed formalization types, exact goal-support boundary text,
`NOT_EVALUATED`, the OCL/EVL non-rewrite decision, and persisted environment
wording. No ID, `RulePhase`, configuration schema, catalog membership,
production evaluator, severity, or certainty was changed.

## Executable semantics and state safety

`ValidationOrchestratorTest` adds a focused snapshot-semantics fixture with a
plugin-owned immutable context and fake evaluator. It proves that precondition
`FAIL`, precondition `UNKNOWN`, false context, and invariant violation remain
distinct `OCL-001`, `OCL-002`, `CTX-001`, and `OCL-003` findings, while PASS
inputs produce no finding from `OCL-001..004`/`CTX-001`.

`UseSnapshotOclEvaluatorTest` runs the real USE adapter: PASS, FAIL, and UNKNOWN
preconditions remain distinct; a bounded SOIL invariant violation is detected;
and the USE snapshot fingerprint is equal before and after the disposable
variation. `LiveUseSnapshotProviderTest` also passes.

## Verification results

| Gate | Result |
|---|---|
| Focused rule/environment/organization/Auction/OCL/state suite | PASS; 23/23 |
| `mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test` | PASS; plugin 215/215; all four reactor modules successful |
| Registry/document parity and local links | PASS |
| Production code/POM changes | none |
| `git diff --check` | PASS after final staging review |

## FINAL GATE

```text
Static semantics spec: PASS
Rule/metamodel matrix: PASS
22-rule registry parity: PASS
ENV catalog parity: PASS (4 separate IDs)
ORG catalog parity: PASS (3 separate IDs)
PASS/FAIL/UNKNOWN: PASS
Focused tests: PASS (23/23)
Reactor: PASS (215/215 plugin tests)
State safety: PASS (fingerprint restored)
git diff --check: PASS
Open failures: 0
Result: PASS
```
