# USE BDI Plugin User Guide

This guide is the operator path for the USE 7.1.1 distribution with the BDI
plugin. It covers the GUI import flow and the Auction case study used by the
thesis evidence. The plugin is an extension of USE; the UML/OCL model remains
the source of truth for the USE snapshot.

## Prerequisites

- Java 21 on `PATH`.
- Maven 3.9 or newer when building from source.
- This repository or a packaged USE 7.1.1 distribution.

The checked-in Auction files are under
`use-bdi-plugin/src/test/resources/fixtures/casestudy/auction/`:

- `Auction.use` - UML/OCL model.
- `auctioneer.asl` - auctioneer AgentSpeak source.
- `bidder.asl` - bidder AgentSpeak source.

Optional project configuration belongs beside the `.use` file:

```text
auction-project/
  Auction.use
  .bdi-plugin/
    rules.json
    suppressions.json
```

The Explorer loads these files when it opens. If either file is absent, its
status text identifies the standard-rule or empty-suppression default. Invalid
configuration is shown as an error and the Explorer is not opened.

## Build And Start USE

From the repository root, build the assembled distribution:

```powershell
mvn --batch-mode --no-transfer-progress -pl use-assembly -am package
```

Extract the ZIP and find the directory containing `lib/use-gui.jar`:

```powershell
$extractRoot = Join-Path $PWD 'use-assembly\target\bdi-dist'
Expand-Archive .\use-assembly\target\use-7.1.1.zip $extractRoot -Force
$useHome = (Get-ChildItem $extractRoot -Directory |
  Where-Object { Test-Path (Join-Path $_.FullName 'lib\use-gui.jar') } |
  Select-Object -First 1).FullName
java -jar (Join-Path $useHome 'lib\use-gui.jar') '-nr' "-H=$useHome"
```

The `-H` value must be the extracted USE home. It lets USE resolve
`lib/plugins`, including the shaded `use-bdi-plugin-7.1.1.jar`.

## GUI Demo Walkthrough

### 1. Load the UML/OCL model

1. In USE, click `File > Open specification...`.
2. Select `Auction.use`.
3. Click `View > Create View > Class diagram` to show the four domain classes,
   operations, associations, and OCL constraints.
4. If an object snapshot is needed, create objects with the standard USE
   command pane first, then click `View > Create View > Object diagram`.
   The `Object diagram` menu entry is only a view; it does not create objects
   by itself.

### 2. Import AgentSpeak

1. Click `Plugins > AgentSpeak > Import AgentSpeak...`.
2. In the file chooser, select both `auctioneer.asl` and `bidder.asl` with
   `Ctrl`-click, then confirm.
3. In the BDI Explorer, expand the two source files. The checked-in pair has
   four initial beliefs, two initial goals, and four plans in total.
4. Select a belief, goal, plan, or step to show its source span and source
   excerpt in the detail area.
5. Open the `Problems` tab to inspect retained diagnostics and consistency
   rows. A valid parser import should not create an `ASL-001` syntax error.

### 3. Review mappings

1. Select the `Mapping` tab beside `Explorer` and `Problems`.
2. Review the exact-name and operation-arity suggestions.
3. Select a candidate and click `Apply selected suggestion`, or use the
   binding controls to add/update it explicitly.
4. Use `Save` to write a `.bdimap.json` mapping file and `Load` to restore it.
5. Return to `Problems` after a mapping change. Stale targets are reported as
   `MAP-003`; a suggestion is not proof of semantic consistency.

Mapping schema `0.2.0` stores path-bearing sources relative to the active USE
project. Loading a `0.1.0` mapping is supported and the next Save writes the
portable format. A mapping cannot be loaded or saved for an unnamed/in-memory
USE model because no trustworthy project root exists.

### 4. Show USE views during the presentation

- `View > Create View > Class diagram`: show the UML structure.
- `View > Create View > Object diagram`: show the current object/link
  snapshot, after objects have been created.
- `Plugins > AgentSpeak > Import AgentSpeak...`: show the import entry point.
- BDI Explorer `Explorer` tab: show beliefs, goals, plans, and ordered steps.
- BDI Explorer `Problems` tab: show rule ID, severity, certainty, source, and
  evidence.
- BDI Explorer `Mapping` tab: show confirmed links and save/load controls.

The tracked screenshots and their exact UI path are indexed in
`docs/project/evidence/ui-screenshots.md`.

## Current Workflow Limits

- Legacy `0.1.0` suppression hashes cannot be converted back into source paths.
  They remain legacy-only and intentionally do not match after moving the
  checkout; recreate a reviewed suppression to store a portable v2 identity.
- There is no one-click export of the current live Problems state. The Auction
  evidence command creates the verified analysis reports; `ReportMain` alone
  is only a serializer demonstration.
- Re-open/re-import the Explorer after changing the USE model or snapshot; the
  current view does not subscribe to later host-state changes.

## Reproducible Evidence Run

The complete Auction evidence path is non-interactive and is useful before a
presentation:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\auction-evidence.ps1
```

The expected final marker is `AUCTION_EVIDENCE_OK`. The bundle covers the
baseline, structural, signature, reference, and OCL mutant slices. It records
`MAP-003`, `SIG-001`, `REF-001`, and `OCL-001` outcomes without changing the
USE core or the checked-in UML/OCL fixture.

## Troubleshooting

### The AgentSpeak menu is missing

1. Stop USE completely.
2. Confirm that
   `lib/plugins/use-bdi-plugin-7.1.1.jar` exists in the extracted USE home.
3. Start USE again with `-H` set to that same home.
4. Run the package smoke if the JAR was built locally:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1
```

Do not copy the test JAR into `lib/plugins`; USE needs the shaded runtime JAR.

### Import reports a source error

Open the `Problems` tab and use the source path, one-based line, and column in
the diagnostic. `ASL-001` means Jason rejected the syntax. Valid Jason syntax
that the normalized IR cannot represent is retained as `ASL-002` unsupported
evidence instead of being silently discarded.

### The GUI smoke is unavailable on a headless machine

The parser, report, package, and clean-clone gates remain runnable without a
display. The GUI check requires a desktop session and is therefore reported
separately by `smoke.ps1`.
