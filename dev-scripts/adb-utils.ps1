# ============================================================
#  ADB Utilities
# ============================================================

# -- ADB auto-detect ------------------------------------------
function Find-ADB {
    $candidates = @(
        "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
        "C:\Android\platform-tools\adb.exe"
    )
    if ($env:ANDROID_HOME) {
        $candidates += "$env:ANDROID_HOME\platform-tools\adb.exe"
    }
    foreach ($c in $candidates) {
        if (Test-Path $c) { return $c }
    }
    $inPath = Get-Command adb -ErrorAction SilentlyContinue
    if ($inPath) { return $inPath.Source }
    WRN "adb not found. Device commands will fail."
    WRN "Set ANDROID_HOME or add platform-tools to PATH."
    return "adb"
}
$ADB = Find-ADB

# -- ADB device guard -----------------------------------------
function Assert-Device {
    # Run in a Continue scope so the ADB daemon startup message (stderr) doesn't
    # trigger $ErrorActionPreference = "Stop" and appear as a fatal error.
    $prev = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $raw = & $ADB devices 2>&1
    $ErrorActionPreference = $prev

    $devices = $raw | Where-Object { $_ -match "`t(device|emulator)" }
    if (-not $devices) {
        ERR "No device/emulator connected. Enable USB Debugging and plug in."
        return $false
    }
    return $true
}

# -- ADB install with conflict detection ----------------------
function Install-APK ([string]$Apk) {
    if (-not (Assert-Device)) { return }
    if (-not (Test-Path $Apk)) { ERR "APK not found: $Apk"; return }

    INF "Installing: $Apk"
    $prev = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $output = (& $ADB install -r $Apk 2>&1) -join "`n"
    $ErrorActionPreference = $prev
    Write-Host $output

    $conflict = $output -match "INSTALL_FAILED_UPDATE_INCOMPATIBLE|INSTALL_FAILED_VERSION_DOWNGRADE|signatures do not match"
    if ($conflict) {
        Write-Host ""
        WRN "Signature conflict detected: mismatch in signing keys."
        WRN "Existing install must be removed for this build."
        Write-Host ""
        INF "APK to install: $(Split-Path $Apk -Leaf)"
        $ans = Read-Host "  Uninstall old version and reinstall? (WIPES DATA) [y/N]"
        if ($ans -match "^[Yy]$") {
            INF "Uninstalling $PACKAGE ..."
            $prev = $ErrorActionPreference
            $ErrorActionPreference = "Continue"
            & $ADB uninstall $PACKAGE | Out-Null
            INF "Installing building ..."
            $output2 = (& $ADB install -r $Apk 2>&1) -join "`n"
            $ErrorActionPreference = $prev
            Write-Host $output2
            if ($output2 -match "Success") { OK "Installed successfully." }
            else { ERR "Install still failed after uninstall." }
        }
        else {
            INF "Install cancelled."
        }
        return
    }

    if ($output -match "Success") { OK "APK installed successfully." }
    else { ERR "Install failed." }
}
