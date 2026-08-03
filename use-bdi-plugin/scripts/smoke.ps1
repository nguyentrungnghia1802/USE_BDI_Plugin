[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$distributionZip = Join-Path $repoRoot 'use-assembly\target\use-7.1.1.zip'
$testJar = Join-Path $repoRoot 'use-bdi-plugin\target\use-bdi-plugin-7.1.1-tests.jar'
$smokeRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("use-bdi-plugin-smoke-" + [guid]::NewGuid())

Push-Location $repoRoot
try {
    & mvn -pl use-assembly -am package
    if ($LASTEXITCODE -ne 0) {
        throw "Maven package failed with exit code $LASTEXITCODE."
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

    $validFixture = Join-Path $repoRoot 'use-bdi-plugin\src\test\resources\fixtures\asl\valid\minimal.asl'
    $invalidFixture = Join-Path $repoRoot 'use-bdi-plugin\src\test\resources\fixtures\asl\invalid\missing-plan-body.asl'
    & java -cp "$pluginJar;$testJar" org.tzi.use.plugins.bdi.importer.PackagedParserSmoke `
        $validFixture $invalidFixture
    if ($LASTEXITCODE -ne 0) {
        throw "Packaged parser smoke failed with exit code $LASTEXITCODE."
    }

    $useGuiJar = Join-Path $useHome.FullName 'lib\use-gui.jar'
    & java -cp "$useGuiJar;$testJar" org.tzi.use.plugins.bdi.PluginGuiSmoke $useHome.FullName
    if ($LASTEXITCODE -ne 0) {
        throw "GUI smoke failed with exit code $LASTEXITCODE."
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
