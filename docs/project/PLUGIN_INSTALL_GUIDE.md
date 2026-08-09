# USE BDI Plugin Install Guide

For the complete presentation walkthrough, see `USER_GUIDE.md`. For extension
and test conventions, see `DEVELOPER_GUIDE.md`. The release license scope is
recorded in `THIRD_PARTY_NOTICES.md`.

## Prerequisites

- Java 21 on `PATH`.
- Maven 3.9 or newer for building from source.
- A checkout of this repository on the plugin branch.

The plugin is built for USE `7.1.1` and Jason `3.3.0`. USE and GUI
dependencies are provided by the distribution; Jason runtime dependencies are
shaded into the plugin JAR.

## Build the distribution

From the repository root:

```powershell
mvn --batch-mode --no-transfer-progress -pl use-assembly -am package
```

The assembled archive is:
`use-assembly/target/use-7.1.1.zip`.

Extract it to a directory without spaces if the local Java setup has legacy
launcher issues. The expected USE home contains `lib/use-gui.jar` and
`lib/plugins/use-bdi-plugin-7.1.1.jar`.

## Start USE with the plugin

PowerShell:

```powershell
$useHome = (Resolve-Path .\use-assembly\target\use-7.1.1).Path
java -jar (Join-Path $useHome 'lib\use-gui.jar') `
  '-nr' "-H=$useHome"
```

Equivalent command prompt form:

```cmd
java -jar "<use-home>\lib\use-gui.jar" -nr -H="<use-home>"
```

Do not place the test JAR in the runtime plugin directory. The runtime file
is the shaded `use-bdi-plugin-7.1.1.jar` produced by the package phase.

## Verify the UI

1. Open `Plugins > AgentSpeak > Hello BDI Plugin`. A successful action confirms
   that USE loaded the descriptor and runtime class.
2. Open `Plugins > AgentSpeak > Import AgentSpeak...`.
3. Select an `.asl` fixture, then show the BDI Explorer tree.
4. Open the `Problems` tab to inspect rule ID, certainty, source location, and
   evidence.

The automated package/menu smoke is:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\smoke.ps1
```

The expected GUI marker is
`GUI_SMOKE_OK: Plugins > AgentSpeak > Hello BDI Plugin + Import AgentSpeak...`.

## Install into an existing extracted USE home

Only when the target home is the same USE `7.1.1` distribution, build the
plugin and copy the shaded runtime JAR:

```powershell
mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin -am package
Copy-Item .\use-bdi-plugin\target\use-bdi-plugin-7.1.1.jar `
  '<use-home>\lib\plugins\use-bdi-plugin-7.1.1.jar' -Force
```

Restart USE after replacing the JAR. Verify the package includes
`META-INF/THIRD-PARTY-NOTICES.txt`:

```powershell
jar tf .\use-bdi-plugin\target\use-bdi-plugin-7.1.1.jar |
  Select-String 'META-INF/THIRD-PARTY-NOTICES.txt'
```

## Clean-clone check

To validate that committed files, not local uncommitted artifacts, are enough
to assemble the plugin:

```powershell
powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\clean-clone.ps1
```

The root `mvn clean verify` gate is covered by ADR-0019 and currently passes.
The release checklist still tracks the tag and the complete backup separately
because external slide/data directories are not present in this checkout.
