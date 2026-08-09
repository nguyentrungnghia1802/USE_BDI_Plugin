# Release Package Evidence

This page defines the repeatable package gates for the USE BDI plugin. It
separates the committed-tree package check from the root USE integration-test
gate, which currently has an independent Failsafe handshake failure.

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
- `clean-clone.ps1` ending with `CLEAN_CLONE_OK` is the committed-tree package
  evidence.
- `THESIS_BACKUP_OK` confirms the source archive and manifest were written; it
  does not claim that missing external slides/data have been recovered.
- Root `mvn clean verify` remains a separate release gate until the existing
  `use-gui` `ShellIT` Failsafe handshake issue is resolved under review.
