[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$distributionZip = Join-Path $repoRoot 'use-assembly\target\use-7.1.1.zip'
$smokeRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("use-bdi-headless-" + [guid]::NewGuid())
$outputRoot = Join-Path $repoRoot 'use-bdi-plugin\target\headless-smoke'

Push-Location $repoRoot
try {
    & mvn --batch-mode --no-transfer-progress -pl use-assembly -am package -DskipTests=true
    if ($LASTEXITCODE -ne 0) {
        throw "Maven package failed with exit code $LASTEXITCODE"
    }
    New-Item -ItemType Directory -Path $smokeRoot | Out-Null
    Expand-Archive -LiteralPath $distributionZip -DestinationPath $smokeRoot
    $useHome = Get-ChildItem -LiteralPath $smokeRoot -Directory |
        Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName 'lib\use-gui.jar') } |
        Select-Object -First 1
    if ($null -eq $useHome) {
        throw 'Extracted USE home was not found'
    }

    $pluginJar = Join-Path $useHome.FullName 'lib\plugins\use-bdi-plugin-7.1.1.jar'
    $useGuiJar = Join-Path $useHome.FullName 'lib\use-gui.jar'
    $classpath = @($pluginJar, $useGuiJar) -join [System.IO.Path]::PathSeparator
    $javaExecutable = if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
        Join-Path $env:JAVA_HOME 'bin\java.exe'
    } else {
        (Get-Command java -ErrorAction Stop).Source
    }
    New-Item -ItemType Directory -Path $outputRoot -Force | Out-Null
    $json = Join-Path $outputRoot 'auction.json'
    $html = Join-Path $outputRoot 'auction.html'
    $useFile = Join-Path $repoRoot 'use-bdi-plugin\src\test\resources\fixtures\casestudy\auction\Auction.use'
    $auctioneer = Join-Path $repoRoot 'use-bdi-plugin\src\test\resources\fixtures\casestudy\auction\auctioneer.asl'
    $bidder = Join-Path $repoRoot 'use-bdi-plugin\src\test\resources\fixtures\casestudy\auction\bidder.asl'

    & $javaExecutable '-cp' $classpath 'org.tzi.use.plugins.bdi.cli.BdiQualityGateMain' `
        '--use' $useFile '--asl' $auctioneer '--asl' $bidder `
        '--json' $json '--html' $html '--timestamp' '2026-08-10T00:00:00Z' '--overwrite'
    if ($LASTEXITCODE -ne 1) {
        throw "Auction finding run expected exit 1 but received $LASTEXITCODE"
    }
    if (-not (Test-Path -LiteralPath $json) -or -not (Test-Path -LiteralPath $html)) {
        throw 'Headless reports were not created'
    }
    if ((Get-Content -Raw -LiteralPath $json) -notmatch '"issues":\[\{') {
        throw 'Headless JSON report has no real findings'
    }

    $missing = Join-Path $outputRoot 'missing.asl'
    & $javaExecutable '-cp' $classpath 'org.tzi.use.plugins.bdi.cli.BdiQualityGateMain' `
        '--use' $useFile '--asl' $missing '--json' (Join-Path $outputRoot 'missing.json') '--overwrite'
    if ($LASTEXITCODE -ne 3) {
        throw "Missing-input run expected exit 3 but received $LASTEXITCODE"
    }
    Write-Output 'HEADLESS_QUALITY_GATE_OK: packaged exits 1/3 and Auction JSON/HTML reports verified'
} finally {
    Pop-Location
    $tempRoot = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath())
    $resolvedSmokeRoot = [System.IO.Path]::GetFullPath($smokeRoot)
    if ($resolvedSmokeRoot.StartsWith($tempRoot, [System.StringComparison]::OrdinalIgnoreCase) -and
            (Split-Path -Leaf $resolvedSmokeRoot).StartsWith('use-bdi-headless-') -and
            (Test-Path -LiteralPath $resolvedSmokeRoot)) {
        Remove-Item -LiteralPath $resolvedSmokeRoot -Recurse -Force
    }
}
