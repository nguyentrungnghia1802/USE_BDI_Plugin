[CmdletBinding()]
param(
    [switch]$KeepClone
)

$ErrorActionPreference = 'Stop'

function Invoke-Checked {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Command,
        [Parameter(Mandatory = $false)]
        [string[]]$Arguments = @()
    )

    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Command failed with exit code $LASTEXITCODE."
    }
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$head = (& git -C $repoRoot rev-parse --verify HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($head)) {
    throw "Could not resolve the repository HEAD: $repoRoot"
}

$cloneRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("use-bdi-clean-clone-" + [guid]::NewGuid())
$cloneCreated = $false

try {
    Invoke-Checked git @('clone', '--no-local', '--no-checkout', '--quiet', $repoRoot, $cloneRoot)
    $cloneCreated = $true
    Invoke-Checked git @('-C', $cloneRoot, 'checkout', '--quiet', '--detach', $head)

    $cloneHead = (& git -C $cloneRoot rev-parse --verify HEAD).Trim()
    if ($LASTEXITCODE -ne 0 -or $cloneHead -ne $head) {
        throw "Clean clone is not at the requested HEAD. Expected $head, got $cloneHead."
    }

    $initialStatus = @(& git -C $cloneRoot status --porcelain)
    if ($LASTEXITCODE -ne 0) {
        throw 'Could not inspect clean-clone status.'
    }
    if ($initialStatus.Count -ne 0) {
        throw "Clean clone is not clean before build: $($initialStatus -join '; ')"
    }

    Push-Location $cloneRoot
    try {
        Invoke-Checked mvn @('--batch-mode', '--no-transfer-progress', '-pl', 'use-assembly', '-am', 'package')
    }
    finally {
        Pop-Location
    }

    $pluginJar = Join-Path $cloneRoot 'use-bdi-plugin\target\use-bdi-plugin-7.1.1.jar'
    $distributionZip = Join-Path $cloneRoot 'use-assembly\target\use-7.1.1.zip'
    if (-not (Test-Path -LiteralPath $pluginJar)) {
        throw "Plugin artifact is missing from the clean clone: $pluginJar"
    }
    if (-not (Test-Path -LiteralPath $distributionZip)) {
        throw "USE distribution is missing from the clean clone: $distributionZip"
    }

    $pluginEntries = @(jar tf $pluginJar)
    $hasPluginClass = $pluginEntries -contains 'org/tzi/use/plugins/bdi/BdiPlugin.class'
    $hasJasonClass = $pluginEntries -contains 'jason/asSemantics/Agent.class'
    $hasJaCaMoParser = $pluginEntries -contains 'jacamo/project/parser/JaCaMoProjectParser.class'
    $hasJaCaMoRuntime = $pluginEntries -contains 'cartago/CartagoService.class'
    $hasThirdPartyNotices = $pluginEntries -contains 'META-INF/THIRD-PARTY-NOTICES.txt'
    if ($LASTEXITCODE -ne 0 -or -not $hasPluginClass -or -not $hasJasonClass -or
            -not $hasJaCaMoParser -or $hasJaCaMoRuntime -or -not $hasThirdPartyNotices) {
        throw 'Clean-clone plugin JAR is not the expected shaded artifact.'
    }

    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [System.IO.Compression.ZipFile]::OpenRead($distributionZip)
    try {
        $pluginEntry = $archive.Entries |
            Where-Object { $_.FullName -eq 'use-7.1.1/lib/plugins/use-bdi-plugin-7.1.1.jar' } |
            Select-Object -First 1
    }
    finally {
        $archive.Dispose()
    }
    if ($null -eq $pluginEntry) {
        throw 'Clean-clone distribution does not contain the BDI plugin JAR.'
    }

    $finalStatus = @(& git -C $cloneRoot status --porcelain)
    if ($LASTEXITCODE -ne 0 -or $finalStatus.Count -ne 0) {
        throw "Clean clone is not clean after build: $($finalStatus -join '; ')"
    }

    Write-Host "CLEAN_CLONE_REPRODUCIBILITY_OK: commit=$cloneHead plugin=shaded distribution=use-7.1.1.zip"
}
finally {
    if ($KeepClone) {
        Write-Host "Keeping clean clone for inspection: $cloneRoot"
    }
    elseif ($cloneCreated -and (Test-Path -LiteralPath $cloneRoot)) {
        $separator = [System.IO.Path]::DirectorySeparatorChar
        $tempRoot = [System.IO.Path]::GetFullPath([System.IO.Path]::GetTempPath()).TrimEnd($separator) + $separator
        $resolvedCloneRoot = [System.IO.Path]::GetFullPath($cloneRoot)
        $safeName = (Split-Path -Leaf $resolvedCloneRoot).StartsWith(
            'use-bdi-clean-clone-', [System.StringComparison]::OrdinalIgnoreCase)
        $insideTemp = $resolvedCloneRoot.StartsWith($tempRoot, [System.StringComparison]::OrdinalIgnoreCase)
        if ($safeName -and $insideTemp) {
            for ($attempt = 1; $attempt -le 10; $attempt++) {
                try {
                    Remove-Item -LiteralPath $resolvedCloneRoot -Recurse -Force -ErrorAction Stop
                    break
                }
                catch {
                    if ($attempt -eq 10) {
                        Write-Warning "Could not remove clean clone: $resolvedCloneRoot"
                    }
                    else {
                        Start-Sleep -Milliseconds 500
                    }
                }
            }
        }
        else {
            Write-Warning "Refusing to remove unexpected clean-clone path: $resolvedCloneRoot"
        }
    }
}
