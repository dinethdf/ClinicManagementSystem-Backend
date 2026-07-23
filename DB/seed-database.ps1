<#
.SYNOPSIS
    ClinicManagementSystem - Database Seeder
    Inserts all sample/seed data into an already-created database.

.DESCRIPTION
    Runs the seed SQL script against your MySQL database.
    Password is NEVER hardcoded — it is read from the $env:DB_PASSWORD
    environment variable or prompted securely at runtime.

.PARAMETER DbHost
    MySQL server hostname. Default: localhost

.PARAMETER DbPort
    MySQL server port. Default: 3307

.PARAMETER DbUser
    MySQL username. Default: root

.PARAMETER DbName
    Target database name. Default: clinicmsystem

.PARAMETER DbPassword
    MySQL password. If not provided, reads $env:DB_PASSWORD or prompts securely.

.EXAMPLE
    # Local — will prompt for password
    .\seed-database.ps1

.EXAMPLE
    # Set password via environment variable (good for automation)
    $env:DB_PASSWORD = "mypassword"
    .\seed-database.ps1

.EXAMPLE
    # Remote server
    .\seed-database.ps1 -DbHost "your-server.com" -DbPort 3306 -DbUser "admin" -DbName "clinicmsystem"
#>

param(
    [string]$DbHost     = "localhost",
    [string]$DbPort     = "3307",
    [string]$DbUser     = "root",
    [string]$DbName     = "clinicmsystem",
    [string]$DbPassword = ""
)

# ─────────────────────────────────────────────
#  HELPERS
# ─────────────────────────────────────────────

function Write-Header {
    Write-Host ""
    Write-Host "╔══════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║       ClinicManagementSystem — Database Seeder       ║" -ForegroundColor Cyan
    Write-Host "╚══════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
}

function Write-Step {
    param([string]$Message)
    Write-Host "  ⟳  $Message" -ForegroundColor Yellow
}

function Write-Ok {
    param([string]$Message)
    Write-Host "  ✅  $Message" -ForegroundColor Green
}

function Write-Fail {
    param([string]$Message)
    Write-Host "  ❌  $Message" -ForegroundColor Red
}

function Write-Info {
    param([string]$Message)
    Write-Host "  ℹ  $Message" -ForegroundColor DarkCyan
}

function Write-SectionLine {
    Write-Host "  ──────────────────────────────────────────────────────" -ForegroundColor DarkGray
}

# ─────────────────────────────────────────────
#  STEP 1 — BANNER
# ─────────────────────────────────────────────

Write-Header

# ─────────────────────────────────────────────
#  STEP 2 — DETECT mysql.exe
# ─────────────────────────────────────────────

Write-Step "Checking for mysql.exe on PATH..."

$mysqlCmd = $null

# ── MySQL 8 is checked FIRST because this project runs on MySQL 8 (port 3307).
# ── The generic "mysql" on PATH may resolve to MySQL 5.5, so we skip it intentionally.
$commonPaths = @(
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",   # ← Your MySQL 8.0.44 (preferred)
    "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe",   # MySQL 8.4 if ever upgraded
    "C:\Program Files\MySQL\MySQL Server 8.2\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.1\bin\mysql.exe",
    "C:\xampp\mysql\bin\mysql.exe",
    "C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe",
    "C:\laragon\bin\mysql\mysql-8.0.30-winx64\bin\mysql.exe",
    "mysql"                                                      # PATH fallback (last resort)
)

foreach ($path in $commonPaths) {
    try {
        $result = & $path --version 2>&1
        if ($LASTEXITCODE -eq 0 -or $result -match "mysql") {
            $mysqlCmd = $path
            break
        }
    } catch {
        # Not found at this path, try next
    }
}

