# Renames the kit's package root and moves the source directories to match (Windows PowerShell 5.1+).
#
#   .\rename-package.ps1 -NewPackage com.company.myapp -AppName "My App"
#
# Reads/writes files as UTF-8 WITHOUT BOM explicitly: Get-Content/Set-Content in PowerShell 5.1
# decode UTF-8 as the ANSI code page and would double-encode every non-ASCII character in comments.
param(
    [Parameter(Mandatory = $true)] [string] $NewPackage,
    [string] $AppName = ""
)
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$utf8 = New-Object System.Text.UTF8Encoding($false)
$props = [System.IO.File]::ReadAllText("gradle.properties", $utf8)
if ($props -notmatch '(?m)^app\.applicationId=(\S+)') { throw "could not read app.applicationId from gradle.properties" }
$OldPackage = $Matches[1]
if ($OldPackage -eq $NewPackage) { Write-Host "already $NewPackage"; exit 0 }

$oldPath = $OldPackage.Replace(".", "\")
$newPath = $NewPackage.Replace(".", "\")
Write-Host "$OldPackage -> $NewPackage"

$extensions = @("*.kt", "*.kts", "*.xml", "*.pro", "*.md", "*.properties", "*.yml", "*.json")
$skip = '\\(build|\.gradle|\.git|\.idea)\\'

# 1. Text replacement.
Get-ChildItem -Recurse -File -Include $extensions | Where-Object { $_.FullName -notmatch $skip } | ForEach-Object {
    $text = [System.IO.File]::ReadAllText($_.FullName, $utf8)
    if ($text.Contains($OldPackage)) {
        [System.IO.File]::WriteAllText($_.FullName, $text.Replace($OldPackage, $NewPackage), $utf8)
    }
}

# 2. Move every com\old\pkg directory to com\new\pkg, deepest first.
$dirs = Get-ChildItem -Recurse -Directory | Where-Object {
    $_.FullName -notmatch $skip -and $_.FullName.EndsWith("\" + $oldPath)
} | Sort-Object { $_.FullName.Length } -Descending

foreach ($dir in $dirs) {
    $base = $dir.FullName.Substring(0, $dir.FullName.Length - $oldPath.Length - 1)
    $target = Join-Path $base $newPath
    New-Item -ItemType Directory -Force $target | Out-Null
    Copy-Item -Path (Join-Path $dir.FullName "*") -Destination $target -Recurse -Force
    Remove-Item -Recurse -Force $dir.FullName
    # prune now-empty parents of the old path
    $p = Split-Path $dir.FullName -Parent
    while ($p -ne $base -and (Test-Path $p) -and -not (Get-ChildItem $p -Force | Select-Object -First 1)) {
        Remove-Item $p; $p = Split-Path $p -Parent
    }
}

# 3. Optional display name.
if ($AppName -ne "") {
    $strings = "core\designsystem\src\main\res\values\strings.xml"
    $text = [System.IO.File]::ReadAllText($strings, $utf8)
    $text = [regex]::Replace($text, '<string name="app_name" translatable="false">[^<]*</string>', "<string name=`"app_name`" translatable=`"false`">$AppName</string>")
    [System.IO.File]::WriteAllText($strings, $text, $utf8)
}

Write-Host "done. Remaining references to the old package:"
$left = Get-ChildItem -Recurse -File -Include $extensions | Where-Object { $_.FullName -notmatch $skip } |
    Select-String -SimpleMatch $OldPackage
if ($left) { $left | ForEach-Object { Write-Host "  $($_.Path):$($_.LineNumber)" } } else { Write-Host "  none" }
