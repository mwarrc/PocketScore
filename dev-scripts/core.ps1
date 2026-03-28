# ============================================================
#  Dev Toolkit Core Framework
# ============================================================

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# -- Paths ----------------------------------------------------
$SCRIPT_DIR = $PSScriptRoot
$ROOT = Split-Path $SCRIPT_DIR -Parent
$LOGS_DIR = Join-Path $ROOT "logs"
$GRADLE = Join-Path $ROOT "gradlew.bat"
$APK_DEBUG = Join-Path $ROOT "app\build\outputs\apk\debug\app-debug.apk"
$APK_SIGNED = Join-Path $ROOT "app\build\outputs\apk\release\app-release-signed.apk"

# -- App identity ---------------------------------------------
$PACKAGE = "com.mwarrc.pocketscore"
$ACTIVITY = "com.mwarrc.pocketscore.MainActivity"

# -- Load local overrides if present --------------------------
$CONFIG = Join-Path $ROOT "dev.config.ps1"
if (Test-Path $CONFIG) { . $CONFIG }

# -- Timestamp ------------------------------------------------
function Get-TS { Get-Date -Format "yyyy-MM-dd_HHmmss" }

# -- Colour helpers -------------------------------------------
function OK  ([string]$m) { Write-Host "  [OK]  $m" -ForegroundColor Green }
function ERR ([string]$m) { Write-Host "  [!!]  $m" -ForegroundColor Red }
function INF ([string]$m) { Write-Host "  [i]   $m" -ForegroundColor Cyan }
function WRN ([string]$m) { Write-Host "  [~~]  $m" -ForegroundColor Yellow }
function HDR ([string]$m) {
    $line = "-" * ($m.Length + 2)
    Write-Host ""
    Write-Host "  $m" -ForegroundColor White
    Write-Host "  $line" -ForegroundColor DarkGray
}

# -- Tool Loaders ---------------------------------------------
. (Join-Path $SCRIPT_DIR "adb-utils.ps1")
. (Join-Path $SCRIPT_DIR "java-utils.ps1")
. (Join-Path $SCRIPT_DIR "gradle-utils.ps1")
. (Join-Path $SCRIPT_DIR "commands-build.ps1")
. (Join-Path $SCRIPT_DIR "commands-device.ps1")
. (Join-Path $SCRIPT_DIR "commands-logs.ps1")
. (Join-Path $SCRIPT_DIR "commands-utils.ps1")
. (Join-Path $SCRIPT_DIR "menu.ps1")

# Ensure logs folder
if (-not (Test-Path $LOGS_DIR)) { New-Item -ItemType Directory -Path $LOGS_DIR | Out-Null }
