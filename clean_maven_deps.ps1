# Maven Dependencies Cleanup Script
# This script removes incorrectly downloaded Maven dependencies from project root

Write-Host "Starting cleanup of Maven dependency folders..." -ForegroundColor Green

# Define list of dependency folders to remove
$depsToRemove = @(
    "aopalliance",
    "asm",
    "ch",
    "com",
    "commons-beanutils",
    "commons-cli",
    "commons-codec",
    "commons-collections",
    "commons-configuration",
    "commons-daemon",
    "commons-dbcp",
    "commons-digester",
    "commons-el",
    "commons-httpclient",
    "commons-io",
    "commons-lang",
    "commons-logging",
    "commons-net",
    "commons-pool",
    "de",
    "dnsjava",
    "io",
    "it",
    "jakarta",
    "javax",
    "javolution",
    "jline",
    "joda-time",
    "junit",
    "log4j",
    "mysql",
    "net",
    "org",
    "oro",
    "redis",
    "ru",
    "sqlline",
    "stax",
    "xerces",
    "xml-apis",
    "xmlenc"
)

$projectRoot = "e:\GitHub\onlyCode"
$totalCount = $depsToRemove.Count
$currentCount = 0

foreach ($dep in $depsToRemove) {
    $currentCount++
    $depPath = Join-Path $projectRoot $dep
    
    if (Test-Path $depPath) {
        Write-Host "[$currentCount/$totalCount] Removing: $dep" -ForegroundColor Yellow
        try {
            Remove-Item -Path $depPath -Recurse -Force -ErrorAction Stop
            Write-Host "  [OK] Deleted: $dep" -ForegroundColor Green
        }
        catch {
            Write-Host "  [FAIL] Failed to delete: $dep - $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    else {
        Write-Host "[$currentCount/$totalCount] Skipped (not exists): $dep" -ForegroundColor Gray
    }
}

Write-Host "`nCleanup completed!" -ForegroundColor Green
Write-Host "Maven local repository is configured at: C:\Users\jack\.m2\repository" -ForegroundColor Cyan
Write-Host "Please run 'mvn clean install' to re-download dependencies to the correct location" -ForegroundColor Cyan

