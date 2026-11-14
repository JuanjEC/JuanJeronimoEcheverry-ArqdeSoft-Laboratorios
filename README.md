[![Build](https://github.com/JuanjEC/JuanJeronimoEcheverry-ArqdeSoft-Laboratorios/actions/workflows/build.yml/badge.svg)](https://github.com/JuanjEC/JuanJeronimoEcheverry-ArqdeSoft-Laboratorios/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios&metric=alert_status)](https://sonarcloud.io/project/overview?id=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios&metric=coverage)](https://sonarcloud.io/project/overview?id=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios&metric=ncloc)](https://sonarcloud.io/project/overview?id=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios&metric=sqale_index)](https://sonarcloud.io/project/overview?id=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios&metric=reliability_rating)](https://sonarcloud.io/project/overview?id=JuanjEC_JuanJeronimoEcheverry-ArqdeSoft-Laboratorios)
[![Snyk](https://snyk.io/test/github/JuanjEC/JuanJeronimoEcheverry-ArqdeSoft-Laboratorios/badge.svg)](https://snyk.io/test/github/JuanjEC/JuanJeronimoEcheverry-ArqdeSoft-Laboratorios)
[![Known Vulnerabilities](https://snyk.io/test/github/JuanjEC/JuanJeronimoEcheverry-ArqdeSoft-Laboratorios/badge.svg)](https://snyk.io/test/github/JuanjEC/JuanJeronimoEcheverry-ArqdeSoft-Laboratorios)
[![Docker Image](https://img.shields.io/badge/Docker-banco%3Alatest-blue?logo=docker)](https://hub.docker.com/r/JuanjEC/banco)
[![Deploy on Render](https://img.shields.io/badge/Render-Deployed-46E3B7?logo=render&logoColor=black)](https://render.com/)

# Laboratorios de Arquitectura de Software

En este repositorio reuní el desarrollo de Banco UdeA, su despliegue con CI/CD y el laboratorio MCP de tareas y pedidos de supermercado a domicilio.

- [Laboratorios 1 y 2: Banco UdeA](<#laboratorios-1-y-2-banco-udea>).
- [Laboratorio MCP](<#laboratorio-mcp>).
- [Pasos seguidos y resultados](<PASOS_SEGUIDOS.md>).

## Estructura del repositorio

```text
src/                              Backend de Banco UdeA
banco-frontend/                   Frontend del banco
laboratorio-mcp/
  servidor-mcp/                   Herramientas, servicios y persistencia MCP
  agente-mcp/                     Cliente ADK y chatbot Gemini
  config/                         Ejemplo de conexión para Claude Desktop
  scripts/                        Arranque y pruebas en PowerShell
.github/workflows/                Pipelines del banco y del laboratorio MCP
screenshots/                      Evidencias de los laboratorios
README.md                         Configuración y ejecución
PASOS_SEGUIDOS.md                 Desarrollo, pruebas y resultados
```

Mantuve la separación por capas: entidades, repositorios, servicios y DTOs. En el banco publiqué operaciones REST mediante controladores; en MCP registré las operaciones con `@Tool` y `ToolCallbackProvider`. El agente consume esas herramientas sin acceder directamente a la base de datos.

Conservé un POM por aplicación porque el banco, Spring AI y ADK utilizan versiones diferentes de Spring Boot. Así pude agregar el chatbot sin cambiar las dependencias ni el despliegue del banco. Todos los comandos se ejecutan desde la raíz con el mismo Maven Wrapper.

## Laboratorios 1 y 2: Banco UdeA

En el laboratorio 1 creé el frontend para consultar clientes, hacer transferencias y revisar movimientos. En el laboratorio 2 agregué el pipeline, publiqué el backend y probé la API desplegada con Postman y con el frontend.

### Despliegue y tecnologías

Servicio: https://banco-latest.onrender.com

Usé Java 17, Spring Boot 4.1.1, Spring Data JPA, H2 en memoria, MapStruct, Lombok, pruebas JUnit, JaCoCo, SonarCloud, GitHub Actions, Docker Hub y Render. El frontend está en [banco-frontend](<banco-frontend/index.html>).

### Endpoints

| Método | Ruta | Función |
|---|---|---|
| GET | `/api/customers` | Consultar clientes |
| GET | `/api/customers/{id}` | Consultar un cliente |
| POST | `/api/customers` | Registrar un cliente |
| POST | `/api/transactions` | Transferir dinero |
| GET | `/api/transactions/{account}` | Consultar movimientos de una cuenta |

### Configuración

Dejé H2 en memoria por defecto. Los datos se pierden al reiniciar el servicio. Para usar MySQL puedo cambiar las variables de entorno de la [configuración del banco](<src/main/resources/application.properties>).

| Variable | Función | Valor por defecto |
|---|---|---|
| `DB_URL` | URL JDBC | H2 en memoria |
| `DB_USER` | Usuario de BD | `sa` |
| `DB_PASSWORD` | Contraseña de BD | Vacía |
| `PORT` | Puerto HTTP | `8080` |

### Ejecución, pruebas y Docker

```bash
bash ./mvnw spring-boot:run
bash ./mvnw clean verify

bash ./mvnw package -DskipTests
docker build -t banco .
docker run -p 8080:8080 banco
```

En Windows uso `mvnw.cmd` en lugar de `bash ./mvnw`. El reporte de cobertura se genera en `target/site/jacoco/index.html`.

### Pipeline

Agregué los jobs `tests`, `sonar`, `build`, `docker` y `deploy` en el [workflow del banco](<.github/workflows/build.yml>). Los jobs de Docker y despliegue sólo se ejecutan al publicar en `main`. Las capturas y los pasos de configuración están en [Pasos seguidos](<PASOS_SEGUIDOS.md#laboratorio-2-cicd-del-backend-banco-udea>).

## Laboratorio MCP

Agregué un servidor de herramientas para administrar tareas y registrar pedidos de supermercado a domicilio. También implementé un chatbot con Google ADK/Gemini y preparé la conexión desde Claude Desktop. Los pedidos descuentan inventario y conservan su detalle; no realizan pagos ni entregas reales.

### Arquitectura

```text
Usuario
  +--> Claude Desktop (host y modelo)
  |      +--> Cliente MCP / mcp-remote
  +--> Interfaz ADK :8081 (host)
         +--> TareaAgent + Gemini + McpToolset (cliente MCP)
                           |
                Streamable HTTP :8088/mcp
                           |
                 Spring Boot + Spring AI
                   ToolCallbackProvider
                           |
               TareaToolsService / SupermercadoToolsService
                           |
                 Spring Data JPA -> MySQL / H2
```

El host presenta la conversación; el cliente MCP negocia el protocolo e invoca herramientas; el servidor ejecuta las operaciones de negocio. Reutilicé las mismas herramientas, validaciones y persistencia para ADK y Claude. Lo que cambia es el modelo, la interfaz y el cliente MCP.

### Tecnologías y puertos

| Componente | Configuración |
|---|---|
| [Servidor MCP](<laboratorio-mcp/servidor-mcp/pom.xml>) | Java 21, Spring Boot 3.5.11, Spring AI 1.1.4 |
| [Agente ADK](<laboratorio-mcp/agente-mcp/pom.xml>) | Java 21, Google ADK y ADK Dev 1.0.0, Spring Boot 4.0.2 |
| Endpoint MCP | `http://127.0.0.1:8088/mcp`, Streamable HTTP |
| Chatbot | `http://127.0.0.1:8081/dev-ui`, agente `TareaAgent` |
| Puente Claude | `mcp-remote@0.16.6` |

Dejé el puerto 8080 para el banco. Para Windows necesito JDK 21 o superior y PowerShell 5.1 o superior. Node.js y npm sólo hacen falta para Inspector o el puente de Claude; MySQL sólo para el perfil predeterminado.

### Herramientas

| Dominio | Operaciones |
|---|---|
| Tareas | `listarTareas`, `listarPendientes`, `listarCompletadas`, `contarTareas`, `buscarTareas`, `crearTarea`, `actualizarTarea`, `marcarCompletada`, `eliminarTarea` |
| Supermercado | `listarProductos`, `buscarProductos`, `crearPedido`, `consultarPedido`, `cancelarPedido` |

Validé fechas reales con formato `yyyy-MM-dd`, IDs positivos y títulos no vacíos. En `actualizarTarea`, los campos omitidos o `null` conservan su valor.

Para crear un pedido uso `cliente`, `direccion`, `items: [{productoId, cantidad}]` y `confirmado`. Admití de 1 a 100 líneas, sin productos repetidos y con cantidades enteras positivas. Calculé los importes en COP con `BigDecimal` y precios del catálogo. Crear y cancelar requieren `confirmado=true`; cancelar un pedido ya cancelado no vuelve a reponer stock.

### Variables y credenciales

Dejé las credenciales en variables de entorno.

| Variable | Componente | Configuración |
|---|---|---|
| `JAVA_HOME` | Java/Maven | Ruta del JDK |
| `DB_URL` | Servidor MySQL | Por defecto: `jdbc:mysql://localhost:3306/tareas_mcp?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC` |
| `DB_USER` | Servidor MySQL | Por defecto: `root` |
| `PWD_UDEA` | Servidor MySQL | Contraseña del usuario de BD; distinta de `DB_PASSWORD` del banco |
| `GOOGLE_API_KEY` | Agente | Clave Gemini; no hace falta para descubrir herramientas |
| `GOOGLE_GENAI_USE_VERTEXAI` | Agente | `FALSE` para Gemini API |
| `GEMINI_MODEL` | Agente | Por defecto: `gemini-2.5-flash`, según acceso y cuota |
| `MCP_SERVER_URL` | Agente | Por defecto: `http://127.0.0.1:8088/mcp` |
| `ADK_PORT` | Agente | Por defecto: `8081` |

### Chatbot ADK/Gemini

Con el servidor MCP activo, inicio el agente en otra terminal. En PowerShell ingreso la clave sin escribirla en el historial:

```powershell
$secret = Read-Host 'Clave Gemini' -AsSecureString
$env:GOOGLE_API_KEY = [System.Net.NetworkCredential]::new('', $secret).Password
$env:GOOGLE_GENAI_USE_VERTEXAI = 'FALSE'
.\laboratorio-mcp\scripts\start-agent.ps1
```

En Linux o Codespaces, con `GOOGLE_API_KEY` definida en el entorno:

```bash
export GOOGLE_GENAI_USE_VERTEXAI=FALSE
bash ./mvnw -f laboratorio-mcp/agente-mcp/pom.xml compile exec:java@runAdkWebServer
```

Abro `http://127.0.0.1:8081/dev-ui` y selecciono `TareaAgent`. En Codespaces abro el puerto 8081 desde **Ports**, con visibilidad **Private**, y uso la URL reenviada con `/dev-ui`. El agente sigue conectándose internamente a `127.0.0.1:8088/mcp`; no necesito publicar el puerto MCP. Agregué la [configuración de Codespaces](<.devcontainer/devcontainer.json>) con Java 21 y reenvío del puerto de la interfaz; mantengo su visibilidad en **Private**.

### Resultados y seguridad

Ejecuté 36 pruebas del servidor y cuatro del agente, sin fallos. Probé la negociación HTTP, las 14 herramientas, el CRUD de tareas, pedidos, descuento de stock y cancelación repetida. También comprobé el arranque de la interfaz ADK. Documenté los resultados en [Pasos seguidos](<PASOS_SEGUIDOS.md#laboratorio-mcp-tareas-y-supermercado>).

Mantuve la escucha en `127.0.0.1` y las claves fuera del repositorio. El booleano `confirmado` no reemplaza autenticación ni autorización: un cliente puede enviarlo sin intervención del usuario. Para publicar el servicio harían falta TLS, identidad, permisos por operación y propietario del pedido. Si una creación queda sin respuesta, compruebo la base antes de repetirla para evitar duplicados.

### Referencias

- [Spring AI: servidor MCP Streamable HTTP](https://docs.spring.io/spring-ai/reference/1.1/api/mcp/mcp-streamable-http-server-boot-starter-docs.html).
- [Model Context Protocol](https://modelcontextprotocol.io/).
- [Google ADK](https://google.github.io/adk-docs/) y [dependencias de ADK Java 1.0.0](https://github.com/google/adk-java/blob/v1.0.0/pom.xml).
- [mcp-remote](https://www.npmjs.com/package/mcp-remote).
- [GitHub Codespaces: puertos](https://docs.github.com/en/codespaces/developing-in-a-codespace/forwarding-ports-in-your-codespace) y [secrets](https://docs.github.com/en/codespaces/managing-your-codespaces/managing-your-account-specific-secrets-for-github-codespaces).
