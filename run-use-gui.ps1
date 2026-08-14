[CmdletBinding()]
param(
    [ValidateSet('auction', 'family-person', 'smart-home', 'smart-queue')]
    [string]$Demo,

    [string]$Specification,

    [string]$CommandFile,

    [switch]$Rebuild,

    [switch]$Headless,

    [switch]$ValidateOnly
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$repo = $PSScriptRoot
$assemblyTarget = Join-Path $repo 'use-assembly\target'
$distributionZip = Join-Path $assemblyTarget 'use-7.1.1.zip'
$runtimeRoot = Join-Path $assemblyTarget 'demo-runtime'

function Invoke-DistributionBuild {
    Write-Host '[INFO] Building the packaged USE distribution...' -ForegroundColor Yellow
    & mvn --batch-mode --no-transfer-progress -pl use-assembly -am `
        '-Dmaven.test.skip=true' package
    if ($LASTEXITCODE -ne 0) {
        throw "Maven failed to build use-assembly (exit code $LASTEXITCODE)."
    }
}

$runtimeMarker = Join-Path $runtimeRoot '.distribution-source'
$runtimeLock = [System.Threading.Mutex]::new($false, 'Local\USE_BDI_GUI_RUNTIME')
$lockAcquired = $false
try {
    try {
        $lockAcquired = $runtimeLock.WaitOne([TimeSpan]::FromMinutes(5))
    } catch [System.Threading.AbandonedMutexException] {
        $lockAcquired = $true
    }
    if (-not $lockAcquired) {
        throw 'Timed out while another USE launcher was preparing the runtime.'
    }

    if ($Rebuild -or -not (Test-Path -LiteralPath $distributionZip -PathType Leaf)) {
        Invoke-DistributionBuild
    }

    $zipInfo = Get-Item -LiteralPath $distributionZip
    $distributionIdentity = "$($zipInfo.Length)|$($zipInfo.LastWriteTimeUtc.Ticks)"
    $useHome = Get-ChildItem -LiteralPath $runtimeRoot -Directory -ErrorAction SilentlyContinue |
        Where-Object {
            (Test-Path -LiteralPath (Join-Path $_.FullName 'lib\use-gui.jar') -PathType Leaf) -and
            (Test-Path -LiteralPath (Join-Path $_.FullName 'oclextensions') -PathType Container)
        } |
        Select-Object -First 1

    $extractedIdentity = if (Test-Path -LiteralPath $runtimeMarker -PathType Leaf) {
        (Get-Content -Raw -LiteralPath $runtimeMarker).Trim()
    } else {
        $null
    }
    $mustExtract = $null -eq $useHome -or $distributionIdentity -ne $extractedIdentity

    if ($mustExtract) {
        Write-Host '[INFO] Extracting the packaged USE distribution...' -ForegroundColor Yellow
        New-Item -ItemType Directory -Force -Path $runtimeRoot | Out-Null
        Expand-Archive -LiteralPath $distributionZip -DestinationPath $runtimeRoot -Force
        Set-Content -LiteralPath $runtimeMarker -Value $distributionIdentity -NoNewline
        $useHome = Get-ChildItem -LiteralPath $runtimeRoot -Directory |
            Where-Object {
                (Test-Path -LiteralPath (Join-Path $_.FullName 'lib\use-gui.jar') -PathType Leaf) -and
                (Test-Path -LiteralPath (Join-Path $_.FullName 'oclextensions') -PathType Container)
            } |
            Select-Object -First 1
    }
} finally {
    if ($lockAcquired) {
        $runtimeLock.ReleaseMutex()
    }
    $runtimeLock.Dispose()
}

if ($null -eq $useHome) {
    throw "No extracted USE distribution was found under '$runtimeRoot'."
}
$useHome = $useHome.FullName

$javaExecutable = if ($env:JAVA_HOME) {
    Join-Path $env:JAVA_HOME 'bin\java.exe'
} else {
    (Get-Command java -ErrorAction Stop).Source
}
if (-not (Test-Path -LiteralPath $javaExecutable -PathType Leaf)) {
    throw "Java executable not found at '$javaExecutable'. Set JAVA_HOME to a Java 21 JDK."
}
$previousErrorActionPreference = $ErrorActionPreference
try {
    # java -version writes its successful output to stderr.
    $ErrorActionPreference = 'Continue'
    $javaVersionOutput = (& $javaExecutable -version 2>&1 | Out-String)
} finally {
    $ErrorActionPreference = $previousErrorActionPreference
}
if ($LASTEXITCODE -ne 0 -or $javaVersionOutput -notmatch 'version "(?<major>\d+)') {
    throw "Could not determine the Java version from '$javaExecutable'."
}
if ([int]$Matches.major -lt 21) {
    throw "USE BDI Plugin requires Java 21; found Java $($Matches.major) at '$javaExecutable'."
}

$guiJar = Join-Path $useHome 'lib\use-gui.jar'
$pluginJar = Join-Path $useHome 'lib\plugins\use-bdi-plugin-7.1.1.jar'
if (-not (Test-Path -LiteralPath $pluginJar -PathType Leaf)) {
    throw "BDI plugin not found at '$pluginJar'. Run this script with -Rebuild."
}

$modeArgument = if ($Headless) { '-q' } else { '-nr' }
$arguments = @('-jar', $guiJar, $modeArgument, "-H=$useHome")
$model = $null
$snapshot = $null
if ($Demo -and ($Specification -or $CommandFile)) {
    throw 'Use either -Demo or -Specification/-CommandFile, not both.'
}
if ($CommandFile -and -not $Specification) {
    throw '-CommandFile requires -Specification so USE can load a model first.'
}
if ($Headless -and -not ($Demo -or ($Specification -and $CommandFile))) {
    throw '-Headless requires -Demo or both -Specification and -CommandFile.'
}
if ($Demo) {
    $demoFiles = @{
        'auction'       = @('Auction.use', 'Auction.cmd')
        'family-person' = @('Family.use', 'Family.cmd')
        'smart-home'    = @('SmartHome.use', 'SmartHome.cmd')
        'smart-queue'   = @('SmartQueue.use', 'SmartQueue.cmd')
    }
    $demoRoot = Join-Path $repo "use-bdi-plugin\demo\$Demo"
    $model = Join-Path $demoRoot $demoFiles[$Demo][0]
    $snapshot = Join-Path $demoRoot $demoFiles[$Demo][1]
    foreach ($file in @($model, $snapshot)) {
        if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
            throw "Demo input not found: '$file'."
        }
    }
    $arguments += @($model, $snapshot)
} elseif ($Specification) {
    $model = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($Specification)
    if (-not (Test-Path -LiteralPath $model -PathType Leaf)) {
        throw "Specification not found: '$model'."
    }
    $arguments += $model
    if ($CommandFile) {
        $snapshot = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($CommandFile)
        if (-not (Test-Path -LiteralPath $snapshot -PathType Leaf)) {
            throw "Command file not found: '$snapshot'."
        }
        $arguments += $snapshot
    }
}

Write-Host "[OK] Java:    $javaExecutable" -ForegroundColor Green
Write-Host "[OK] USE_HOME: $useHome" -ForegroundColor Green
Write-Host "[OK] Plugin:  $pluginJar" -ForegroundColor Green
if ($model) {
    Write-Host "[OK] Model:   $model" -ForegroundColor Green
    if ($snapshot) {
        Write-Host "[OK] State:   $snapshot" -ForegroundColor Green
    }
} else {
    Write-Host '[INFO] Starting with no model. Use File > Open specification...' -ForegroundColor Cyan
}

if ($ValidateOnly) {
    Write-Host 'USE_GUI_LAUNCHER_OK' -ForegroundColor Green
    return
}

Set-Location -LiteralPath $repo
& $javaExecutable @arguments
if ($LASTEXITCODE -ne 0) {
    throw "USE exited with code $LASTEXITCODE."
}
