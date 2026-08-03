# USE BDI Plugin - Phase 0 spike

This module is the smallest verified USE plugin shell for the thesis project. It
adds `Plugins > AgentSpeak > Hello BDI Plugin` and deliberately contains no
AgentSpeak import or consistency-checking features yet.

## Build and automated smoke

From the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1
```

The script builds the reactor through `package`, checks that the plugin JAR is in
the USE distribution, starts the GUI briefly, and verifies the menu hierarchy.

## Manual GUI run

```powershell
mvn -pl use-assembly -am clean package
Expand-Archive .\use-assembly\target\use-7.1.1.zip .\use-assembly\target\bdi-dist
java -jar .\use-assembly\target\bdi-dist\use-7.1.1\lib\use-gui.jar -nr -H=.\use-assembly\target\bdi-dist\use-7.1.1
```

Open `Plugins > AgentSpeak > Hello BDI Plugin`. The action is enabled even when
no UML/OCL model is loaded.