if (-not $mysqlCmd) {
    Write-Fail "mysql.exe not found automatically."
    Write-Host ""
    Write-Host "  To fix this, do ONE of the following:" -ForegroundColor White
    Write-Host ""
    Write-Host "  Option A — Add MySQL to PATH:" -ForegroundColor Yellow
    Write-Host "    1. Find your MySQL bin folder, e.g.:" -ForegroundColor Gray
    Write-Host "       C:\Program Files\MySQL\MySQL Server 8.0\bin" -ForegroundColor Gray
    Write-Host "    2. Add it to your System Environment Variables -> PATH" -ForegroundColor Gray
    Write-Host "    3. Restart PowerShell and run this script again" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  Option B — Specify the full path in this script:" -ForegroundColor Yellow
    Write-Host "    Edit seed-database.ps1 and add your mysql.exe path to `$commonPaths" -ForegroundColor Gray
    Write-Host ""
    exit 1
}

Write-Ok "Found mysql at: $mysqlCmd"

# ─────────────────────────────────────────────
#  STEP 3 — GET PASSWORD (SECURELY)
# ─────────────────────────────────────────────

Write-SectionLine

if ($DbPassword -eq "") {
    # Check environment variable first
    if ($env:DB_PASSWORD -and $env:DB_PASSWORD -ne "") {
        $DbPassword = $env:DB_PASSWORD
        Write-Info "Password loaded from `$env:DB_PASSWORD environment variable."
    } else {
        # Prompt securely (input is masked)
        Write-Host ""
        $securePass = Read-Host "  🔒 Enter MySQL password for user '$DbUser'" -AsSecureString
        $DbPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
            [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePass)
        )
        Write-Host ""
    }
} else {
    Write-Info "Password provided via -DbPassword parameter."
}

# ─────────────────────────────────────────────
#  STEP 4 — SHOW CONNECTION DETAILS
# ─────────────────────────────────────────────

Write-SectionLine
Write-Host ""
Write-Host "  Connection Details:" -ForegroundColor White
Write-Host "    Host     : $DbHost" -ForegroundColor Gray
Write-Host "    Port     : $DbPort" -ForegroundColor Gray
Write-Host "    User     : $DbUser" -ForegroundColor Gray
Write-Host "    Database : $DbName" -ForegroundColor Gray
Write-Host "    Password : $('*' * [Math]::Min($DbPassword.Length, 8)) (masked)" -ForegroundColor Gray
Write-Host ""

# ─────────────────────────────────────────────
#  STEP 5 — TEST CONNECTION
# ─────────────────────────────────────────────

Write-SectionLine
Write-Step "Testing MySQL connection..."

$testResult = & $mysqlCmd `
    --host="$DbHost" `
    --port="$DbPort" `
    --user="$DbUser" `
    "--password=$DbPassword" `
    --connect-timeout=10 `
    -e "SELECT 1;" `
    "$DbName" 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Fail "Cannot connect to MySQL."
    Write-Host ""
    Write-Host "  Error details:" -ForegroundColor Red
    Write-Host "    $testResult" -ForegroundColor DarkRed
    Write-Host ""
    Write-Host "  Possible causes:" -ForegroundColor Yellow
    Write-Host "    - Wrong password" -ForegroundColor Gray
    Write-Host "    - MySQL is not running" -ForegroundColor Gray
    Write-Host "    - Wrong host/port (current: ${DbHost}:${DbPort})" -ForegroundColor Gray
    Write-Host "    - Database '$DbName' does not exist yet (run schema script first)" -ForegroundColor Gray
    Write-Host ""
    exit 1
}

Write-Ok "Connected to MySQL at ${DbHost}:${DbPort} successfully."

# ─────────────────────────────────────────────
#  STEP 6 — LOCATE SEED FILE
# ─────────────────────────────────────────────

Write-SectionLine

# Resolve path relative to this script's location
$scriptDir  = Split-Path -Parent $MyInvocation.MyCommand.Path
$seedFile   = Join-Path $scriptDir "2_Basic insert dml"

if (-not (Test-Path $seedFile)) {
    Write-Fail "Seed file not found: $seedFile"
    Write-Host "  Make sure '2_Basic insert dml' is in the same folder as this script." -ForegroundColor Yellow
    exit 1
}

Write-Ok "Seed file located: $(Split-Path -Leaf $seedFile)"

# ─────────────────────────────────────────────
#  STEP 7 — RUN SEED FILE
# ─────────────────────────────────────────────

Write-SectionLine
Write-Step "Running seed data insert..."
Write-Host ""

$output = & $mysqlCmd `
    --host="$DbHost" `
    --port="$DbPort" `
    --user="$DbUser" `
    "--password=$DbPassword" `
    --default-character-set=utf8mb4 `
    --comments `
    "$DbName" `
    --execute="SOURCE $seedFile" 2>&1

$exitCode = $LASTEXITCODE

if ($exitCode -ne 0) {
    Write-Fail "Seed script failed with errors."
    Write-Host ""

    # Pretty-print errors
    $lines = $output -split "`n"
    foreach ($line in $lines) {
        $line = $line.Trim()
        if ($line -ne "") {
            if ($line -match "ERROR") {
                Write-Host "    $line" -ForegroundColor Red
            } elseif ($line -match "Warning") {
                Write-Host "    $line" -ForegroundColor Yellow
            } else {
                Write-Host "    $line" -ForegroundColor Gray
            }
        }
    }

    Write-Host ""
    Write-Host "  Common causes:" -ForegroundColor Yellow
    Write-Host "    - Schema tables don't exist yet (run '1_DB Create Quary' + '4_New_Migration' first)" -ForegroundColor Gray
    Write-Host "    - Duplicate data — database may already have seed data inserted" -ForegroundColor Gray
    Write-Host ""
    exit 1
}

