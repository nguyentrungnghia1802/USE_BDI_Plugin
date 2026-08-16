[CmdletBinding()]
param(
    [string]$Output = 'target\release-evidence\release-evidence-manifest.json'
)

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$outputPath = if ([System.IO.Path]::IsPathRooted($Output)) {
    [System.IO.Path]::GetFullPath($Output)
} else {
    [System.IO.Path]::GetFullPath((Join-Path $repoRoot $Output))
}
$outputDirectory = Split-Path -Parent $outputPath
New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null

$head = (& git -C $repoRoot rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($head)) {
    throw 'Could not resolve repository HEAD.'
}

$sourceArchive = Join-Path $outputDirectory "use-thesis-$head.zip"
& git -C $repoRoot archive --format=zip "--prefix=use-thesis-$head/" HEAD "--output=$sourceArchive"
if ($LASTEXITCODE -ne 0) {
    throw 'Could not create the committed source archive.'
}

$required = [System.Collections.Generic.List[string]]::new()
foreach ($path in @(
    'use-assembly\target\use-7.1.1.zip',
    'use-assembly\target\use-7.1.1.tar.gz',
    'use-bdi-plugin\target\use-bdi-plugin-7.1.1.jar',
    'docs\project\metamodel\use-jacamo-analysis.ecore',
    'docs\project\evidence\bdi-metamodel-diagram.mmd',
    'docs\project\evidence\ir-class-diagram.mmd',
    'docs\project\evidence\auction-architecture.mmd',
    'docs\project\evidence\traceability-diagram.mmd',
    'docs\project\metamodel\correspondence-diagram.mmd',
    'docs\project\evidence\auction-evaluation-manifest.json',
    'docs\project\evidence\auction-evaluation-run\evaluation-results.json',
    'docs\project\evidence\auction-evaluation-run\evaluation-results.csv',
    'docs\project\evidence\auction-evaluation-run\evaluation-results.html',
    'docs\project\evidence\diagram-performance.md',
    'docs\project\THIRD_PARTY_NOTICES.md',
    'target\release-evidence\current-analysis.json',
    'target\release-evidence\auction-current-view.svg'
)) {
    $required.Add($path)
}

foreach ($path in Get-ChildItem -LiteralPath (Join-Path $repoRoot 'docs\project') -File |
        Where-Object { $_.Name -in @(
            'README.md', '00_PROJECT_CONTEXT.md', '01_PRODUCT_REQUIREMENTS.md',
            '04_SYSTEM_ARCHITECTURE.md', '08_CONSISTENCY_RULE_CATALOG.md',
            '10_PLUGIN_TECHNICAL_DESIGN.md', '12_REQUIREMENT_TRACEABILITY.md',
            '16_PROJECT_COMPLETION_CHECKLIST.md', 'DECISION_LOG.md',
            'DEVELOPER_GUIDE.md', 'USER_GUIDE.md') }) {
    $required.Add([System.IO.Path]::GetRelativePath($repoRoot, $path.FullName))
}
foreach ($path in Get-ChildItem -LiteralPath (Join-Path $repoRoot 'docs\report\images') -Filter 'release_*.png' -File) {
    $required.Add([System.IO.Path]::GetRelativePath($repoRoot, $path.FullName))
}

$artifacts = [System.Collections.Generic.List[object]]::new()
$artifacts.Add([ordered]@{
    kind = 'source-archive'
    path = [System.IO.Path]::GetRelativePath($repoRoot, $sourceArchive).Replace('\', '/')
    sha256 = (Get-FileHash -LiteralPath $sourceArchive -Algorithm SHA256).Hash.ToLowerInvariant()
    bytes = (Get-Item -LiteralPath $sourceArchive).Length
})
foreach ($relative in $required | Sort-Object -Unique) {
    $absolute = Join-Path $repoRoot $relative
    if (-not (Test-Path -LiteralPath $absolute -PathType Leaf)) {
        throw "Required release artifact is missing: $relative"
    }
    $item = Get-Item -LiteralPath $absolute
    $artifacts.Add([ordered]@{
        kind = if ($relative -like 'docs\report\images\release_*.png') { 'screenshot' } else { 'release-artifact' }
        path = $relative.Replace('\', '/')
        sha256 = (Get-FileHash -LiteralPath $absolute -Algorithm SHA256).Hash.ToLowerInvariant()
        bytes = $item.Length
    })
}

$javaExecutable = if (-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
    Join-Path $env:JAVA_HOME 'bin\java.exe'
} else {
    (Get-Command java -ErrorAction Stop).Source
}
$javaVersion = (& $javaExecutable -version 2>&1 | Select-Object -First 1).ToString()
$mavenVersion = (& mvn --version | Select-Object -First 1).ToString()
$dependencyTree = & mvn --batch-mode --no-transfer-progress -pl use-bdi-plugin dependency:tree
if ($LASTEXITCODE -ne 0) {
    throw 'Could not inspect the plugin dependency tree.'
}
$unexpectedRuntime = @($dependencyTree | Select-String -Pattern 'org\.eclipse\.emf|org\.eclipse\.sirius')
if ($unexpectedRuntime.Count -ne 0) {
    throw "Unexpected EMF/Sirius runtime dependency: $($unexpectedRuntime -join '; ')"
}

$manifest = [ordered]@{
    schemaVersion = '1.0.0'
    generatedAtUtc = (Get-Date).ToUniversalTime().ToString('o')
    commit = $head
    releaseTag = $null
    releaseOwnerApproval = $false
    tools = [ordered]@{
        os = [System.Environment]::OSVersion.VersionString
        java = $javaVersion
        maven = $mavenVersion
        use = '7.1.1'
        plugin = '0.1.0'
        analysisProfile = '1.0.0'
        jason = '3.3.0'
        jacamo = '1.3.0'
        cartago = '3.1'
        moise = '1.1'
    }
    artifacts = @($artifacts)
    externalArtifacts = @()
    missingExternalKinds = @('data', 'slides')
    backupGate = 'BLOCKED_EXTERNAL'
}
$json = $manifest | ConvertTo-Json -Depth 8
[System.IO.File]::WriteAllText($outputPath, $json, [System.Text.UTF8Encoding]::new($false))
$manifestHash = (Get-FileHash -LiteralPath $outputPath -Algorithm SHA256).Hash.ToLowerInvariant()
Write-Host "Manifest: $outputPath"
Write-Host "Manifest SHA-256: $manifestHash"
Write-Host "RELEASE_EVIDENCE_MANIFEST_OK: artifacts=$($artifacts.Count) backup=BLOCKED_EXTERNAL tag=NOT_CREATED"
