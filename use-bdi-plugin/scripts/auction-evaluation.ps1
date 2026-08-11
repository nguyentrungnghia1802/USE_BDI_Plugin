[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$distributionZip = Join-Path $repoRoot 'use-assembly\target\use-7.1.1.zip'
$packageRoot = Join-Path ([System.IO.Path]::GetTempPath()) ('use-bdi-evaluation-package-' + [guid]::NewGuid())
$outputRoot = Join-Path $repoRoot 'docs\project\evidence\auction-evaluation-run'
$repeatRoot = Join-Path ([System.IO.Path]::GetTempPath()) ('use-bdi-evaluation-repeat-' + [guid]::NewGuid())

Push-Location $repoRoot
try {
    & mvn --batch-mode --no-transfer-progress -pl use-assembly -am package -DskipTests=true
    if ($LASTEXITCODE -ne 0) {
        throw "Maven package failed with exit code $LASTEXITCODE"
    }

    New-Item -ItemType Directory -Path $packageRoot | Out-Null
    Expand-Archive -LiteralPath $distributionZip -DestinationPath $packageRoot
    $useHome = Get-ChildItem -LiteralPath $packageRoot -Directory |
        Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName 'lib\use-gui.jar') } |
        Select-Object -First 1
    if ($null -eq $useHome) {
        throw 'Extracted USE home was not found'
    }
    $classpath = @(
        (Join-Path $useHome.FullName 'lib\plugins\use-bdi-plugin-7.1.1.jar'),
        (Join-Path $useHome.FullName 'lib\use-gui.jar')
    ) -join [System.IO.Path]::PathSeparator
    $javaExecutable = if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
        Join-Path $env:JAVA_HOME 'bin\java.exe'
    } else {
        (Get-Command java -ErrorAction Stop).Source
    }
    $manifest = Join-Path $repoRoot 'docs\project\evidence\auction-evaluation-manifest.json'
    New-Item -ItemType Directory -Path $outputRoot -Force | Out-Null
    & $javaExecutable '-cp' $classpath 'org.tzi.use.plugins.bdi.evaluation.EvaluationRunnerMain' `
        '--manifest' $manifest '--root' $repoRoot '--out' $outputRoot `
        '--timestamp' '2026-08-11T00:00:00Z'
    if ($LASTEXITCODE -ne 0) {
        throw "Evaluation runner expected exit 0 but received $LASTEXITCODE"
    }

    foreach ($name in @('evaluation-results.json', 'evaluation-results.csv', 'evaluation-results.html')) {
        if (-not (Test-Path -LiteralPath (Join-Path $outputRoot $name) -PathType Leaf)) {
            throw "Missing evaluation output: $name"
        }
    }
    $json = Get-Content -Raw -LiteralPath (Join-Path $outputRoot 'evaluation-results.json')
    if ($json -notmatch '"passed":1' -or $json -notmatch '"detected":4' -or $json -match 'use-bdi-evaluation-') {
        throw 'Evaluation JSON did not contain the expected scoped result or contains a temporary path'
    }

    New-Item -ItemType Directory -Path $repeatRoot | Out-Null
    & $javaExecutable '-cp' $classpath 'org.tzi.use.plugins.bdi.evaluation.EvaluationRunnerMain' `
        '--manifest' $manifest '--root' $repoRoot '--out' $repeatRoot `
        '--timestamp' '2026-08-11T00:00:00Z'
    if ($LASTEXITCODE -ne 0) {
        throw "Repeated evaluation runner expected exit 0 but received $LASTEXITCODE"
    }
    foreach ($name in @('evaluation-results.json', 'evaluation-results.csv', 'evaluation-results.html')) {
        $firstHash = (Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $outputRoot $name)).Hash
        $repeatHash = (Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $repeatRoot $name)).Hash
        if ($firstHash -ne $repeatHash) {
            throw "Repeated evaluation output is not byte-stable: $name"
        }
    }
    Write-Output 'AUCTION_EVALUATION_OK: reviewed manifest detected four mutants and preserved deterministic outputs'
} finally {
    Pop-Location
    foreach ($path in @($packageRoot, $repeatRoot)) {
        $resolved = [System.IO.Path]::GetFullPath($path)
        $tempRoot = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath())
        if ($resolved.StartsWith($tempRoot, [System.StringComparison]::OrdinalIgnoreCase) -and (Test-Path -LiteralPath $resolved)) {
            Remove-Item -LiteralPath $resolved -Recurse -Force
        }
    }
}
