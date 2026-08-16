# Metamodel-to-Java IR Alignment Validation

Date: 2026-08-17

Task: 06 — Metamodel ↔ Existing Java IR Conformance

Result: **PASS**

## Scope and decision

The versioned Ecore is a specification artifact. The existing immutable Java
records remain the executable representation; conformance is demonstrated by
an explicit concept matrix, adapter ownership, fixture behavior, and an
executable contract. No generated-EMF runtime or duplicate semantic validator
was introduced.

## Inventory and reverse audit

- Ecore classifier inventory: **48 EClasses**.
- Alignment coverage: **48/48 EClass names represented**.
- Executable Java realization check: **47 concrete/nested Java types load**;
  abstract `SourceLocatedElement` is deliberately flattened across the two
  source-span records and MAS source evidence.
- Unresolved `GAP` rows: **0**.
- Reverse audit: Java parser evidence, diagnostics, UI identities, indexes,
  traces, reports, mappings, and rule results are classified as technical or
  evidence concerns rather than silently promoted into domain metaclasses.

The normative result is recorded in
[`METAMODEL_TO_JAVA_ALIGNMENT.md`](../metamodel/METAMODEL_TO_JAVA_ALIGNMENT.md).
Multiplicity deviations, partial/invalid representation, unknown coordinates,
portable source identity, ordering, duplicate handling, and null policy are
explicit in that matrix.

## Boundary and fixture evidence

`MetamodelIrAlignmentContractTest` proves:

- every EClass has an alignment row and no row has `GAP` status;
- expected Java realization types exist;
- Jason, JaCaMo, CArtAgO, and Moise concrete parser/runtime types do not leak
  beyond the importer boundary;
- a minimal ASL fixture produces agent, belief, goal, plan, context, trigger,
  internal action, and source evidence;
- Smart Queue produces ordered context/action/goal/test/belief-update evidence
  and its actual `AGENT_CLASS` / `ACTION_OPERATION` mappings;
- Auction `.jcm` produces three agent instances, two normalized sources,
  organization evidence, resources, and diagnostics.

The first focused run intentionally exposed an over-strong test assumption:
the Smart Queue sidecar does not contain a `BELIEF_ATTRIBUTE` mapping. The
contract and matrix were corrected to assert the fixture's actual mapping
kinds; no absent concept was fabricated. Smart Home remains declaration-only
because its checked-in project evidence does not provide an inspectable
artifact class.

The updated [`ir-class-diagram.mmd`](ir-class-diagram.mmd) records Java
ownership, all IR hierarchies, the four adapter boundaries, portable identity,
and the absence of concrete parser types downstream.

## Commands and results

Focused conformance/boundary suite:

```text
mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am \
  -Dtest=MetamodelIrAlignmentContractTest,MetamodelProfileArtifactTest,AgentModelJsonSerializerTest,IrHierarchyTest,JasonAslParserAdapterTest,MasProjectAnalysisServiceTest,MoiseOrganizationParserAdapterTest,CArtAgOArtifactAdapterTest,DiagramPackageBoundaryTest,ProjectSourceIdTest test

Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Full required reactor:

```text
mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test

Tests run: 217, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Additional static gates:

```text
Ecore EClass count: 48
Missing alignment names: 0
Unresolved GAP rows: 0
git diff --check: PASS
```

Root `clean verify` was **not required**: Task 06 changes documentation and a
plugin-owned contract test only; no production class or dependency was
refactored.

## Final gate

```text
Alignment matrix: PASS (48/48 EClasses)
Unresolved GAP: 0
IR diagram: PASS
Conformance contract: PASS (2/2 contract tests)
Canonical fixtures: PASS (minimal ASL, Smart Queue, Auction)
Boundary tests: PASS (28/28 focused suite)
Reactor: PASS (217/217 plugin tests)
Root verify if required: NOT REQUIRED
git diff --check: PASS
Result: PASS
```