# Print any warnings (non-fatal)
if ($output -match "Warning") {
    $lines = $output -split "`n"
    foreach ($line in $lines) {
        if ($line -match "Warning") {
            Write-Host "    ⚠  $($line.Trim())" -ForegroundColor Yellow
        }
    }
}

Write-Ok "Seed data inserted successfully!"

# ─────────────────────────────────────────────
#  STEP 8 — VERIFY ROW COUNTS
# ─────────────────────────────────────────────

Write-SectionLine
Write-Step "Verifying inserted data..."
Write-Host ""

$tables = @("clinics", "userAuth", "doctor", "patient", "specialization", "doctorSchedule", "item_types", "systemConfig", "expense_categories", "lab_test_types")

$allOk = $true

foreach ($table in $tables) {
    $countResult = & $mysqlCmd `
        --host="$DbHost" `
        --port="$DbPort" `
        --user="$DbUser" `
        "--password=$DbPassword" `
        --silent `
        --skip-column-names `
        "$DbName" `
        --execute="SELECT COUNT(*) FROM \`$table\`;" 2>&1

    if ($LASTEXITCODE -eq 0 -and $countResult -match "^\d+$") {
        $count = $countResult.Trim()
        if ([int]$count -gt 0) {
            Write-Host ("    {0,-30} {1,5} rows   ✅" -f $table, $count) -ForegroundColor Green
        } else {
            Write-Host ("    {0,-30} {1,5} rows   ⚠  (empty)" -f $table, $count) -ForegroundColor Yellow
            $allOk = $false
        }
    } else {
        Write-Host ("    {0,-30} ERROR reading count" -f $table) -ForegroundColor Red
        $allOk = $false
    }
}

# ─────────────────────────────────────────────
#  STEP 9 — FINAL SUMMARY
# ─────────────────────────────────────────────

Write-Host ""
Write-SectionLine
Write-Host ""

if ($allOk) {
    Write-Host "  ╔══════════════════════════════════════════════════╗" -ForegroundColor Green
    Write-Host "  ║  🎉  SEEDING COMPLETE — All data inserted!       ║" -ForegroundColor Green
    Write-Host "  ╚══════════════════════════════════════════════════╝" -ForegroundColor Green
} else {
    Write-Host "  ╔══════════════════════════════════════════════════╗" -ForegroundColor Yellow
    Write-Host "  ║  ⚠   SEEDING DONE — Some tables may be empty    ║" -ForegroundColor Yellow
    Write-Host "  ╚══════════════════════════════════════════════════╝" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "  Next steps:" -ForegroundColor White
Write-Host "    1. Open MySQL Workbench and verify the data" -ForegroundColor Gray
Write-Host "    2. Start your Spring Boot app (mvn spring-boot:run)" -ForegroundColor Gray
Write-Host "    3. Login with: dineth@admin.com  |  dineth@admin2.com" -ForegroundColor Gray
Write-Host ""
