[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$reportDirectory = Join-Path $repoRoot 'use-bdi-plugin\target\case-study\auction'
$jsonReport = Join-Path $reportDirectory 'auction-baseline.json'
$htmlReport = Join-Path $reportDirectory 'auction-baseline.html'

Push-Location $repoRoot
try {
    & mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin `
        -Dtest=AuctionBaselineReportTest test
    if ($LASTEXITCODE -ne 0) {
        throw "Auction baseline report failed with exit code $LASTEXITCODE."
    }
    if (-not (Test-Path -LiteralPath $jsonReport)) {
        throw "Auction JSON baseline report was not created: $jsonReport"
    }
    if (-not (Test-Path -LiteralPath $htmlReport)) {
        throw "Auction HTML baseline report was not created: $htmlReport"
    }
    Write-Output 'AUCTION_BASELINE_REPORT_OK'
    Get-Content -LiteralPath $jsonReport -Raw
}
finally {
    Pop-Location
}
