<#
.SYNOPSIS
Captures screenshots from a connected ADB device interactively.

.DESCRIPTION
This script provides an interactive loop that allows quickly taking screenshots from
a connected Android device. Press Enter to grab the next screenshot in sequence
or specify a specific number to replace an older screenshot.
#>

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$screenshotsDir = Join-Path $scriptDir "screenshots"

if (-not (Test-Path $screenshotsDir)) {
    New-Item -ItemType Directory -Path $screenshotsDir | Out-Null
}

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "        Interactive Screenshot Tool       " -ForegroundColor White
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Instructions:"
Write-Host "- Navigate on your phone, then press " -NoNewline; Write-Host "[Enter]" -ForegroundColor Yellow -NoNewline; Write-Host " to capture."
Write-Host "- To replace an existing screenshot, type its " -NoNewline; Write-Host "[Number]" -ForegroundColor Yellow -NoNewline; Write-Host " (e.g. '5') and press Enter."
Write-Host "- Type " -NoNewline; Write-Host "'c'" -ForegroundColor Magenta -NoNewline; Write-Host " to toggle " -NoNewline; Write-Host "continuous mode" -ForegroundColor Magenta -NoNewline; Write-Host " (just keep pressing Enter non-stop)."
Write-Host "- Type " -NoNewline; Write-Host "'q'" -ForegroundColor Yellow -NoNewline; Write-Host " to quit.`n"

# Resolve ADB path - tries project-local, then Android Studio's SDK location,
# then the standard SDK location, then falls back to whatever is on PATH.
$adbPath = "adb"
$localAppData = [Environment]::GetFolderPath('LocalApplicationData')
$projectRoot = Split-Path -Parent $scriptDir

$pathsToTry = @(
    (Join-Path $projectRoot "platform-tools\adb.exe"),
    (Join-Path $localAppData "Android\Sdk\platform-tools\adb.exe"),
    (Join-Path $localAppData "Programs\Android Studio\plugins\android\resources\installer\win\x86_64\adb.exe"),
    (Join-Path $env:APPDATA "..\Local\Android\sdk\platform-tools\adb.exe")
)

foreach ($p in $pathsToTry) {
    if (Test-Path $p) {
        $adbPath = $p
        Write-Host "Using ADB: $adbPath" -ForegroundColor DarkGray
        break
    }
}

# Verify ADB connection
$devices = & $adbPath devices 2>&1
if (-not ($devices -match "device\b")) {
    Write-Host "Warning: No ADB devices detected. Please connect a device or start an emulator." -ForegroundColor Red
}

$tempDevicePath = "/sdcard/temp_capture_ps.png"
$continuousMode = $false

while ($true) {
    # Re-evaluate existing files each iteration so count stays accurate after manual deletes
    $existingFiles = @(Get-ChildItem -Path $screenshotsDir -Filter "screen_*.jpg" |
    Where-Object { $_.Name -match '^screen_\d+\.jpg$' } |
    Select-Object Name, @{Name = "Index"; Expression = { [int]($_.Name -replace '^screen_', '' -replace '\.jpg', '') } } |
    Sort-Object Index)

    $nextIndex = 1
    if ($existingFiles.Count -gt 0) {
        $nextIndex = $existingFiles[-1].Index + 1
    }

    if ($continuousMode) {
        $modeTag = " [CONTINUOUS]"
        Write-Host "screen_$($nextIndex.ToString('00')).jpg$modeTag - press Enter to capture, type to exit continuous: " -NoNewline -ForegroundColor Magenta
    }
    else {
        Write-Host "Next -> [screen_$($nextIndex.ToString('00')).jpg] (Enter/Number/c/q): " -NoNewline -ForegroundColor Green
    }

    $userInput = Read-Host

    if ($userInput -eq 'q' -or $userInput -eq 'quit' -or $userInput -eq 'exit') {
        Write-Host "Exiting..." -ForegroundColor DarkGray
        break
    }

    # Toggle continuous mode
    if ($userInput -eq 'c') {
        $continuousMode = -not $continuousMode
        if ($continuousMode) {
            Write-Host " >> Continuous mode ON - just keep pressing Enter.`n" -ForegroundColor Magenta
        }
        else {
            Write-Host " >> Continuous mode OFF.`n" -ForegroundColor DarkGray
        }
        continue
    }

    # In continuous mode, any non-empty input that isn't a number exits continuous mode first
    if ($continuousMode -and -not [string]::IsNullOrWhiteSpace($userInput)) {
        if ($userInput -notmatch '^\d+$') {
            $continuousMode = $false
            Write-Host " >> Continuous mode OFF.`n" -ForegroundColor DarkGray
            continue
        }
    }

    $targetIndex = $nextIndex
    if (-not [string]::IsNullOrWhiteSpace($userInput)) {
        if ($userInput -match '^\d+$') {
            $targetIndex = [int]$userInput
        }
        else {
            Write-Host "Invalid input. Please press Enter, type a number, 'c' for continuous, or 'q'." -ForegroundColor Red
            continue
        }
    }

    $filename = "screen_$($targetIndex.ToString('00')).jpg"
    $destPath = Join-Path $screenshotsDir $filename

    if (Test-Path $destPath) {
        Write-Host " Overwriting $filename..." -ForegroundColor Yellow
    }
    else {
        Write-Host " Capturing $filename..." -ForegroundColor Cyan
    }

    # Capture on device
    $adbOut = & $adbPath shell screencap -p $tempDevicePath 2>&1

    if ($LASTEXITCODE -ne 0) {
        Write-Host " [X] Failed to capture on device. Is it connected and awake?" -ForegroundColor Red
        Write-Host "     ADB Output: $adbOut" -ForegroundColor DarkGray
        continue
    }

    # Pull to PC then clean up temp file on device
    & $adbPath pull $tempDevicePath $destPath 2>&1 | Out-Null
    & $adbPath shell rm $tempDevicePath 2>&1 | Out-Null

    if (Test-Path $destPath) {
        Write-Host " [OK] Saved to $filename`n" -ForegroundColor Green
    }
    else {
        Write-Host " [X] Failed to pull $filename to PC`n" -ForegroundColor Red
    }
} 