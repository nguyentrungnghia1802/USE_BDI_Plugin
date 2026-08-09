$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\.."))
Push-Location $repoRoot
try {
    & mvn --batch-mode --no-transfer-progress `
        -pl use-bdi-plugin `
        clean `
        "-Dtest=AuctionFaultInjectionTest,AuctionEvidenceArtifactTest" test
    if ($LASTEXITCODE -ne 0) {
        throw "Auction fault-injection/artifact tests failed with exit code $LASTEXITCODE"
    }

    & powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\auction-baseline.ps1
    if ($LASTEXITCODE -ne 0) {
        throw "Auction baseline script failed with exit code $LASTEXITCODE"
    }

    & powershell -ExecutionPolicy Bypass -File .\use-bdi-plugin\scripts\auction-structural-mutant.ps1
    if ($LASTEXITCODE -ne 0) {
        throw "Auction structural mutant script failed with exit code $LASTEXITCODE"
    }

    $artifactPaths = @(
        ".\docs\project\evidence\auction-ground-truth.json",
        ".\docs\project\evidence\auction-metrics.csv",
        ".\docs\project\evidence\auction-architecture.mmd",
        ".\docs\project\evidence\ir-class-diagram.mmd",
        ".\docs\project\evidence\bdi-metamodel-diagram.mmd",
        ".\docs\project\evidence\auction-mapping-examples.md"
    )
    foreach ($artifactPath in $artifactPaths) {
        if (-not (Test-Path -LiteralPath $artifactPath -PathType Leaf)) {
            throw "Missing Auction evidence artifact: $artifactPath"
        }
    }

    Write-Output "AUCTION_EVIDENCE_OK"
}
finally {
    Pop-Location
}
