# Full-Project Audit Evidence — 2026-08-23

Status: technical gates passed; thesis release remains `BLOCKED_EXTERNAL`

This evidence records the repository-wide source, build, packaging,
documentation, and release-boundary audit requested after completion of the
implementation roadmap. The audit followed `docs/agent/AGENT.md`; in
particular, it did not expand the supported static-analysis scope into JaCaMo
runtime execution and did not modify USE core or parser behavior.

## Confirmed Findings And Corrections

| Finding | Correction | Commit |
| --- | --- | --- |
| File dialogs embedded one contributor's absolute checkout path. | Discover the nearest active USE checkout from `user.dir`, with a current-directory fallback, and cover both paths by tests. | `37b17a84` |
| Demo instructions embedded machine-specific launch paths. | Use repository-relative and placeholder paths throughout operational guidance. | `3093eb91` |
| The report smoke wrote generated artifacts into tracked documentation. | Keep smoke output under `use-bdi-plugin/target/report-smoke`. | `b109f1f6` |
| The assembly final name was configured on a goal where Maven treats it as read-only. | Configure the final name on the assembly project build. | `9eb36ebe` |
| Plugin code used Guava and JUnit APIs without declaring all direct dependencies. | Declare the direct dependencies and make dependency analysis deterministic for the shaded plugin. | `0d7f4e7d` |
| Distribution TAR creation used a non-portable long-file strategy. | Emit POSIX-compatible long TAR entries. | `8e0edbf5` |
| The shaded plugin JAR contained duplicate generator resources. | Exclude the duplicate Jason/JaCaMo generator resources while retaining runtime classes. | `5ebdc200` |
| Traceability grouped planned runtime support with implemented requirements. | Separate planned and optional requirements and assert the distinction in the documentation contract. | `30b9445d` |
| Portable checkout discovery changed an architectural boundary without a decision record. | Add ADR-0046 and protect it with the documentation contract. | `309e8144` |

Each confirmed finding was corrected and committed independently before the
next correction, preserving an auditable sequence.

## Static Audit

- 87 Markdown files: no broken repository-local links or fragments.
- 23 JSON files and 23 XML/Ecore files: all parsed successfully.
- PowerShell scripts: no parser errors.
- Requirement traceability: all 91 requirement IDs accounted for, with no
  missing or extra IDs.
- No operational source or documentation retains a contributor-specific
  absolute checkout path.
- No secret-like credential assignments were found in tracked source.
- Planned, optional, partial, and external work remains labelled explicitly;
  it is not reported as implemented.

## Executable Gates

The following gates passed on the audit branch after the corrections:

- `mvn --batch-mode --no-transfer-progress clean verify`
  - all five reactor modules succeeded;
  - USE core integration tests: 1 passed;
  - USE GUI integration tests: 121 passed;
  - BDI plugin tests: 225 discovered, 224 executed successfully, and the one
    release-screenshot test skipped by its documented opt-in condition.
- `use-bdi-plugin/scripts/smoke.ps1`: packaged parser, IR, diagnostic, report,
  archive, and GUI-menu smoke passed.
- `use-bdi-plugin/scripts/headless-quality-gate.ps1`: expected quality-finding
  and invalid-input exit contracts passed.
- `use-bdi-plugin/scripts/auction-evidence.ps1`: baseline, structural mutant,
  and committed evidence checks passed.
- `use-bdi-plugin/scripts/auction-evaluation.ps1`: five reviewed cases yielded
  one pass, four detected mutants, and zero misses or unexpected findings.
- `use-bdi-plugin/scripts/performance.ps1`: import/index benchmark passed.
- `use-bdi-plugin/scripts/diagram-performance.ps1`: all four canonical cases
  preserved the same structural fingerprint across repeated runs.
- The `family-person`, `smart-home`, `smart-queue`, and `auction` canonical
  headless demo scenarios passed.
- The opt-in release screenshot capture passed for all 11 required images; the
  regenerated files were byte-identical to the tracked evidence.

Generated reports, benchmarks, extracted distributions, and manifests remain
under ignored `target` directories unless they are intentionally reviewed and
committed evidence.

## Release Boundary

The coding, static-analysis logic, build, packaging, documentation contracts,
and reproducible repository-owned evidence pass their technical gates. The
thesis release is still `BLOCKED_EXTERNAL`, consistently with Task 12, because:

1. no thesis data/slides directories or configured external locations were
   supplied;
2. release-owner approval for `v1.0.0-thesis-rc` was not supplied; and
3. the release tag was not created.

Those owner-controlled actions are deliberately not fabricated or inferred by
this audit.
