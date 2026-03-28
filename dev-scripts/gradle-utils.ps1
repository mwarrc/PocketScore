# ============================================================
#  Gradle Utilities
# ============================================================

# -- Build failure prompt: [R]etry / [V]iew errors / [Enter] back ----------
function Show-BuildFailPrompt ([string]$Log, [int]$ExitCode) {
    Write-Host ""
    Write-Host "  +--------------------------------------------------+" -ForegroundColor DarkRed
    Write-Host "    BUILD FAILED (Code: $ExitCode)".PadRight(44) +           " " -ForegroundColor Red
    Write-Host "    [R] Retry   [V] View errors   [Enter] Menu      " -ForegroundColor DarkRed
    Write-Host "  +--------------------------------------------------+" -ForegroundColor DarkRed
    $ans = Read-Host "  Choice"
    if ($ans -match "^[Vv]$") {
        Write-Host ""
        Write-Host "  -------------- Error lines from log -------------- " -ForegroundColor DarkYellow
        if (Test-Path $Log) {
            Get-Content $Log |
                Where-Object { $_ -match "error:|FAILED|Exception|> Task .+ FAILED" } |
                ForEach-Object { Write-Host "  $_" -ForegroundColor Yellow }
        }
        Write-Host "  Log: $Log" -ForegroundColor DarkGray
        Write-Host ""
        $ans2 = Read-Host "  [R] Retry  [Enter] Menu"
        return ($ans2 -match "^[Rr]$")
    }
    return ($ans -match "^[Rr]$")
}

# -- Gradle wrapper helper ------------------------------------
function Invoke-Gradle ([string[]]$Tasks, [string]$Log) {
    if (-not (Test-Path $GRADLE)) { throw "gradlew.bat not found in $ROOT" }
    $joined = $Tasks -join " "
    HDR "Gradle: $joined"

    # Inject keystore as env vars so build.gradle.kts signingConfig works locally
    $env:CM_KEYSTORE          = $KS_PATH
    $env:CM_KEYSTORE_PASSWORD = $KS_PASS
    $env:CM_KEY_ALIAS         = $KS_ALIAS
    $env:CM_KEY_PASSWORD      = $KS_KEYPASS

    # Always log to a file -- spinner tails it live.
    # If the caller supplied a log path, use it; otherwise make a temp one.
    $useLog = if ($Log) { $Log } else { Join-Path $LOGS_DIR "gradle_$(Get-TS).log" }

    # Write a tiny batch file to dodge cmd.exe quoting nightmares
    $bat = Join-Path $env:TEMP "pocketscore_gradle.bat"
    "@echo off`r`ncall `"$GRADLE`" $joined > `"$useLog`" 2>&1`r`nexit /b %ERRORLEVEL%" |
        Set-Content $bat -Encoding ASCII

    # Launch Gradle in the background (inherits all $env: vars we just set)
    $proc = Start-Process "cmd.exe" `
        -ArgumentList "/c `"$bat`"" `
        -NoNewWindow -WorkingDirectory $ROOT -PassThru

    # -- Animated spinner -----------------------------------------
    $frames   = @('|', '/', '-', '\')
    $dots     = @('   ', '.  ', '.. ', '...')
    $frameIdx = 0
    $start    = Get-Date
    $lastTask = "Starting Gradle..."
    $spinW    = 82   # console width to clear

    while (-not $proc.HasExited) {
        $elapsed    = (Get-Date) - $start
        $elapsedStr = "{0:mm\:ss}" -f $elapsed

        # Peek at the tail of the live log for the latest task name
        if (Test-Path $useLog) {
            $tail = Get-Content $useLog -Tail 6 -ErrorAction SilentlyContinue
            $hit  = $tail | Where-Object { $_ -match "^> Task " } | Select-Object -Last 1
            if ($hit -and $hit -match "> Task (.+)") { $lastTask = $Matches[1] }
        }

        $frame  = $frames[$frameIdx % $frames.Length]
        $dot    = $dots[$frameIdx % $dots.Length]
        $status = "  $frame  Building$dot  [$elapsedStr]  $lastTask"
        Write-Host "`r$($status.PadRight($spinW))" -NoNewline -ForegroundColor Cyan

        Start-Sleep -Milliseconds 180
        $frameIdx++
    }

    # Ensure process has fully terminated and exit code is flushed
    $proc.WaitForExit()
    $exitCode = $proc.ExitCode

    # Wait a tiny bit extra to let the file system flush the log
    Start-Sleep -Milliseconds 200

    # Clear spinner line before dumping output
    Write-Host "`r$(' ' * $spinW)`r" -NoNewline
    # --------------------------------------------------------------

    # Stream captured output to console
    if (Test-Path $useLog) { Get-Content $useLog | Write-Host }

    # Fallback to checking the exact string in the log if exit code is null
    $finalCode = if ($null -eq $exitCode) {
        if ((Test-Path $useLog) -and (Get-Content $useLog | Select-String 'BUILD SUCCESSFUL' -Quiet)) { 0 } else { 1 }
    } else { 
        [int]$exitCode 
    }

    if ($finalCode -eq 0 -and (Test-Path $useLog)) {
        if ((Get-Content $useLog | Select-String 'BUILD FAILED' -Quiet)) {
            $finalCode = 1
        }
    }

    return $finalCode
}
