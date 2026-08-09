# Third-Party Notices And License Scope

This document records the license evidence used for the current USE BDI
plugin release package. It is intentionally scoped to the checked-in USE
distribution and the runtime dependencies resolved by `use-bdi-plugin`.

## USE Distribution

USE is distributed under GNU General Public License version 2. The repository
copy is `COPYING` at the repository root. A release archive must retain that
file. The root `README.md` describes USE as a GPL-licensed project and the
assembly includes the repository license file.

## Shaded Plugin Runtime

The plugin POM pins JaCaMo `1.3.0`, CArtAgO `3.1`, and Jason `3.3.0`. JaCaMo
is used for its official static `.jcm` parser/model and CArtAgO for static
artifact API inspection; all dependency transitives are excluded. The verified
runtime dependency tree is:

| Component | Maven coordinate | License evidence | Project |
| --- | --- | --- | --- |
| JaCaMo project parser/model 1.3.0 | `org.jacamo:jacamo:1.3.0` | GNU LGPLv3 | [github.com/jacamo-lang/jacamo](https://github.com/jacamo-lang/jacamo) |
| CArtAgO artifact API 3.1 | `org.jacamo:cartago:3.1` | GNU LGPL 2.1 or later | [github.com/CArtAgO-lang/cartago](https://github.com/CArtAgO-lang/cartago) |
| Jason interpreter 3.3.0 | `io.github.jason-lang:jason-interpreter:3.3.0` | GNU LGPLv3 | [jason-lang.github.io](https://jason-lang.github.io) |
| JADE 4.3 | `net.sf.ingenias:jade:4.3` | GNU LGPL | [jade.tilab.com](http://jade.tilab.com/) |
| JSON Processing provider 1.1.4 | `org.glassfish:javax.json:1.1.4` | CDDL 1.1 or GPLv2 with Classpath Exception | [javaee.github.io/jsonp](https://javaee.github.io/jsonp) |

The exact notice text is embedded in the runtime artifact at
`use-bdi-plugin/src/main/resources/META-INF/THIRD-PARTY-NOTICES.txt` and is
packaged into `use-bdi-plugin-7.1.1.jar`. The redundant `javax.json-api`
artifact is excluded by the POM because the selected GlassFish provider already
contains the required `javax.json` API classes.

Verify the packaged notice and dependency classes with:

```powershell
jar tf .\use-bdi-plugin\target\use-bdi-plugin-7.1.1.jar |
  Select-String 'META-INF/THIRD-PARTY-NOTICES.txt|jacamo/project/parser/JaCaMoProjectParser.class|cartago/OPERATION.class|jason/asSemantics/Agent.class'
mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin dependency:tree -Dscope=runtime
```

## Scope And Limitations

- The table above is based on the checked-in plugin POM, Maven runtime tree,
  and embedded notice file; no license is inferred from package names.
- The plugin is shaded without relocation. The release check must therefore
  preserve the notice and the root USE `COPYING` file.
- The CArtAgO JAR is packaged so user artifact classes can link to its API, but
  the plugin does not create a workspace or invoke `CartagoService`; Moise and
  other JaCaMo runtime transitives remain absent.
- This document does not claim a legal review of every historical USE
  dependency or of external thesis materials that are not in this repository.
  A release maintainer must rerun the dependency tree when changing the POM.
- External presentation slides and data were not present in this checkout on
  2026-08-09; they cannot be licensed or archived by this repository alone.
