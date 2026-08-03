# USE BDI Plugin

This module contains the verified USE plugin shell for the thesis project. It
adds `Plugins > AgentSpeak > Hello BDI Plugin` and includes the first importer
slice: a Jason 3.3.0 adapter that parses multiple AgentSpeak files into ordered,
immutable, Jason-independent summaries. File selection, partial-success import,
the full BDI IR, and consistency checking are not implemented yet.

## Parser test

From the repository root:

```powershell
mvn -pl use-bdi-plugin test
```

The parser fixture tests verify one initial belief, one initial goal, one plan,
the pinned parser version, and a structured `ASL-001` diagnostic for invalid
syntax. Diagnostics carry severity, source file, message, and a one-based line
and column when Jason provides an error token. The adapter disables Jason's
optional web mind inspector before initializing the parser so an offline import
does not open a background HTTP server. Multi-file imports preserve input order,
return immutable per-file summaries, and currently fail fast on the first error.

## Build and automated smoke

From the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1
```

The script builds the reactor through `package`, imports two valid fixtures and
checks one invalid fixture using the shaded plugin JAR from the distribution,
starts the GUI briefly, and verifies the menu hierarchy.

## Manual GUI run

```powershell
mvn -pl use-assembly -am clean package
Expand-Archive .\use-assembly\target\use-7.1.1.zip .\use-assembly\target\bdi-dist
java -jar .\use-assembly\target\bdi-dist\use-7.1.1\lib\use-gui.jar -nr -H=.\use-assembly\target\bdi-dist\use-7.1.1
```

Open `Plugins > AgentSpeak > Hello BDI Plugin`. The action is enabled even when
no UML/OCL model is loaded.
