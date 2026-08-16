[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$report = Join-Path $repoRoot 'use-bdi-plugin\target\performance\diagram-performance.json'

function Invoke-DiagramBenchmark {
    $identity = & git rev-parse --verify HEAD 2>$null
    $gitExitCode = $LASTEXITCODE
    $identity = ([string]$identity).Trim()
    if ($gitExitCode -ne 0 -or [string]::IsNullOrWhiteSpace($identity)) {
        throw 'Could not resolve the benchmark Git identity.'
    }
    & mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin "-Dbdi.benchmark.identity=$identity" '-Dtest=DiagramPerformanceBenchmarkTest' test | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "Diagram performance benchmark failed with exit code $LASTEXITCODE."
    }
    if (-not (Test-Path -LiteralPath $report)) {
        throw "Diagram performance report was not created: $report"
    }
    return Get-Content -LiteralPath $report -Raw | ConvertFrom-Json
}

function Get-StructuralProjection($result) {
    return [ordered]@{
        structureFingerprint = $result.structureFingerprint
        cases = @($result.cases | ForEach-Object {
            [ordered]@{
                name = $_.name
                nodes = $_.nodes
                edges = $_.edges
                groups = $_.groups
                visibleNodes = $_.visibleNodes
                visibleEdges = $_.visibleEdges
                mode = $_.mode
                hiddenLayers = @($_.hiddenLayers)
                svgSha256 = $_.svgSha256
            }
        })
    } | ConvertTo-Json -Depth 6 -Compress
}

Push-Location $repoRoot
try {
    $first = Invoke-DiagramBenchmark
    $second = Invoke-DiagramBenchmark
    $firstStructure = Get-StructuralProjection $first
    $secondStructure = Get-StructuralProjection $second
    if ($firstStructure -cne $secondStructure) {
        throw 'Diagram benchmark structural output changed between repeated runs.'
    }
    if (@($second.cases).Count -ne 4) {
        throw "Expected four canonical cases, found $(@($second.cases).Count)."
    }
    Get-Content -LiteralPath $report -Raw
    Write-Output "DIAGRAM_PERFORMANCE_OK: four canonical cases preserved structural fingerprint $($second.structureFingerprint) across two runs"
}
finally {
    Pop-Location
}
