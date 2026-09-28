param (
    [string]$DeviceId = "HOST-001",
    [string]$Secret = "change_me",
    [int]$DaysValid = 365
)

function Base64UrlEncode([byte[]]$bytes) {
    return [Convert]::ToBase64String($bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')
}

$header = @{
    alg = "HS256"
    typ = "JWT"
} | ConvertTo-Json -Compress

$now = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$exp = [DateTimeOffset]::UtcNow.AddDays($DaysValid).ToUnixTimeSeconds()

$payload = @{
    device_id = $DeviceId
    iss = "project-sentinel"
    sub = $DeviceId
    iat = $now
    exp = $exp
} | ConvertTo-Json -Compress

$headerB64 = Base64UrlEncode ([System.Text.Encoding]::UTF8.GetBytes($header))
$payloadB64 = Base64UrlEncode ([System.Text.Encoding]::UTF8.GetBytes($payload))
$toSign = "$headerB64.$payloadB64"

$hmac = New-Object System.Security.Cryptography.HMACSHA256
$hmac.Key = [System.Text.Encoding]::UTF8.GetBytes($Secret)
$sigBytes = $hmac.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($toSign))
$sigB64 = Base64UrlEncode $sigBytes

$token = "$toSign.$sigB64"

Write-Output "=== Sentinel JWT Token ==="
Write-Output "Device ID : $DeviceId"
Write-Output "Secret    : $Secret"
Write-Output "Issued At : $([DateTimeOffset]::FromUnixTimeSeconds($now).ToString('yyyy-MM-dd HH:mm:ss UTC'))"
Write-Output "Expires   : $([DateTimeOffset]::FromUnixTimeSeconds($exp).ToString('yyyy-MM-dd HH:mm:ss UTC'))"
Write-Output ""
Write-Output "Token:"
Write-Output $token
