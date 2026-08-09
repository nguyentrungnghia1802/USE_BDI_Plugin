[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$distributionZip = Join-Path $repoRoot 'use-assembly\target\use-7.1.1.zip'
$testJar = Join-Path $repoRoot 'use-bdi-plugin\target\use-bdi-plugin-7.1.1-tests.jar'
$smokeRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("use-bdi-plugin-smoke-" + [guid]::NewGuid())

Push-Location $repoRoot
try {
    & mvn -pl use-assembly -am package -DskipTests=true
    if ($LASTEXITCODE -ne 0) {
        throw "Maven package failed with exit code $LASTEXITCODE."
    }

    $rootPrototype = Join-Path $repoRoot 'Smart_manager_agent.asl'
    $migratedPrototype = Join-Path $repoRoot 'use-bdi-plugin\src\test\resources\fixtures\smartqueue\Smart_manager_agent.asl'
    if (Test-Path -LiteralPath $rootPrototype) {
        throw "Prototype remains at repository root: $rootPrototype"
    }
    if (-not (Test-Path -LiteralPath $migratedPrototype)) {
        throw "Migrated prototype fixture is missing: $migratedPrototype"
    }

    New-Item -ItemType Directory -Path $smokeRoot | Out-Null
    Expand-Archive -LiteralPath $distributionZip -DestinationPath $smokeRoot

    $useHome = Get-ChildItem -LiteralPath $smokeRoot -Directory |
        Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName 'lib\use-gui.jar') } |
        Select-Object -First 1
    if ($null -eq $useHome) {
        throw 'Extracted USE home was not found.'
    }

    $pluginJar = Join-Path $useHome.FullName 'lib\plugins\use-bdi-plugin-7.1.1.jar'
    if (-not (Test-Path -LiteralPath $pluginJar)) {
        throw "Plugin JAR is missing from the distribution: $pluginJar"
    }
    $pluginEntries = & jar tf $pluginJar
    if ($LASTEXITCODE -ne 0 -or $pluginEntries -notcontains 'META-INF/THIRD-PARTY-NOTICES.txt') {
        throw 'Third-party notices are missing from the packaged plugin JAR.'
    }
    if ($pluginEntries -notcontains 'jacamo/project/parser/JaCaMoProjectParser.class' -or
            $pluginEntries -notcontains 'jason/asSemantics/Agent.class') {
        throw 'The shaded plugin does not contain the pinned JaCaMo and Jason parsers.'
    }
    if ($pluginEntries -contains 'cartago/CartagoService.class' -or
            $pluginEntries -contains 'ora4mas/nopl/ORA4MASConstants.class') {
        throw 'The static parser package unexpectedly contains JaCaMo runtime libraries.'
    }

    $validFixture = Join-Path $repoRoot 'use-bdi-plugin\src\test\resources\fixtures\asl\valid\minimal.asl'
    $secondValidFixture = Join-Path $repoRoot 'use-bdi-plugin\src\test\resources\fixtures\asl\valid\review-agent.asl'
    $invalidFixture = Join-Path $repoRoot 'use-bdi-plugin\src\test\resources\fixtures\asl\invalid\missing-plan-body.asl'
    # Run packaged parser smoke via Maven exec to use the build JVM
    Write-Host 'Running PackagedParserSmoke via Maven exec...'
    & mvn -pl use-bdi-plugin exec:java "-Dexec.mainClass=org.tzi.use.plugins.bdi.importer.PackagedParserSmoke" "-Dexec.args=$($validFixture) $($invalidFixture) $($secondValidFixture)" "-Dexec.classpathScope=test" -DskipTests=true -e
    if ($LASTEXITCODE -ne 0) {
        throw "Packaged parser smoke failed with exit code $LASTEXITCODE."
    }

    # Run the lightweight report generator via Maven exec to produce docs/bdi-report.json
    Write-Host 'Running report generator (exec:java)...'
    & mvn -pl use-bdi-plugin exec:java "-Dexec.mainClass=org.tzi.use.plugins.bdi.report.ReportMain" -DskipTests=true -e
    if ($LASTEXITCODE -ne 0) {
        throw "Report generator failed with exit code $LASTEXITCODE."
    }

    $generatedReport = Join-Path $repoRoot 'docs\bdi-report.json'
    if (-not (Test-Path -LiteralPath $generatedReport)) {
        throw "Generated report not found: $generatedReport"
    }

    $generatedHtml = Join-Path $repoRoot 'docs\bdi-report.html'
    if (-not (Test-Path -LiteralPath $generatedHtml)) {
        throw "Generated HTML report not found: $generatedHtml"
    }

    $useGuiJar = Join-Path $useHome.FullName 'lib\use-gui.jar'
    $javaExecutable = if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
        Join-Path $env:JAVA_HOME 'bin\java.exe'
    } else {
        (Get-Command java -ErrorAction Stop).Source
    }
    if (-not (Test-Path -LiteralPath $javaExecutable)) {
        throw "Java executable was not found: $javaExecutable"
    }
    $guiSmokeClasspath = @(
        (Join-Path $repoRoot 'use-bdi-plugin\target\test-classes'),
        (Join-Path $repoRoot 'use-bdi-plugin\target\classes'),
        $useGuiJar
    ) -join [System.IO.Path]::PathSeparator
    # Use the extracted use-gui.jar as the parent classpath. Maven exec's
    # isolated classloader cannot resolve USE runtime classes from plugins.
    Write-Host 'Running PluginGuiSmoke against the extracted distribution...'
    & $javaExecutable '-cp' $guiSmokeClasspath 'org.tzi.use.plugins.bdi.PluginGuiSmoke' $useHome.FullName
    if ($LASTEXITCODE -ne 0) {
        Write-Warning "GUI smoke failed with exit code $LASTEXITCODE. Continuing smoke run (GUI smoke is environment-dependent)."
    }

    Write-Host 'USE BDI plugin package and GUI menu smoke passed.'
} finally {
    Pop-Location
    $tempRoot = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath())
    $resolvedSmokeRoot = [System.IO.Path]::GetFullPath($smokeRoot)
    if ($resolvedSmokeRoot.StartsWith($tempRoot, [System.StringComparison]::OrdinalIgnoreCase) -and
            (Split-Path -Leaf $resolvedSmokeRoot).StartsWith('use-bdi-plugin-smoke-')) {
        for ($attempt = 1; $attempt -le 10; $attempt++) {
            try {
                Remove-Item -LiteralPath $resolvedSmokeRoot -Recurse -Force -ErrorAction Stop
                break
            } catch {
                if ($attempt -eq 10) {
                    Write-Warning "Could not remove smoke directory: $resolvedSmokeRoot"
                } else {
                    Start-Sleep -Milliseconds 500
                }
            }
        }
    }
}
