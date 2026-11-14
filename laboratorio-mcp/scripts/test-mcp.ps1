#Requires -Version 5.1
[CmdletBinding()]
param([string]$Url = 'http://127.0.0.1:8088/mcp', [switch]$WriteDemo)
$ErrorActionPreference = 'Stop'
$script:headers = @{ Accept = 'application/json, text/event-stream' }
$script:sequence = 0
function Invoke-Mcp([string]$Method, [hashtable]$Params, [switch]$Notification) {
    $body = @{ jsonrpc = '2.0'; method = $Method; params = $Params }
    if (-not $Notification) { $script:sequence++; $body.id = $script:sequence }
    $response = Invoke-WebRequest -UseBasicParsing -Method Post -Uri $Url -Headers $script:headers `
        -ContentType 'application/json; charset=utf-8' -Body ($body | ConvertTo-Json -Depth 20 -Compress) -TimeoutSec 30
    if ($response.Headers['Mcp-Session-Id']) { $script:headers['Mcp-Session-Id'] = $response.Headers['Mcp-Session-Id'] }
    if ($Notification) { return }
    $text = [string]$response.Content
    if ($text.TrimStart().StartsWith('{')) { $rpc = $text | ConvertFrom-Json }
    else {
        $rpc = $null
        foreach ($line in ($text -split '\r?\n')) {
            if ($line.StartsWith('data:')) {
                $candidate = $line.Substring(5).Trim() | ConvertFrom-Json
                if ($candidate.id -eq $body.id) { $rpc = $candidate; break }
            }
        }
        if (-not $rpc) { throw "No se recibió respuesta JSON-RPC para $Method" }
    }
    if ($rpc.error) { throw ($rpc.error | ConvertTo-Json -Compress) }
    return $rpc.result
}
function Invoke-Tool([string]$Name, [hashtable]$Arguments) {
    $result = Invoke-Mcp 'tools/call' @{ name = $Name; arguments = $Arguments }
    if ($result.isError) { throw ($result.content | ConvertTo-Json -Depth 10 -Compress) }
    $text = ($result.content | Where-Object { $_.type -eq 'text' } | ForEach-Object { $_.text }) -join "`n"
    try { return ($text | ConvertFrom-Json -ErrorAction Stop) }
    catch { return $text } # Los resultados String de Spring AI pueden ser texto sin comillas JSON.
}

try {
    $init = Invoke-Mcp 'initialize' @{ protocolVersion = '2025-03-26'; capabilities = @{}; clientInfo = @{ name = 'laboratorio-smoke'; version = '1.0' } }
    $script:headers['MCP-Protocol-Version'] = $init.protocolVersion
    Invoke-Mcp 'notifications/initialized' @{} -Notification
    $tools = Invoke-Mcp 'tools/list' @{}
    $expected = @('listarTareas','listarPendientes','buscarTareas','crearTarea','marcarCompletada','actualizarTarea','eliminarTarea','contarTareas','listarCompletadas','listarProductos','buscarProductos','crearPedido','consultarPedido','cancelarPedido')
    foreach ($name in $expected) {
        if ($name -notin $tools.tools.name) { throw "Falta herramienta $name" }
    }
    Write-Host "Handshake correcto. Herramientas: $($tools.tools.Count)"
    $pending = @(Invoke-Tool 'listarPendientes' @{})
    $products = @(Invoke-Tool 'listarProductos' @{})
    Write-Host "Tareas pendientes: $($pending.Count). Productos: $($products.Count)."
    if ($WriteDemo) {
        # Sólo datos sintéticos. El pedido queda registrado CANCELADO y el stock se repone.
        $task = $null
        $order = $null
        try {
            $task = Invoke-Tool 'crearTarea' @{ titulo = "Smoke MCP $([guid]::NewGuid())"; fecha = '2026-11-20' }
            $null = Invoke-Tool 'actualizarTarea' @{ id = $task.id; titulo = 'Smoke MCP actualizada'; fecha = $null }
            $done = Invoke-Tool 'marcarCompletada' @{ id = $task.id }
            if (-not $done.completado) { throw 'No se completó la tarea' }
            $product = $products | Where-Object { $_.stock -gt 0 } | Select-Object -First 1
            if (-not $product) { throw 'Se necesita un producto de demostración con stock' }
            $order = Invoke-Tool 'crearPedido' @{ cliente = 'Cliente de prueba MCP'; direccion = 'Dirección ficticia, laboratorio'; items = @(@{ productoId = $product.id; cantidad = 1 }); confirmado = $true }
            $saved = Invoke-Tool 'consultarPedido' @{ id = $order.id }
            if ($saved.estado -ne 'CREADO') { throw 'Estado inesperado del pedido' }
            Write-Host "Pedido sintético creado y consultado: $($order.id)"
        } finally {
            if ($order) { $null = Invoke-Tool 'cancelarPedido' @{ id = $order.id; confirmado = $true } }
            if ($task) { $null = Invoke-Tool 'eliminarTarea' @{ id = $task.id } }
        }
    }
    Write-Host 'Prueba MCP finalizada correctamente.'
} finally {
    if ($script:headers['Mcp-Session-Id']) {
        try { $null = Invoke-WebRequest -UseBasicParsing -Method Delete -Uri $Url -Headers $script:headers -TimeoutSec 10 }
        catch { Write-Warning 'No fue posible cerrar la sesión MCP; expirará en el servidor.' }
    }
}
