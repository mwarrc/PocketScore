# ============================================================
#  PocketScore Dev Toolkit  --  dev.ps1
#  Place in project root. Run with:  .\dev.ps1  or  .\dev.ps1 <command>
#  PowerShell 5.1+ required (ships with Windows 10/11)
# ============================================================
#Requires -Version 5.1
[CmdletBinding()] param([string]$Command = "")

# Load the modular core which will handle sourcing everything else
. (Join-Path $PSScriptRoot "dev-scripts\core.ps1")

# Handle CLI arguments or drop into interactive menu
if ($Command -and $DISPATCH.ContainsKey($Command)) {
    & $DISPATCH[$Command]
    exit
}
elseif ($Command -and $Command -ne "") {
    ERR "Unknown command: $Command"
    INF "Available: install_debug, build_debug, build_debug_adb, build_release,"
    INF "           build_release_install, clean, clean_build, logcat, logcat_all,"
    INF "           screenshot, version, devices, launch, stop"
    exit 1
}

# -- Interactive loop -----------------------------------------
while ($true) {
    $script:NeedsPause = $true   # build commands set this to $false
    Show-Menu
    $choice = Read-Host "  Enter choice"
    if ($choice -eq "0") { break }

    if ($DISPATCH.ContainsKey($choice)) {
        try {
            & $DISPATCH[$choice]
        }
        catch {
            ERR "Command failed: $_"
            $script:NeedsPause = $true
        }
    }
    else {
        WRN "Invalid choice: $choice"
    }

    Write-Host ""
    if ($script:NeedsPause) { Pause-Brief 10 }
}

Write-Host ""
INF "Bye!"