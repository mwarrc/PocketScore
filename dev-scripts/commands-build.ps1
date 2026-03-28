# ============================================================
#  Build Commands
# ============================================================

function cmd-install_debug {
    $script:NeedsPause = $false
    while ($true) {
        HDR "Build + Install DEBUG (Gradle installDebug)"
        $log = Join-Path $LOGS_DIR "install_debug_$(Get-TS).log"
        $rc  = Invoke-Gradle @("installDebug") $log
        if ($rc -eq 0) { OK "Debug install succeeded."; $script:NeedsPause = $true; break }
        if (-not (Show-BuildFailPrompt $log $rc)) { break }
    }
}

function cmd-build_debug_adb {
    $script:NeedsPause = $false
    while ($true) {
        HDR "Build DEBUG APK then install via ADB"
        $log = Join-Path $LOGS_DIR "build_adb_$(Get-TS).log"
        $rc  = Invoke-Gradle @("assembleDebug") $log
        if ($rc -ne 0) {
            if (Show-BuildFailPrompt $log $rc) { continue } else { break }
        }
        OK "Build succeeded."
        Install-APK $APK_DEBUG
        $script:NeedsPause = $true
        break
    }
}

function cmd-build_debug {
    $script:NeedsPause = $false
    while ($true) {
        HDR "Build DEBUG APK (no install)"
        $log = Join-Path $LOGS_DIR "build_debug_$(Get-TS).log"
        $rc  = Invoke-Gradle @("assembleDebug") $log
        if ($rc -eq 0) { OK "APK: $APK_DEBUG"; $script:NeedsPause = $true; break }
        if (-not (Show-BuildFailPrompt $log $rc)) { break }
    }
}

# Returns $true if APK was produced, $false if build failed (caller handles retry)
function Build-ReleaseAPK ([ref]$OutApk) {
    if (-not (Ensure-Keystore)) { return $false }
    $log = Join-Path $LOGS_DIR "build_release_$(Get-TS).log"
    $rc  = Invoke-Gradle @("assembleRelease") $log
    if ($rc -ne 0) {
        if (Show-BuildFailPrompt $log $rc) { $OutApk.Value = "RETRY" }
        else { $OutApk.Value = $null }
        return $false
    }
    $releaseDir = Join-Path $ROOT "app\build\outputs\apk\release"
    $candidates = @(
        (Join-Path $releaseDir "app-release.apk"),
        (Join-Path $releaseDir "app-release-unsigned.apk")
    )
    $source = $null
    foreach ($c in $candidates) { if (Test-Path $c) { $source = $c; break } }
    if (-not $source) { ERR "Could not locate release APK in $releaseDir"; $OutApk.Value = $null; return $false }
    if ($source -notmatch "unsigned") {
        OK "Gradle signed APK: $source"
        Copy-Item $source $APK_SIGNED -Force
        OK "Ready: $APK_SIGNED"
        $OutApk.Value = $APK_SIGNED
        return $true
    }
    INF "Signing manually: $source"
    $ok = Sign-APK $source $APK_SIGNED
    if ($ok) { OK "Ready: $APK_SIGNED"; $OutApk.Value = $APK_SIGNED; return $true }
    $OutApk.Value = $null
    return $false
}

function cmd-build_release {
    $script:NeedsPause = $false
    while ($true) {
        HDR "Build RELEASE APK + auto-sign"
        $apk = $null
        if (Build-ReleaseAPK ([ref]$apk)) { $script:NeedsPause = $true; break }
        # Build-ReleaseAPK already showed fail prompt; loop = retry, no loop = exit
        if ($apk -eq $null) { break }   # user chose menu
    }
}

function cmd-build_release_install {
    $script:NeedsPause = $false
    while ($true) {
        HDR "Build RELEASE + Sign + ADB Install"
        $apk = $null
        $built = Build-ReleaseAPK ([ref]$apk)
        if (-not $built) {
            if ($apk -eq "RETRY") { continue }
            break
        }
        Install-APK $apk
        $script:NeedsPause = $true
        break
    }
}

function cmd-clean_build_debug {
    $script:NeedsPause = $false
    while ($true) {
        HDR "Clean + Build DEBUG"
        $log = Join-Path $LOGS_DIR "clean_build_$(Get-TS).log"
        $rc  = Invoke-Gradle @("clean", "assembleDebug") $log
        if ($rc -eq 0) { OK "Clean build succeeded."; $script:NeedsPause = $true; break }
        if (-not (Show-BuildFailPrompt $log $rc)) { break }
    }
}

function cmd-clean {
    $script:NeedsPause = $false
    HDR "Clean Project"
    $rc = Invoke-Gradle @("clean") ""
    if ($rc -eq 0) { OK "Project cleaned."; $script:NeedsPause = $true } else { ERR "Clean failed." }
}
