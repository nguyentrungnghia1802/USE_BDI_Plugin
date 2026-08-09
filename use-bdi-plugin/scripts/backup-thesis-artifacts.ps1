[CmdletBinding()]
param(
    [string]$Destination
)

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path

if ([string]::IsNullOrWhiteSpace($Destination)) {
    $stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $Destination = Join-Path $repoRoot "target\backups\thesis-$stamp"
}

$destinationFull = [System.IO.Path]::GetFullPath($Destination)
New-Item -ItemType Directory -Path $destinationFull -Force | Out-Null

$head = (& git -C $repoRoot rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($head)) {
    throw 'Could not resolve the repository HEAD for the source archive.'
}

$archiveName = "use-thesis-$head.zip"
$archivePath = Join-Path $destinationFull $archiveName
$prefix = "use-thesis-$head/"
& git -C $repoRoot archive --format=zip "--prefix=$prefix" HEAD "--output=$archivePath"
if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $archivePath)) {
    throw "Could not create the source archive: $archivePath"
}

$copiedDirectories = [System.Collections.Generic.List[string]]::new()
$missingDirectories = [System.Collections.Generic.List[string]]::new()
foreach ($relativePath in @('data', 'slides', 'presentation')) {
    $sourcePath = Join-Path $repoRoot $relativePath
    if (Test-Path -LiteralPath $sourcePath -PathType Container) {
        $copyName = $relativePath -replace '[\\/]', '-'
        Copy-Item -LiteralPath $sourcePath -Destination (Join-Path $destinationFull $copyName) -Recurse -Force
        $copiedDirectories.Add($relativePath)
    } else {
        $missingDirectories.Add($relativePath)
    }
}

$trackedDocs = [ordered]@{
    source = $true
    report = Test-Path -LiteralPath (Join-Path $repoRoot 'docs\report') -PathType Container
    projectEvidence = Test-Path -LiteralPath (Join-Path $repoRoot 'docs\project\evidence') -PathType Container
    agentDocs = Test-Path -LiteralPath (Join-Path $repoRoot 'docs\agent') -PathType Container
}
$statusLines = @(& git -C $repoRoot status --porcelain)
$manifest = [ordered]@{
    schemaVersion = '0.1.0'
    generatedAtUtc = (Get-Date).ToUniversalTime().ToString('o')
    commit = $head
    sourceArchive = $archiveName
    sourceArchiveScope = 'committed HEAD only'
    committedDocumentation = $trackedDocs
    copiedExternalDirectories = @($copiedDirectories)
    missingExternalDirectories = @($missingDirectories)
    uncommittedStatusLines = @($statusLines)
}
$manifestPath = Join-Path $destinationFull 'thesis-backup-manifest.json'
$json = $manifest | ConvertTo-Json -Depth 6
[System.IO.File]::WriteAllText($manifestPath, $json, [System.Text.UTF8Encoding]::new($false))

Write-Host "Source archive: $archivePath"
Write-Host "Manifest: $manifestPath"
Write-Host 'THESIS_BACKUP_OK'
