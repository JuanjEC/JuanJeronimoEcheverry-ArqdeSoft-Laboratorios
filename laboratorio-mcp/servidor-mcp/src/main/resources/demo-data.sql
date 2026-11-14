-- Opcional, manual y separado del esquema. Alternativa al seed Java app.seed-demo=true.
-- Cada INSERT omite registros ya existentes. Nunca borra ni restablece stock.
USE tareas_mcp;
INSERT INTO producto (nombre, precio, stock, version)
SELECT 'Arroz 1 kg', 4500.00, 30, 0 WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Arroz 1 kg');
INSERT INTO producto (nombre, precio, stock, version)
SELECT 'Leche 1 L', 3800.00, 20, 0 WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Leche 1 L');
INSERT INTO producto (nombre, precio, stock, version)
SELECT 'Huevos docena', 12000.00, 15, 0 WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Huevos docena');
INSERT INTO producto (nombre, precio, stock, version)
SELECT 'Pan integral', 6500.00, 12, 0 WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Pan integral');
INSERT INTO tarea (titulo, fecha, completado, version)
SELECT 'Preparar charla MCP', '2026-11-14', b'0', 0
WHERE NOT EXISTS (SELECT 1 FROM tarea WHERE titulo = 'Preparar charla MCP');
INSERT INTO tarea (titulo, fecha, completado, version)
SELECT 'Revisar slides', '2026-11-15', b'0', 0
WHERE NOT EXISTS (SELECT 1 FROM tarea WHERE titulo = 'Revisar slides');
INSERT INTO tarea (titulo, fecha, completado, version)
SELECT 'Probar demo en vivo', '2026-11-15', b'1', 0
WHERE NOT EXISTS (SELECT 1 FROM tarea WHERE titulo = 'Probar demo en vivo');
