# Test Keycloak auth: get token and call protected endpoints.
# Usage: .\test-auth.ps1 -Username "youruser" -Password "yourpassword"
# Or set env: $env:TEST_USER = "user"; $env:TEST_PASS = "pass"; .\test-auth.ps1

param(
    [string]$Username = $env:TEST_USER,
    [string]$Password = $env:TEST_PASS,
    [string]$KeycloakUrl = "http://localhost:8080",
    [string]$Realm = "qtrobot",
    [string]$ClientId = "qtrobot-realm",
    [string]$ClientSecret = $env:KEYCLOAK_CLIENT_SECRET,
    [string]$BackendUrl = "http://localhost:8000"
)

# Load .env from backend root if ClientSecret not set
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendDir = Split-Path -Parent $scriptDir
$envPath = Join-Path $backendDir ".env"
if (-not $ClientSecret -and (Test-Path $envPath)) {
    Get-Content $envPath | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
            $key = $matches[1].Trim(); $val = $matches[2].Trim()
            if ($key -eq "KEYCLOAK_CLIENT_SECRET") { $script:ClientSecret = $val }
        }
    }
}

if (-not $Username -or -not $Password) {
    Write-Host "Usage: .\test-auth.ps1 -Username 'youruser' -Password 'yourpassword'"
    Write-Host "   Or: `$env:TEST_USER='user'; `$env:TEST_PASS='pass'; .\test-auth.ps1"
    exit 1
}
if (-not $ClientSecret) {
    Write-Host "KEYCLOAK_CLIENT_SECRET not set. Set it in backend\.env or pass -ClientSecret."
    exit 1
}

$tokenUrl = "$KeycloakUrl/realms/$Realm/protocol/openid-connect/token"
$body = @{
    username     = $Username
    password     = $Password
    grant_type   = "password"
    client_id    = $ClientId
    client_secret = $ClientSecret
}

Write-Host "Requesting token from Keycloak..."
try {
    $response = Invoke-RestMethod -Uri $tokenUrl -Method Post -Body $body -ContentType "application/x-www-form-urlencoded"
} catch {
    Write-Host "Token request failed: $_"
    exit 1
}

$token = $response.access_token
Write-Host "Token received. Calling backend..."

$headers = @{ Authorization = "Bearer $token" }

Write-Host "`n--- GET $BackendUrl/protected ---"
Invoke-RestMethod -Uri "$BackendUrl/protected" -Headers $headers | ConvertTo-Json

Write-Host "`n--- GET $BackendUrl/user/me ---"
Invoke-RestMethod -Uri "$BackendUrl/user/me" -Headers $headers | ConvertTo-Json

Write-Host "`n--- GET $BackendUrl/user/roles ---"
Invoke-RestMethod -Uri "$BackendUrl/user/roles" -Headers $headers | ConvertTo-Json

Write-Host "`nAuth test completed successfully."
