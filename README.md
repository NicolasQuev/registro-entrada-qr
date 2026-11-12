# Registro de Entrada

Aplicación móvil Android nativa (Kotlin + Jetpack Compose) para el **registro y control de ingreso a eventos mediante códigos QR**.
Adopta una arquitectura híbrida *offline-first*: la validación en puerta, emisión de entradas firmadas y auditoría operan sin depender de internet sobre SQLite (**Room v3**, con migraciones `1→2` y `2→3`) y **DataStore**, complementadas con sincronización REST de eventos hacia **Cloud Firestore** (`Retrofit` + `OkHttp`) y geolocalización GPS/mapa con **Google Maps Compose**. Es el proyecto final (entrega T3) de prácticas de campo del curso.

## Estado por práctica de campo

| Práctica | Estado | Entregable |
|---|---|---|
| **PC 05** — Planificación de sprints, historias de usuario, criterios de aceptación, requerimientos no funcionales, arquitectura MVVM y modelo de datos | Terminada | `entrega/PC05-Registro-de-Entrada-Planificacion-y-Arquitectura.docx` y los documentos de `docs/` |
| **PC 06** — Sprint de interfaz y navegación en Kotlin/Jetpack Compose con validaciones y componentes principales | Terminada | Código Compose en `app/src/main/java/pe/edu/upn/registroentrada/ui/` (tema Material 3, componentes, navegación y pantallas) |
| **PC 07** — Sprint de persistencia local con DataStore, SQLite/Room y patrón Repository; evidencia de MVVM | Terminada | `data/` (Room + DataStore + repositorios), `domain/` (casos de uso y contratos), `app/schemas/…/1.json` y pruebas en verde |
| **PC 08/09** — Sprint de integración con API REST (definición de endpoints, contrato JSON y consumo con Retrofit) | Terminada | `data/remote/api/EventosApiService.kt`, `data/remote/dto/`, `di/NetworkModule.kt` y pruebas con Retrofit en verde |
| **PC 10/11** — Sprint de operaciones CRUD con API REST, manejo de errores, estados de carga y conectividad | Terminada | Operaciones GET, POST, PATCH, DELETE en `EventoRepositoryImpl`, `MonitorConectividad` y `EstadoCarga` con banner offline en `EventosScreen` |
| **PC 12** — Integración final (persistencia v3, REST Firestore, geolocalización GPS/Maps), pruebas funcionales, Lint y R8 | Terminada | `ProveedorUbicacionGps`, `ValidadorCoordenadas`, `MapaEventoScreen`, `FlujoFuncionalComposeTest`, Android Lint (`0` errores), desugaring y APK release con R8 (`23,4 MiB`) |
| **PC 13 / T3** — Informe final, producto funcional y paquete de evidencias | Terminada | `entrega/Informe_Proyecto Final_DESAP.docx`, `.pdf`, APKs compilados y carpeta `entrega/evidencias-T3/` |

## Código de la aplicación

El proyecto Android vive en la raíz del repositorio (Gradle Kotlin DSL con catálogo de
versiones en `gradle/libs.versions.toml`, `minSdk = 24`, `targetSdk = 36`, versión `1.0.0`).

- `app/src/main/java/pe/edu/upn/registroentrada/core/` — utilidades transversales
  (`Resultado`, `Reloj`, `GeneradorId`, `FirmadorQr`, `Hasher`, `ValidadorDocumento`,
  `ValidadorCoordenadas`, `MonitorConectividad`, `EstadoCarga`, `ProveedorUbicacionGps`).
- `.../domain/` — modelos, **contratos de repositorio** y casos de uso con las reglas RN-01 a RN-15.
- `.../data/local/` — las **seis tablas de Room (esquema v3)** (`eventos`, `tipos_entrada`, `asistentes`,
  `entradas`, `registros_acceso`, `operadores`), migraciones `Migracion1a2` y `Migracion2a3`, esquemas en `app/schemas/` y siembra inicial.
- `.../data/remote/` — cliente Retrofit contra **Cloud Firestore REST API v1** (`EventosApiService`, DTOs `EventoDto`).
- `.../data/preferences/` — **DataStore** para sesión, evento activo, puerta, tema, sonido y vibración.
- `.../data/repository/` — implementaciones **Repository** híbridas (Room transaccional + sincronización REST).
- `.../scanner/` — generación de QR con ZXing y escaneo en vivo con CameraX 1.4.0 + ML Kit 17.3.0.
- `.../ui/` — tema Material 3, navegación y pantallas: login, inicio, eventos (con nube), detalle de evento (`Cloud ID`, `Ver en Mapa`), formulario con GPS/Google Maps, escáner, entradas, historial y ajustes.
- `app/src/test/` — **48 pruebas unitarias (13 suites)** en JVM (dominio, firma, validadores, API REST, repositorios y ViewModels).
- `app/src/androidTest/` — **7 pruebas instrumentadas** en emulador (`AppDatabaseTest` y `FlujoFuncionalComposeTest`).

