param(
    [string]$JarPath = "task1/target/gold-price-api-0.0.1-SNAPSHOT.jar",
    [int]$Port = 18086
)

$ErrorActionPreference = "Stop"
$resolvedJar = (Resolve-Path $JarPath).Path
$smokeRoot = Join-Path ([System.IO.Path]::GetTempPath()) "gold-price-api-week6-smoke"
New-Item -ItemType Directory -Force -Path $smokeRoot | Out-Null
$stdoutPath = Join-Path $smokeRoot "stdout.log"
$stderrPath = Join-Path $smokeRoot "stderr.log"

$env:SERVER_PORT = $Port.ToString()
$env:DB_URL = "jdbc:h2:mem:week6-smoke;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1"
$env:CACHE_TYPE = "simple"
$env:GOLD_PRICE_SCHEDULER_ENABLED = "false"
$env:LOG_PATH = Join-Path $smokeRoot "logs"

$process = Start-Process -FilePath java -ArgumentList @("-jar", $resolvedJar) `
    -WorkingDirectory $smokeRoot -RedirectStandardOutput $stdoutPath `
    -RedirectStandardError $stderrPath -PassThru -WindowStyle Hidden
$processId = $process.Id
$result = $null

try {
    $health = $null
    for ($attempt = 0; $attempt -lt 30; $attempt++) {
        if ($process.HasExited) { break }
        try {
            $health = Invoke-RestMethod "http://localhost:$Port/api/gold-prices/health" -TimeoutSec 2
            if ($health.status -eq "ok") { break }
        } catch {
            Start-Sleep -Milliseconds 500
        }
    }

    if ($null -eq $health -or $health.status -ne "ok") {
        Get-Content $stdoutPath -Tail 100 -ErrorAction SilentlyContinue
        Get-Content $stderrPath -Tail 100 -ErrorAction SilentlyContinue
        throw "Packaged JAR did not become healthy"
    }

    $openApi = Invoke-WebRequest "http://localhost:$Port/v3/api-docs" -UseBasicParsing -TimeoutSec 5
    $list = Invoke-RestMethod "http://localhost:$Port/api/gold-prices?page=0&size=1" -TimeoutSec 5
    $result = [pscustomobject]@{
        JarProcessId = $processId
        Health = $health.status
        Service = $health.service
        OpenApiStatus = $openApi.StatusCode
        ListPage = $list.page
        TotalElements = $list.totalElements
        LogFileExists = Test-Path (Join-Path $env:LOG_PATH "gold-price-api.log")
    }
} finally {
    if (Get-Process -Id $processId -ErrorAction SilentlyContinue) {
        Stop-Process -Id $processId -Force
    }
    $listener = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if ($listener) {
        Stop-Process -Id $listener.OwningProcess -Force
    }
    Remove-Item Env:SERVER_PORT, Env:DB_URL, Env:CACHE_TYPE, `
        Env:GOLD_PRICE_SCHEDULER_ENABLED, Env:LOG_PATH -ErrorAction SilentlyContinue
}

$result
exit 0
