# Registro de Entrada

Aplicación móvil Android para el **registro de ingreso a un local de eventos mediante códigos QR**.
Funciona 100 % sin internet (offline-first): base de datos local con Room (SQLite) y preferencias
con DataStore. Es el proyecto de prácticas de campo del curso.

## Estado por práctica de campo

| Práctica | Estado | Entregable |
|---|---|---|
| **PC 05** — Planificación de sprints, historias de usuario, criterios de aceptación, requerimientos no funcionales, arquitectura MVVM y modelo de datos | Terminada | `entrega/PC05-Registro-de-Entrada-Planificacion-y-Arquitectura.docx` y los documentos de `docs/` |
| **PC 06** — Sprint de interfaz y navegación en Kotlin/Jetpack Compose con validaciones y componentes principales | Terminada | Código Compose en `app/src/main/java/pe/edu/upn/registroentrada/ui/` (tema Material 3, componentes, navegación y pantallas) |
| **PC 07** — Sprint de persistencia local con DataStore, SQLite/Room y patrón Repository; evidencia de MVVM | Terminada | `data/` (Room + DataStore + repositorios), `domain/` (casos de uso y contratos), `app/schemas/…/1.json` y pruebas en verde |
| **PC 08/09** — Sprint de integración con API REST (definición de endpoints, contrato JSON y consumo con Retrofit) | Terminada | `data/remote/api/EventosApiService.kt`, `data/remote/dto/`, `di/NetworkModule.kt` y pruebas con Retrofit en verde |
| **PC 10/11** — Sprint de operaciones CRUD con API REST, manejo de errores, estados de carga y conectividad | Terminada | Operaciones GET, POST, PATCH, DELETE en `EventoRepositoryImpl`, `MonitorConectividad` y `EstadoCarga` con banner offline en `EventosScreen` |
| **PC 12** — Sprint de geolocalización con Google Maps API, permisos, privacidad y validación funcional | Terminada | Pantalla interactiva `MapaEventoScreen` con Google Maps Compose, selector de coordenadas en creación y manejo de permisos `ACCESS_FINE_LOCATION` |

## Código de la aplicación

El proyecto Android vive en la raíz del repositorio (Gradle Kotlin DSL con catálogo de
versiones en `gradle/libs.versions.toml`). Implementa las **semanas 5 (SQLite/Room y
DataStore)** y **7 (arquitectura MVVM y pruebas básicas)** del curso.

- `app/src/main/java/pe/edu/upn/registroentrada/core/` — utilidades transversales sin Android
  (`Resultado`, `Reloj`, `GeneradorId`, `FirmadorQr`, `Hasher`, `ValidadorDocumento`).
- `.../domain/` — modelos, **contratos de repositorio** y casos de uso con las reglas RN-01 a RN-15.
- `.../data/local/` — las **seis tablas de Room** (`eventos`, `tipos_entrada`, `asistentes`,
  `entradas`, `registros_acceso`, `operadores`) con sus DAO, índices, claves foráneas y siembra inicial.
- `.../data/preferences/` — **DataStore** para sesión, evento activo, puerta, tema, sonido y vibración.
- `.../data/repository/` — implementaciones del patrón **Repository** (incluye la transacción atómica
  de acceso con `Room.withTransaction`).
- `.../scanner/` — generación del QR con ZXing (el escaneo con cámara CameraX + ML Kit entra en el Sprint 3).
- `.../ui/` — tema Material 3, componentes reutilizables, navegación y pantallas:
  login, inicio (aforo en vivo), eventos, asistentes (opcional), emisión de entradas genéricas por cantidad
  con QR, detalle de la entrada con **Compartir** y **Guardar imagen (PNG)**, historial y ajustes.
- `app/src/test/` — pruebas unitarias de dominio (validadores, firma HMAC, casos de uso) con dobles en memoria.
- `app/src/androidTest/` — pruebas instrumentadas de los DAO de Room.

### Compilar, probar y verificar

```bash
# Compilar el APK de depuración (queda en app/build/outputs/apk/debug/)
./gradlew :app:assembleDebug

# Pruebas unitarias de dominio y datos (JVM)
./gradlew :app:testDebugUnitTest

# Pruebas instrumentadas de los DAO (requiere emulador o dispositivo)
./gradlew :app:connectedDebugAndroidTest
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

## Evidencias

En `docs/evidencias/` hay capturas del emulador: login, inicio, eventos, escáner, formulario de asistente,
emisión de entradas con QR, resultado de puerta, historial, compartir el QR y ajustes (tema claro y oscuro).

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

Fuera de alcance en esta versión: venta de entradas en línea, backend, lector QR físico y múltiples locales.
