param([ValidatePattern('^\d+\.\d+\.\d+$')][string]$Version = '1.0.0')
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path $PSScriptRoot -Parent)
if ($env:OS -ne 'Windows_NT') { throw 'Execute no Windows com JDK 17 e WiX 3.x.' }
function Check-Exit([string]$Step) { if ($LASTEXITCODE -ne 0) { throw "$Step falhou: $LASTEXITCODE" } }
foreach ($tool in @('java','javac','jar','jlink','jpackage')) { Get-Command $tool -ErrorAction Stop | Out-Null }
if (-not (Get-Command candle.exe -ErrorAction SilentlyContinue)) {
    $wix = Get-ChildItem "${env:ProgramFiles(x86)}\WiX Toolset v3*\bin\candle.exe" -ErrorAction SilentlyContinue | Select-Object -Last 1
    if (-not $wix) { throw 'WiX 3.x (candle.exe e light.exe) não encontrado.' }
    $env:Path = "$($wix.DirectoryName);$env:Path"
}
Get-Command light.exe -ErrorAction Stop | Out-Null
# Always compile and validate the exact source being packaged.
cmd /c build.bat test
Check-Exit 'Compilação e testes'
$stage = 'build/windows-package'
if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
New-Item "$stage/input" -ItemType Directory -Force | Out-Null
Copy-Item 'dist/super-api-bros.jar' "$stage/input/"
jlink --add-modules java.desktop,java.net.http,jdk.httpserver,jdk.crypto.ec --bind-services --strip-debug --no-header-files --no-man-pages --compress=2 --output "$stage/runtime"
Check-Exit 'Runtime Java'
jpackage --type app-image --name RedeDosHorizontes --app-version $Version --vendor 'Super API Bros' --input "$stage/input" --main-jar super-api-bros.jar --main-class br.edu.superapi.Main --runtime-image "$stage/runtime" --dest "$stage/image"
Check-Exit 'Aplicação Windows'
$app = "$stage/image/RedeDosHorizontes"
# Tests run with the bundled Java, not the runner's JDK.
& "$app/runtime/bin/java.exe" '-Djava.awt.headless=true' '-cp' "$app/app/super-api-bros.jar;build/test-classes" br.edu.superapi.AllTests
Check-Exit 'Testes com Java embutido'
# Exercise the native launcher and Swing startup before packaging it.
$process = Start-Process "$app/RedeDosHorizontes.exe" -PassThru
try {
    $opened = $false
    for ($attempt = 0; $attempt -lt 30; $attempt++) {
        Start-Sleep -Seconds 1
        $process.Refresh()
        if ($process.HasExited) { throw "Launcher encerrou antes de abrir a janela: $($process.ExitCode)" }
        if ($process.MainWindowHandle -ne 0) { $opened = $true; break }
    }
    if (-not $opened) { throw 'A janela do jogo não apareceu em 30 segundos.' }
    Write-Host 'Launcher nativo abriu a janela com sucesso.'
} finally { if (-not $process.HasExited) { Stop-Process -Id $process.Id -Force } }
jpackage --type exe --name RedeDosHorizontes --app-version $Version --app-image $app --dest "$stage/installer" --win-per-user-install --win-menu --win-shortcut --win-dir-chooser --win-upgrade-uuid 'd387f553-739b-445d-9816-3d91ac6ccace'
Check-Exit 'Instalador EXE'
New-Item installers -ItemType Directory -Force | Out-Null
$exe = Get-Item "$stage/installer/RedeDosHorizontes-$Version.exe"
if ($exe.Length -ge 95MB) { throw 'Instalador excede o limite de distribuição pelo repositório.' }
Copy-Item $exe.FullName 'installers/RedeDosHorizontes-Windows-x64.exe' -Force
$hash = (Get-FileHash 'installers/RedeDosHorizontes-Windows-x64.exe' -Algorithm SHA256).Hash.ToLowerInvariant()
"$hash  RedeDosHorizontes-Windows-x64.exe" | Set-Content 'installers/SHA256SUMS.txt' -Encoding ascii
$source = (git rev-parse HEAD).Trim()
$manifest = [ordered]@{ version=$Version; source_commit=$source; platform='Windows x64'; java='Temurin 17'; sha256=$hash; bytes=$exe.Length; validation=@('source tests','bundled runtime tests','native launcher window opened') }
$manifest | ConvertTo-Json | Set-Content 'installers/build-info.json' -Encoding utf8
Write-Host "Pronto: installers/RedeDosHorizontes-Windows-x64.exe ($($exe.Length) bytes)"
