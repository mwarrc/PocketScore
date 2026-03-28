# ============================================================
#  Log Commands
# ============================================================

function cmd-logcat_all {
    HDR "Live Logcat (all)  -- Ctrl+C to stop"
    if (-not (Assert-Device)) { return }
    & $ADB logcat -v time
}

function cmd-logcat_app {
    HDR "Live Logcat (app only)  -- Ctrl+C to stop"
    if (-not (Assert-Device)) { return }
    $log = Join-Path $LOGS_DIR "logcat_app_$(Get-TS).log"
    INF "Exporting live log to: $log"
    
    $pidStr = (& $ADB shell pidof -s $PACKAGE 2>$null) -join ""
    $pidStr = $pidStr.Trim()
    if ($pidStr -match "^\d+$") {
        INF "Attaching to PID $pidStr"
        & $ADB logcat --pid=$pidStr -v time | Tee-Object -FilePath $log
    }
    else {
        WRN "App not running. Falling back to tag filter."
        & $ADB logcat -v time PocketScore:V AndroidRuntime:E "*:S" | Tee-Object -FilePath $log
    }
}

function cmd-dump_logcat {
    HDR "Live Logcat (all to file) -- Ctrl+C to stop"
    if (-not (Assert-Device)) { return }
    $log = Join-Path $LOGS_DIR "logcat_$(Get-TS).log"
    INF "Exporting live log to: $log"
    & $ADB logcat -v time | Tee-Object -FilePath $log
}

function cmd-clear_logcat {
    HDR "Clear Logcat Buffer"
    if (-not (Assert-Device)) { return }
    & $ADB logcat -c
    OK "Buffer cleared."
}
