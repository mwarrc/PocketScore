# ============================================================
#  Java and Sign Utilities
# ============================================================

# -- Java tool auto-detect ------------------------------------
function Find-JavaTool ([string]$tool) {
    $homes = @()
    if ($env:JAVA_HOME) { $homes += $env:JAVA_HOME }
    if ($env:ANDROID_HOME) { $homes += "$env:ANDROID_HOME\jdk" }
    foreach ($h in $homes) {
        $p = Join-Path $h "bin\$tool.exe"
        if (Test-Path $p) { return $p }
    }
    $inPath = Get-Command $tool -ErrorAction SilentlyContinue
    if ($inPath) { return $inPath.Source }
    return $null
}

# ============================================================
#  KEYSTORE -- zero-headache auto-setup
# ============================================================
function Ensure-Keystore {
    if (Test-Path $KS_PATH) { return $true }

    WRN "No keystore found at: $KS_PATH"
    INF "Auto-generating a dev keystore (valid for sideloading and testing)..."

    $keytool = Find-JavaTool "keytool"
    if (-not $keytool) {
        ERR "keytool not found. Install JDK or set JAVA_HOME."
        return $false
    }

    & $keytool `
        -genkeypair -v `
        -keystore   $KS_PATH `
        -alias      $KS_ALIAS `
        -keyalg     RSA `
        -keysize    2048 `
        -validity   10000 `
        -storepass  $KS_PASS `
        -keypass    $KS_KEYPASS `
        -dname      "CN=PocketScore Dev, OU=Dev, O=mwarrc, L=Nairobi, S=Nairobi, C=KE"

    if ($LASTEXITCODE -eq 0) {
        OK  "Keystore created: $KS_PATH"
        INF "To use your own keystore, edit dev.config.ps1"
        return $true
    }
    else {
        ERR "Keystore generation failed."
        return $false
    }
}

# -- Sign + zipalign an APK -----------------------------------
function Sign-APK ([string]$InputApk, [string]$OutputApk) {
    if (-not (Test-Path $InputApk)) {
        ERR "APK not found: $InputApk"
        return $false
    }

    $apksigner = Find-JavaTool "apksigner"
    $zipalign = $null

    # zipalign lives in build-tools
    if ($env:ANDROID_HOME) {
        $btRoot = "$env:ANDROID_HOME\build-tools"
        if (Test-Path $btRoot) {
            $latest = Get-ChildItem $btRoot -Directory |
            Sort-Object Name -Descending |
            Select-Object -First 1
            if ($latest) {
                $z = Join-Path $latest.FullName "zipalign.exe"
                if (Test-Path $z) { $zipalign = $z }
            }
        }
    }
    if (-not $zipalign) {
        $zCmd = Get-Command zipalign -ErrorAction SilentlyContinue
        if ($zCmd) { $zipalign = $zCmd.Source }
    }

    $aligned = $OutputApk -replace "\.apk$", "-aligned.apk"

    # Step 1 -- zipalign
    if ($zipalign) {
        INF "zipalign -> $aligned"
        & $zipalign -v -p 4 $InputApk $aligned | Out-Null
        if ($LASTEXITCODE -ne 0) {
            WRN "zipalign failed -- signing unaligned APK."
            $aligned = $InputApk
        }
    }
    else {
        WRN "zipalign not found. Skipping alignment (set ANDROID_HOME to fix this)."
        $aligned = $InputApk
    }

    # Step 2 -- sign
    if ($apksigner) {
        INF "apksigner -> $OutputApk"
        & $apksigner sign `
            --ks            $KS_PATH `
            --ks-key-alias  $KS_ALIAS `
            "--ks-pass"     "pass:$KS_PASS" `
            "--key-pass"    "pass:$KS_KEYPASS" `
            --out           $OutputApk `
            $aligned
    }
    else {
        $jarsigner = Find-JavaTool "jarsigner"
        if (-not $jarsigner) {
            ERR "Neither apksigner nor jarsigner found. Install JDK."
            return $false
        }
        INF "jarsigner -> $OutputApk"
        Copy-Item $aligned $OutputApk -Force
        & $jarsigner `
            -verbose `
            -sigalg    SHA256withRSA `
            -digestalg SHA-256 `
            -keystore  $KS_PATH `
            -storepass $KS_PASS `
            -keypass   $KS_KEYPASS `
            $OutputApk $KS_ALIAS
    }

    if ($LASTEXITCODE -ne 0) { ERR "Signing failed."; return $false }
    OK "Signed APK: $OutputApk"
    return $true
}
