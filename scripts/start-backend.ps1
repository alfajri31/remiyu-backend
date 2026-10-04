param([switch]$UseWindowsTrust)
$ErrorActionPreference = 'Stop'
Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    $taskMavenArgs = @('spring-boot:run')
    if ($UseWindowsTrust) { $taskMavenArgs = @('-Djavax.net.ssl.trustStoreType=Windows-ROOT', '-Djavax.net.ssl.trustStore=NONE') + $taskMavenArgs }
    & mvn.cmd @taskMavenArgs
    if ($LASTEXITCODE -ne 0) { throw 'Backend gagal dijalankan.' }
} finally { Pop-Location }
