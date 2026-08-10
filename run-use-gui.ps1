Set-Location $PSScriptRoot
if (-not (Test-Path "use-gui\target\use-gui.jar")) {
    Write-Host "[INFO] use-gui.jar not found. Building project with Maven..." -ForegroundColor Yellow
    mvn package -pl use-assembly -am
}
New-Item -ItemType Directory -Force -Path "use-gui\lib\plugins" | Out-Null
if (Test-Path "use-bdi-plugin\target\use-bdi-plugin-7.1.1.jar") {
    Copy-Item -Force "use-bdi-plugin\target\use-bdi-plugin-7.1.1.jar" "use-gui\lib\plugins\"
}
$javaCmd = if ($env:JAVA_HOME) { "$env:JAVA_HOME\bin\java.exe" } else { "java" }
& $javaCmd -jar "use-gui\target\use-gui.jar" -nr

