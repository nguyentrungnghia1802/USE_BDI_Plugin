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

Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [System.IO.Compression.ZipFile]::OpenRead($archivePath)
try {
    $restoreSpotCheck = $archive.Entries |
        Where-Object { $_.FullName -eq "${prefix}pom.xml" } |
        Select-Object -First 1
} finally {
    $archive.Dispose()
}
if ($null -eq $restoreSpotCheck) {
    throw 'Source archive restore spot-check could not find the root pom.xml.'
}

$copiedArtifacts = [System.Collections.Generic.List[object]]::new()
$missingRequiredKinds = [System.Collections.Generic.List[string]]::new()
$requirements = [ordered]@{
    data = @('data')
    reports = @('target\release-evidence', 'docs\report')
    slides = @('slides', 'presentation')
}

foreach ($kind in $requirements.Keys) {
    $source = $null
    foreach ($candidate in $requirements[$kind]) {
        $candidatePath = Join-Path $repoRoot $candidate
        if (Test-Path -LiteralPath $candidatePath) {
            $source = Get-Item -LiteralPath $candidatePath
            break
        }
    }
    if ($null -eq $source) {
        $missingRequiredKinds.Add($kind)
        continue
    }

    $copyTarget = Join-Path $destinationFull $kind
    Copy-Item -LiteralPath $source.FullName -Destination $copyTarget -Recurse -Force
    $files = if ($source.PSIsContainer) {
        Get-ChildItem -LiteralPath $copyTarget -Recurse -File
    } else {
        @(Get-Item -LiteralPath $copyTarget)
    }
    foreach ($file in $files) {
        $copiedArtifacts.Add([ordered]@{
            kind = $kind
            file = [System.IO.Path]::GetRelativePath($destinationFull, $file.FullName).Replace('\', '/')
            sha256 = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
            bytes = $file.Length
        })
    }
}

$trackedDocs = [ordered]@{
    source = $true
    report = Test-Path -LiteralPath (Join-Path $repoRoot 'docs\report') -PathType Container
    projectEvidence = Test-Path -LiteralPath (Join-Path $repoRoot 'docs\project\evidence') -PathType Container
    metamodel = Test-Path -LiteralPath (Join-Path $repoRoot 'docs\project\metamodel\use-jacamo-analysis.ecore') -PathType Leaf
    agentDocs = Test-Path -LiteralPath (Join-Path $repoRoot 'docs\agent') -PathType Container
}
$statusLines = @(& git -C $repoRoot status --porcelain)
$backupComplete = $missingRequiredKinds.Count -eq 0
$manifest = [ordered]@{
    schemaVersion = '0.2.0'
    generatedAtUtc = (Get-Date).ToUniversalTime().ToString('o')
    commit = $head
    sourceArchive = $archiveName
    sourceArchiveScope = 'committed HEAD only'
    sourceArchiveSha256 = (Get-FileHash -LiteralPath $archivePath -Algorithm SHA256).Hash.ToLowerInvariant()
    sourceArchiveReadable = $true
    restoreSpotCheck = "${prefix}pom.xml"
    committedDocumentation = $trackedDocs
    copiedArtifacts = @($copiedArtifacts)
    missingRequiredKinds = @($missingRequiredKinds)
    backupComplete = $backupComplete
    destinationExists = Test-Path -LiteralPath $destinationFull -PathType Container
    uncommittedStatusLines = @($statusLines)
}
$manifestPath = Join-Path $destinationFull 'thesis-backup-manifest.json'
$json = $manifest | ConvertTo-Json -Depth 8
[System.IO.File]::WriteAllText($manifestPath, $json, [System.Text.UTF8Encoding]::new($false))

Write-Host "Source archive: $archivePath"
Write-Host "Manifest: $manifestPath"
if ($backupComplete) {
    Write-Host 'THESIS_BACKUP_OK'
} else {
    Write-Host "THESIS_BACKUP_BLOCKED_EXTERNAL: missing=$($missingRequiredKinds -join ',')"
}
