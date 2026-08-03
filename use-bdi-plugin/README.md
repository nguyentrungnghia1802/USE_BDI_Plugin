# USE BDI Plugin

This module contains the verified USE plugin shell for the thesis project. It
adds `Plugins > AgentSpeak > Hello BDI Plugin` and includes the first importer
slice: a Jason 3.3.0 adapter that parses a single valid AgentSpeak file into a
Jason-independent summary. File selection, the full BDI IR, and consistency
checking are not implemented yet.

## Parser test

From the repository root:

```powershell
mvn -pl use-bdi-plugin test
```

The parser fixture test verifies one initial belief, one initial goal, one plan,
and the pinned parser version. The adapter disables Jason's optional web mind
inspector before initializing the parser so an offline import does not open a
background HTTP server.

## Build and automated smoke

From the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1
```

The script builds the reactor through `package`, parses the valid fixture using
the shaded plugin JAR from the distribution, starts the GUI briefly, and
verifies the menu hierarchy.

## Manual GUI run

```powershell
mvn -pl use-assembly -am clean package
Expand-Archive .\use-assembly\target\use-7.1.1.zip .\use-assembly\target\bdi-dist
java -jar .\use-assembly\target\bdi-dist\use-7.1.1\lib\use-gui.jar -nr -H=.\use-assembly\target\bdi-dist\use-7.1.1
```

Open `Plugins > AgentSpeak > Hello BDI Plugin`. The action is enabled even when
no UML/OCL model is loaded.
