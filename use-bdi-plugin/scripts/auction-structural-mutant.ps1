$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\.."))
Push-Location $repoRoot
try {
    & mvn --batch-mode --no-transfer-progress `
        -pl use-bdi-plugin `
        "-Dtest=AuctionStructuralMutantTest" test
    if ($LASTEXITCODE -ne 0) {
        throw "Auction structural mutant test failed with exit code $LASTEXITCODE"
    }
    Write-Output "AUCTION_STRUCTURAL_MUTANT_OK"
}
finally {
    Pop-Location
}
