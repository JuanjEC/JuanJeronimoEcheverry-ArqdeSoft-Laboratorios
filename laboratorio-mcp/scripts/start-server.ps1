#Requires -Version 5.1
[CmdletBinding()]
param([switch]$Local, [switch]$Demo)
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
if (-not $env:JAVA_HOME) { throw 'Define JAVA_HOME con un JDK 21 o superior antes de ejecutar.' }
if (-not $Local -and [string]::IsNullOrWhiteSpace($env:PWD_UDEA)) {
    throw 'Define PWD_UDEA con la contraseña de MySQL en esta terminal, o usa -Local para H2.'
}
$runArgs = @()
if ($Local) { $runArgs += '--spring.profiles.active=local' }
if ($Demo) { $runArgs += '--app.seed-demo=true' }
Push-Location $root
try {
    $mavenArgs = @('-f', 'laboratorio-mcp/servidor-mcp/pom.xml', 'spring-boot:run')
    if ($runArgs.Count -gt 0) { $mavenArgs += "-Dspring-boot.run.arguments=$($runArgs -join ' ')" }
    & (Join-Path $root 'mvnw.cmd') @mavenArgs
    if ($LASTEXITCODE -ne 0) { throw "Maven terminó con código $LASTEXITCODE" }
} finally { Pop-Location }
