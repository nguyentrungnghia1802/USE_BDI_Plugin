[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$report = Join-Path $repoRoot 'use-bdi-plugin\target\performance\bdi-import-index.json'

Push-Location $repoRoot
try {
    & mvn -pl use-bdi-plugin -Dtest=BdiPerformanceBenchmarkTest test
    if ($LASTEXITCODE -ne 0) {
        throw "Performance benchmark failed with exit code $LASTEXITCODE."
    }
    if (-not (Test-Path -LiteralPath $report)) {
        throw "Performance report was not created: $report"
    }
    Get-Content -LiteralPath $report -Raw
}
finally {
    Pop-Location
}
