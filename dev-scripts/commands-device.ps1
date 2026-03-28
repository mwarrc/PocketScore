# ============================================================
#  Device Commands
# ============================================================

function cmd-list_devices {
    HDR "Connected ADB Devices"
    & $ADB devices -l
}

function cmd-launch_app {
    HDR "Launch App"
    if (-not (Assert-Device)) { return }
    $prev = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $r = (& $ADB shell am start -n "$PACKAGE/$ACTIVITY" 2>&1) -join "`n"
    $ErrorActionPreference = $prev
    
    if ($r -match "Error type 3|does not exist") {
        ERR "Launch failed: The application is not installed on the device."
        INF "Hint: Use a 'Build + Install' command first to deploy the app."
    } elseif ($r -match "Error") {
        ERR "Launch failed: $r"
    } else {
        OK "App launched."
    }
}

function cmd-stop_app {
    HDR "Force-Stop App"
    if (-not (Assert-Device)) { return }
    $prev = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $r = (& $ADB shell am force-stop $PACKAGE 2>&1) -join "`n"
    $ErrorActionPreference = $prev
    
    if ($r -match "unknown package|not found") {
        WRN "App was not running or is not installed."
    } else {
        OK "Force-stopped."
    }
}

function cmd-clear_data {
    HDR "Clear App Data"
    if (-not (Assert-Device)) { return }
    WRN "Confirm: This will wipe all app data on the device."
    $ans = Read-Host "  Type YES to confirm"
    if ($ans -eq "YES") {
        $prev = $ErrorActionPreference
        $ErrorActionPreference = "Continue"
        $r = (& $ADB shell pm clear $PACKAGE 2>&1) -join "`n"
        $ErrorActionPreference = $prev
        if ($r -match "Success") {
            OK "App data cleared."
        } elseif ($r -match "Failed") {
            ERR "Clear failed: App is likely not installed."
        } else {
            ERR "Clear result: $r"
        }
    }
    else { INF "Cancelled." }
}

function cmd-uninstall {
    HDR "Uninstall App"
    if (-not (Assert-Device)) { return }
    WRN "Confirm: This will fully uninstall the app."
    $ans = Read-Host "  Type YES to confirm"
    if ($ans -eq "YES") {
        $prev = $ErrorActionPreference
        $ErrorActionPreference = "Continue"
        $r = (& $ADB uninstall $PACKAGE 2>&1) -join "`n"
        $ErrorActionPreference = $prev
        if ($r -match "Success") {
            OK "Uninstalled."
        } elseif ($r -match "DELETE_FAILED_INTERNAL_ERROR|Unknown package|not installed") {
            ERR "Uninstall failed: App is not currently installed."
        } else {
            ERR "Uninstall result: $r"
        }
    }
    else { INF "Cancelled." }
}
