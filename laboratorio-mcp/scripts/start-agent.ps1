#Requires -Version 5.1
[CmdletBinding()]
param([switch]$CheckMcp)
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
if (-not $env:JAVA_HOME) { throw 'Define JAVA_HOME con un JDK 21 o superior.' }
if (-not $CheckMcp -and [string]::IsNullOrWhiteSpace($env:GOOGLE_API_KEY)) {
    throw 'Define GOOGLE_API_KEY en esta terminal. -CheckMcp no necesita clave Gemini.'
}
if (-not $env:GOOGLE_GENAI_USE_VERTEXAI) { $env:GOOGLE_GENAI_USE_VERTEXAI = 'FALSE' }
$execution = if ($CheckMcp) { 'exec:java@checkMcp' } else { 'exec:java@runAdkWebServer' }
Push-Location $root
try {
    & (Join-Path $root 'mvnw.cmd') -f laboratorio-mcp/agente-mcp/pom.xml compile $execution
    if ($LASTEXITCODE -ne 0) { throw "Maven terminó con código $LASTEXITCODE" }
} finally { Pop-Location }
