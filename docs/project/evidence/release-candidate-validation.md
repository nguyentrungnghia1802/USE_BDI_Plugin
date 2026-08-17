# Task 12 Release-Candidate Validation

Date: 2026-08-17 (Asia/Bangkok)

Technical result: **PASS**  
Release result: **BLOCKED_EXTERNAL**

The implementation, packaging, screenshots, evidence, and reproducibility
gates are complete. Release publication is intentionally blocked by the two
owner-controlled inputs named in Task 12: external thesis data/slides for the
complete backup, and explicit approval to create `v1.0.0-thesis-rc`.

## Ordered gate record

| Gate | Result | Evidence |
|---|---|---|
| Raster screenshots | PASS | `ReleaseScreenshotCaptureTest`; 11 reviewed 1280×820 Auction PNGs; `RELEASE_SCREENSHOTS_OK` |
| Diagram performance | PASS | two runs; 4 cases; fingerprint `19c5cc0d5d9e103db160a71c3e0ba2e45f9ef20eedfacb803b1f9f4a64135591` |
| Plugin reactor | PASS | 223 discovered, 222 executed, 1 opt-in capture skipped, 0 failures/errors |
| Assembly | PASS | five-module reactor; ZIP and TAR.GZ generated |
| GUI smoke | PASS | packaged parser/report plus `GUI_SMOKE_OK` |
| Headless smoke | PASS | expected finding/input-error exits and `HEADLESS_QUALITY_GATE_OK` |
| Auction evidence | PASS | baseline, fault injection, structural mutant; `AUCTION_EVIDENCE_OK` |
| Auction evaluation | PASS | 5 cases: 1 baseline pass, 4 mutants detected, 0 misses/errors |
| General performance | PASS | 2 warmups, 7 measurements; marker `PERFORMANCE_BENCHMARK_OK` |
| Clean clone | PASS | committed candidate `6a84fc5e`; shaded plugin and distribution reproduced |
| Root verify | PASS | `use-core`, `use-gui`, plugin, and assembly reactor success |
| Package/license | PASS | eight required entries, root `COPYING`, no EMF/Sirius dependency |
| Diff check | PASS | `git diff --check` |

The release manifest script hashes the committed source archive, assembly
archives, shaded plugin, canonical documents, Ecore/profile diagrams, Auction
manifest/results, diagram evidence, all release screenshots, current-analysis
JSON/SVG exports, and notices. The generated manifest is intentionally under
`target/release-evidence/`; its path and final hash are printed by the script.

## Backup and tag gates

The backup at `target/backups/thesis-20260817-070524` contains a readable
committed-source ZIP and copied generated reports. SHA-256 values from that run:

- backup manifest: `3d0f6f1d2ea10b61e7a1ff668a527a448ddad6db965a5f9ae2d83d1f52f08449`;
- source archive: `fad81d624f6ef624ceaf396fbcd126813b492112eb482f00f5a9eb33f3e181ed`;
- restore spot-check: the archived root `pom.xml` was readable.

The manifest records `backupComplete=false` and
`missingRequiredKinds=[data,slides]`. These external artifacts were not present
and were not fabricated. Release-owner approval was not supplied, so tag
`v1.0.0-thesis-rc` was not created.

## Failures found and repaired

- The first production Swing capture initialized the plugin runtime without a
  host `MainWindow`; capture now starts USE with plugins disabled and exercises
  the production views directly.
- The first mutant capture did not expose `SIG-001` through the Explorer-only
  path; it now consumes the same headless analysis and trace contributor used
  by the reviewed mutant evidence.
- The first manifest run used a .NET API absent from Windows PowerShell 5.1;
  both release scripts now use a containment-checking relative-path helper.
- Java writes its version to stderr, which PowerShell promoted under strict
  error handling; the script now captures that native output and separately
  checks the process exit code.

After the fixes, affected focused checks were rerun. The final committed-tree
clean-clone and focused documentation/release contracts are rerun after this
evidence record is committed.

## Claim sanity review

The exact phrase scan found only explicit prohibitions/limitations, not positive
claims of full runtime integration, novelty primacy, general correctness, or an
editable graphical editor. Static-only qualifiers remain visible in the UI and
screenshots; Auction metrics remain scoped; `UNKNOWN` remains distinct from
PASS; and no missing external artifact is represented as archived.

## Final gate

```text
Raster screenshots: PASS
Diagram performance: PASS
Plugin reactor: PASS
Assembly: PASS
GUI smoke: PASS
Headless smoke: PASS
Auction evidence: PASS
Auction evaluation: PASS
Performance: PASS
Clean clone: PASS
Root verify: PASS
Package/license: PASS
Backup: BLOCKED_EXTERNAL (data and slides absent)
Release-owner approval: NOT PROVIDED
Tag: NOT CREATED
Docs/checklist: PASS
git diff --check: PASS
Open failures: NONE; two explicit owner-controlled release blockers remain
RELEASE RESULT: BLOCKED_EXTERNAL
```
