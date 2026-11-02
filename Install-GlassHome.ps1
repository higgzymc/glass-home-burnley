param(
    [Parameter(Mandatory=$true)][string]$StickIP,
    [string]$Adb = 'adb'
)
$ErrorActionPreference = 'Stop'
if ($StickIP -notmatch '^\d{1,3}(\.\d{1,3}){3}$') { throw 'Enter the Fire Stick IPv4 address, without a port.' }
$device = "${StickIP}:5555"
$apk = Join-Path $PSScriptRoot 'release/GlassHome-Scenic.apk'
if (!(Test-Path -LiteralPath $apk)) { throw 'Download/extract the entire repository, including release/GlassHome-Scenic.apk.' }
function Invoke-AdbChecked {
    param([string[]]$Arguments)
    $result = & $Adb @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw ($result -join "`n") }
    return ($result -join "`n").Trim()
}
Write-Host (Invoke-AdbChecked -Arguments @('connect',$device))
Write-Host 'Accept the USB debugging prompt on your TV if shown.'
$state = Invoke-AdbChecked -Arguments @('-s',$device,'get-state')
if ($state -ne 'device') { throw 'Authorize this computer on the TV, then run this script again.' }
$previous = Invoke-AdbChecked -Arguments @('-s',$device,'shell','settings','get','secure','enabled_accessibility_services')
$enabled = Invoke-AdbChecked -Arguments @('-s',$device,'shell','settings','get','secure','accessibility_enabled')
$backup = Join-Path $PSScriptRoot ("accessibility-backup-{0}-{1}.json" -f $StickIP,(Get-Date -Format 'yyyyMMdd-HHmmss'))
@{device=$device;enabled_accessibility_services=$previous;accessibility_enabled=$enabled} | ConvertTo-Json | Set-Content -LiteralPath $backup
$install = Invoke-AdbChecked -Arguments @('-s',$device,'install','-r',$apk)
if ($install -notmatch 'Success') { throw $install }
$service = 'local.glasshome/local.glasshome.HomeService'
$services = @()
if ($previous -and $previous -ne 'null') { $services = @($previous -split ':') }
if ($services -notcontains $service) { $services += $service }
Write-Host (Invoke-AdbChecked -Arguments @('-s',$device,'shell','settings','put','secure','enabled_accessibility_services',($services -join ':')))
Write-Host (Invoke-AdbChecked -Arguments @('-s',$device,'shell','settings','put','secure','accessibility_enabled','1'))
Write-Host (Invoke-AdbChecked -Arguments @('-s',$device,'shell','am','start','-n','local.glasshome/.MainActivity'))
Write-Host "Installed. Press Home to test. Previous accessibility settings saved to $backup"
Write-Host 'This installer does not disable or uninstall any other apps.'
