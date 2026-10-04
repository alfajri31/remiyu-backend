param([switch]$UseWindowsTrust)
$ErrorActionPreference = 'Stop'
$taskOriginalNodeOptions = $env:NODE_OPTIONS
if ($UseWindowsTrust) { $env:NODE_OPTIONS = (@($env:NODE_OPTIONS, '--use-system-ca') -join ' ').Trim() }
Push-Location (Split-Path -Parent $PSScriptRoot)
try {
    & npx.cmd --yes @mockoon/cli@9.9.0 start --data ./mockoon/remiyu-ai.json --port 3002
    if ($LASTEXITCODE -ne 0) { throw 'Mockoon gagal dijalankan.' }
} finally { $env:NODE_OPTIONS = $taskOriginalNodeOptions; Pop-Location }
