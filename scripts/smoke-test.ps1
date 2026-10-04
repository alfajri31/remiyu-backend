param([string]$BaseUrl = 'http://localhost:8081')
$ErrorActionPreference = 'Stop'
$taskSuffix = [Guid]::NewGuid().ToString('N')
$taskCredentials = @{
    name = 'Smoke Test'
    email = "smoke-$taskSuffix@remiyu.local"
    password = [Guid]::NewGuid().ToString('N')
}
$taskAuth = Invoke-RestMethod "$BaseUrl/api/v1/auth/register" -Method Post -ContentType 'application/json' -Body ($taskCredentials | ConvertTo-Json)
$taskHeaders = @{ Authorization = "Bearer $($taskAuth.accessToken)" }
$taskWorkspace = Invoke-RestMethod "$BaseUrl/api/v1/workspaces" -Method Post -Headers $taskHeaders -ContentType 'application/json' -Body '{"name":"Smoke Test Workspace","platform":"WHATSAPP"}'
$taskForm = @{ file = Get-Item (Join-Path (Split-Path -Parent $PSScriptRoot) 'mockoon/sample-chat.txt') }
$taskImport = Invoke-RestMethod "$BaseUrl/api/v1/workspaces/$($taskWorkspace.id)/imports" -Method Post -Headers $taskHeaders -Form $taskForm
if ($taskImport.newMessages -ne 3 -or $taskImport.taskIds.Count -ne 1 -or $taskImport.agendaIds.Count -ne 1) { throw 'Import mock response mismatch' }
$taskConfirmed = Invoke-RestMethod "$BaseUrl/api/v1/workspaces/$($taskWorkspace.id)/tasks/$($taskImport.taskIds[0])/confirm" -Method Post -Headers $taskHeaders
if ($taskConfirmed.status -ne 'TODO') { throw 'Task confirmation failed' }
$taskRepeated = Invoke-RestMethod "$BaseUrl/api/v1/workspaces/$($taskWorkspace.id)/imports" -Method Post -Headers $taskHeaders -Form $taskForm
if ($taskRepeated.newMessages -ne 0 -or $taskRepeated.duplicateMessages -ne 3) { throw 'Reimport deduplication failed' }
Invoke-RestMethod "$BaseUrl/api/v1/auth/logout" -Method Post -Headers $taskHeaders
[pscustomobject]@{ backend = $BaseUrl; aiProvider = $taskImport.aiProvider; importMessages = $taskImport.newMessages; repeatedMessages = $taskRepeated.duplicateMessages; taskStatus = $taskConfirmed.status; result = 'PASS' }
# Persists one clearly labelled smoke-test account/workspace in the development database.

