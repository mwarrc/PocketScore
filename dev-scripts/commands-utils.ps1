# ============================================================
#  Util Commands
# ============================================================

function cmd-screenshot {
    HDR "Screenshot -> PC"
    if (-not (Assert-Device)) { return }
    $ts = Get-TS
    $remote = "/sdcard/ps_shot_$ts.png"
    $local = Join-Path $LOGS_DIR "screenshot_$ts.png"
    & $ADB shell screencap -p $remote
    & $ADB pull $remote $local | Out-Null
    & $ADB shell rm $remote   | Out-Null
    OK "Saved: $local"
    Start-Process $local
}

function cmd-pull_apk {
    HDR "Pull Installed APK from Device"
    if (-not (Assert-Device)) { return }
    $pathLine = (& $ADB shell pm path $PACKAGE 2>&1) -join ""
    $devicePath = ($pathLine -replace "package:", "").Trim()
    if (-not $devicePath) { ERR "Could not find installed APK path."; return }
    $out = Join-Path $LOGS_DIR "pocketscore_$(Get-TS).apk"
    INF "Device path: $devicePath"
    & $ADB pull $devicePath $out
    OK "Saved: $out"
}

function cmd-version_info {
    HDR "App Version Info on Device"
    if (-not (Assert-Device)) { return }
    & $ADB shell dumpsys package $PACKAGE |
    Select-String "versionName|versionCode|firstInstallTime|lastUpdateTime" |
    ForEach-Object { Write-Host "  $_" -ForegroundColor Cyan }
}

function cmd-open_logs {
    HDR "Opening Logs Folder"
    Start-Process explorer $LOGS_DIR
}

function cmd-keystore_info {
    HDR "Keystore Info"
    if (Test-Path $KS_PATH) {
        INF "Keystore: $KS_PATH"
        $keytool = Find-JavaTool "keytool"
        if ($keytool) {
            & $keytool -list -v -keystore $KS_PATH -storepass $KS_PASS 2>&1 |
            Where-Object { $_ -match "Alias|Valid|SHA|Owner|until" } |
            ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
        }
        Write-Host ""
        $regen = Read-Host "  Regenerate keystore? (loses existing signatures) [y/N]"
        if ($regen -match "^[Yy]$") {
            Remove-Item $KS_PATH -Force
            Ensure-Keystore | Out-Null
        }
    }
    else {
        WRN "No keystore at $KS_PATH"
        Ensure-Keystore | Out-Null
    }
}
