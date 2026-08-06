Set-Location $PSScriptRoot
$javaCmd = if ($env:JAVA_HOME) { "$env:JAVA_HOME\bin\java.exe" } else { "java" }
& $javaCmd -jar "use-gui\target\use-gui.jar" -nr
