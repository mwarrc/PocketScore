# ============================================================
#  MENU
# ============================================================
function Show-Menu {
    Clear-Host
    Write-Host ""
    Write-Host "  +------------------------------------------------+" -ForegroundColor DarkCyan
    Write-Host "  |       PocketScore Dev Toolkit  (PS1)           |" -ForegroundColor Cyan
    Write-Host "  +------------------------------------------------+" -ForegroundColor DarkCyan
    $adbShort = if ($ADB.Length -gt 44) { "..." + $ADB.Substring($ADB.Length - 41) } else { $ADB }
    Write-Host "  | ADB: $($adbShort.PadRight(43))|" -ForegroundColor Gray
    Write-Host "  +------------------------------------------------+" -ForegroundColor DarkCyan
    Write-Host "  | BUILD                                          |" -ForegroundColor DarkCyan
    Write-Host "  |  1.  Install Debug          (Gradle)           |" -ForegroundColor Green
    Write-Host "  |  2.  Build Debug   + ADB Install               |" -ForegroundColor Gray
    Write-Host "  |  3.  Build Debug            (no install)       |" -ForegroundColor Gray
    Write-Host "  |  4.  Build Release + Sign   (no install)       |" -ForegroundColor Gray
    Write-Host "  |  5.  Build Release + Sign   + ADB Install      |" -ForegroundColor Green
    Write-Host "  |  6.  Clean + Build Debug                       |" -ForegroundColor Gray
    Write-Host "  |  7.  Clean Project                             |" -ForegroundColor Gray
    Write-Host "  +------------------------------------------------+" -ForegroundColor DarkCyan
    Write-Host "  | DEVICE / ADB                                   |" -ForegroundColor DarkCyan
    Write-Host "  |  8.  List connected devices                    |" -ForegroundColor Gray
    Write-Host "  |  9.  Launch app on device                      |" -ForegroundColor Gray
    Write-Host "  | 10.  Force-stop app                            |" -ForegroundColor Gray
    Write-Host "  | 11.  Clear app data on device                  |" -ForegroundColor Gray
    Write-Host "  | 12.  Uninstall from device                     |" -ForegroundColor Gray
    Write-Host "  +------------------------------------------------+" -ForegroundColor DarkCyan
    Write-Host "  | LOGS                                           |" -ForegroundColor DarkCyan
    Write-Host "  | 13.  Live logcat  (all)                        |" -ForegroundColor Gray
    Write-Host "  | 14.  Live logcat  (app only > .log)            |" -ForegroundColor Gray
    Write-Host "  | 15.  Live logcat  (all > .log)                 |" -ForegroundColor Gray
    Write-Host "  | 16.  Clear logcat buffer                       |" -ForegroundColor Gray
    Write-Host "  +------------------------------------------------+" -ForegroundColor DarkCyan
    Write-Host "  | UTILS                                          |" -ForegroundColor DarkCyan
    Write-Host "  | 17.  Screenshot -> PC                          |" -ForegroundColor Gray
    Write-Host "  | 18.  Pull APK from device                      |" -ForegroundColor Gray
    Write-Host "  | 19.  Show app version info                     |" -ForegroundColor Gray
    Write-Host "  | 20.  Open logs folder                          |" -ForegroundColor Gray
    Write-Host "  | 21.  Keystore info / regenerate                |" -ForegroundColor Gray
    Write-Host "  |  0.  Exit                                      |" -ForegroundColor DarkGray
    Write-Host "  +------------------------------------------------+" -ForegroundColor DarkCyan
    Write-Host ""
}

# ============================================================
#  DISPATCH
# ============================================================
$DISPATCH = @{
    "1"                     = "cmd-install_debug"
    "2"                     = "cmd-build_debug_adb"
    "3"                     = "cmd-build_debug"
    "4"                     = "cmd-build_release"
    "5"                     = "cmd-build_release_install"
    "6"                     = "cmd-clean_build_debug"
    "7"                     = "cmd-clean"
    "8"                     = "cmd-list_devices"
    "9"                     = "cmd-launch_app"
    "10"                    = "cmd-stop_app"
    "11"                    = "cmd-clear_data"
    "12"                    = "cmd-uninstall"
    "13"                    = "cmd-logcat_all"
    "14"                    = "cmd-logcat_app"
    "15"                    = "cmd-dump_logcat"
    "16"                    = "cmd-clear_logcat"
    "17"                    = "cmd-screenshot"
    "18"                    = "cmd-pull_apk"
    "19"                    = "cmd-version_info"
    "20"                    = "cmd-open_logs"
    "21"                    = "cmd-keystore_info"
    # named aliases for CLI use
    "install_debug"         = "cmd-install_debug"
    "build_debug"           = "cmd-build_debug"
    "build_debug_adb"       = "cmd-build_debug_adb"
    "build_release"         = "cmd-build_release"
    "build_release_install" = "cmd-build_release_install"
    "clean"                 = "cmd-clean"
    "clean_build"           = "cmd-clean_build_debug"
    "logcat"                = "cmd-logcat_app"
    "logcat_all"            = "cmd-logcat_all"
    "screenshot"            = "cmd-screenshot"
    "version"               = "cmd-version_info"
    "devices"               = "cmd-list_devices"
    "launch"                = "cmd-launch_app"
    "stop"                  = "cmd-stop_app"
}

# -- Quick-pause: auto-continues after N seconds or on Enter key ----------
function Pause-Brief ([int]$Sec = 3) {
    $sw = [Diagnostics.Stopwatch]::StartNew()
    while ($sw.Elapsed.TotalSeconds -lt $Sec) {
        $remaining = [Math]::Ceiling($Sec - $sw.Elapsed.TotalSeconds)
        Write-Host "`r  [returning to menu in ${remaining}s -- press Enter to skip] " -NoNewline -ForegroundColor DarkGray
        if ([Console]::KeyAvailable) {
            [Console]::ReadKey($true) | Out-Null
            break
        }
        Start-Sleep -Milliseconds 150
    }
    Write-Host "`r$(' ' * 55)`r" -NoNewline
}
