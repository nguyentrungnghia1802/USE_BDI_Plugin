# Release Package Evidence

This page defines the repeatable package gates for the USE BDI plugin. It
records both the committed-tree package check and the root USE integration-test
gate.

## Reproduction

Run the following from the repository root:

```powershell
mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am test
mvn --batch-mode --no-transfer-progress -pl use-assembly -am package
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\clean-clone.ps1
```

The package gate checks the shaded plugin JAR, `useplugin.xml`, Jason runtime
classes, embedded third-party notices, the plugin entry in the distribution ZIP,
the parser/report smoke, and the GUI menu probe. The clean-clone script uses
the exact committed `HEAD`, so it does not accidentally package an unstaged
working-tree file.

## Backup Procedure

Create a source archive and a manifest with:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\backup-thesis-artifacts.ps1
```

The script archives committed source and documentation with `git archive`,
copies any present `data`, `slides`, or `presentation` directories, and lists
missing external material in a JSON manifest. It never deletes an existing
directory and records uncommitted paths separately from the source archive.

## Evidence Interpretation

- `smoke.ps1` ending with `GUI_SMOKE_OK` is the local desktop UI evidence.
- `clean-clone.ps1` ending with `CLEAN_CLONE_REPRODUCIBILITY_OK` is the
  committed-tree package evidence.
- `THESIS_BACKUP_OK` is emitted only when source, data, reports, and slides are
  all present. `THESIS_BACKUP_BLOCKED_EXTERNAL` records a readable hashed
  source/report backup while keeping absent owner-supplied data/slides explicit.
- Root `mvn clean verify` passed after the ADR-0019 integration-test repair;
  the release tag and complete external-artifact backup remain separate gates.

## Verified Run - 2026-08-09

- Commit tested: `594b2b07e2b24e75c7d1473aafcfab235427f382`.
- `clean-clone.ps1` passed from that committed `HEAD` with
  `CLEAN_CLONE_REPRODUCIBILITY_OK`.
- The clean clone built the full `use-assembly` package and verified the
  shaded plugin, Jason runtime class, embedded notice, and ZIP plugin entry.
- The root `mvn --batch-mode --no-transfer-progress clean verify` run passed
  `use-core`, 121 `use-gui` integration tests, 74 plugin tests, and assembly.

## Task 12 release-candidate run - 2026-08-17

Candidate `6a84fc5ee0cff7e06c1873d0a5d7c05e52fdc0c0` passed the prescribed ordered
plugin-reactor, assembly, GUI smoke, headless smoke, Auction evidence, Auction
evaluation, general performance, diagram performance, clean-clone, root clean
verify, and diff gates. The clean clone ended with
`CLEAN_CLONE_REPRODUCIBILITY_OK` for that exact commit. Package inspection
confirmed the JaCaMo project parser, Jason agent runtime, CArtAgO operation and
artifact APIs, Moise OS parser/model plus schema, embedded third-party notice,
`useplugin.xml`, and root `COPYING`; the dependency tree contained no EMF or
Sirius runtime.

The generated release-evidence manifest covered 40 hashed artifacts. Its first
successful run recorded SHA-256
`41eacdccc301208c33886b6b5642cbe9db6fed282a8d700662aacb9d9da76cfb`;
the final manifest is regenerated after the evidence-only commit. The backup
created a readable committed-source archive and report copy, but correctly
ended `THESIS_BACKUP_BLOCKED_EXTERNAL: missing=data,slides`. No tag was created
because release-owner approval and a complete external-artifact backup were
not available.