### Compilar, probar y verificar

```bash
# Pruebas unitarias en JVM (48 pruebas / 13 suites)
./gradlew :app:testDebugUnitTest

# Pruebas instrumentadas de Room y Compose UI en emulador/dispositivo (7 pruebas / 2 clases)
./gradlew :app:connectedDebugAndroidTest

# Análisis estático (0 errores, 41 advertencias)
./gradlew :app:lintDebug

# APKs debug (42,8 MiB) y release optimizado con R8 + shrinkResources (23,4 MiB)
./gradlew :app:assembleDebug :app:assembleRelease
```

El operador sembrado en el primer arranque es **admin** con PIN **1234**.

## Identidad visual

La interfaz tiene una identidad propia —código interno *Pase*: señalización de puerta y credencial de
acceso— construida sobre Material 3:

- **Color:** base grafito con un único acento ámbar de señalética; los colores de señal (verde acceso,
  rojo rechazo, ámbar atención) se reservan para el resultado de la puerta. Esquemas claro y oscuro completos.
- **Tipografía:** *Barlow Condensed* para títulos, botones y sellos de estado; *IBM Plex Mono* para códigos
  y horas. Ambas son libres (OFL) y están empaquetadas en `app/src/main/res/font/`.
- **Componentes:** `BarraSuperior` con el tick ámbar, `TarjetaApp` con banda de estado, `ChipEstado` tipo
  sello, `BarraAforo` segmentada y el detalle de la entrada con perforación de ticket.
- Todo el sistema vive en `ui/theme/` y `ui/components/`; ninguna pantalla define colores ni tipografías literales.

## Evidencias y paquete T3 (`entrega/`)

- `docs/evidencias/` — capturas del emulador desde el flujo núcleo hasta las capturas PC12 (`pc12_eventos_rest_sync.png`, `pc12_evento_detalle_cloud.png`, `pc12_evento_form_gps.png`, `pc12_mapa_google_gps.png`).
- `entrega/Informe_Proyecto Final_DESAP.docx` y `.pdf` — informe final PC13 completo.
- `entrega/RegistroEntrada-v1.0.0-debug.apk` y `RegistroEntrada-v1.0.0-release-unsigned.apk` — binarios listos para revisión e instalación.
- `entrega/evidencias-T3/` — evidencias clave organizadas junto con el resumen de pruebas.

> Nota sobre Google Maps: la vista de mapa, permisos en tiempo de ejecución y coordenadas GPS funcionan en el emulador; la descarga de teselas de mapa requiere habilitar *Maps SDK for Android* y la huella SHA-1 del certificado en Google Cloud Console para la API key declarada.

## Documentación

- `docs/PC05-planificacion.md` — problema, solución, objetivos, alcance, plan de sprints, 24 historias de
  usuario con criterios de aceptación (Dado/Cuando/Entonces), 46 requerimientos no funcionales,
  15 reglas de negocio, definición de hecho, riesgos y matriz de trazabilidad.
- `docs/PC05-arquitectura-y-modelo-de-datos.md` — arquitectura MVVM en capas, estructura de paquetes,
  mapa de navegación, modelo entidad-relación, diccionario de datos de las 6 tablas, claves de DataStore,
  contratos de repositorios, flujo transaccional del escaneo y decisiones de diseño.
- `docs/figuras/` — los cinco diagramas (SVG y PNG) referenciados por el documento.

## Cómo regenerar las figuras y el documento Word

```bash
# Figuras: SVG + PNG (requiere rsvg-convert)
python3 scripts/diagramas.py

# Documento Word (requiere pypandoc-binary y python-docx)
python3 scripts/generar_docx.py
```

El script del `.docx` arma la carátula, genera el índice con números de página reales (compila dos veces
para medir la paginación), aplica estilos, tablas y pie de página, y deja el PDF de revisión en `.build/`.

## Alcance resumido

- Eventos, tipos de entrada (general, preventa, VIP) con cupo y aforo máximo.
- Emisión de **entradas genéricas por cantidad** (sin registrar personas): cada una con **código UUID
  único** firmado (HMAC-SHA256), **hora de emisión** y **estado** (válida / usada / anulada). La base
  garantiza unicidad con un índice único sobre el código.
- Escaneo en puerta con CameraX + ML Kit, registro de cada intento (permitido o rechazado) para auditoría.
- Control de aforo en vivo, historial con filtros y exportación a CSV.
- Operadores con rol (administrador o portero) y sesión recordada en DataStore.

Los asistentes (registrar personas) quedan como módulo **opcional**; la emisión ya no los requiere.

- Sincronización REST de metadatos de eventos con Cloud Firestore (`cursos-upn-nicolas`) y geolocalización GPS/Google Maps Compose.

Fuera de alcance en esta versión: venta de entradas en línea, sincronización multi-puerta de asistencias en vivo, lector QR físico por OTG y publicación en Google Play Store.
