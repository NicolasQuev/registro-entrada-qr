# Resumen de Verificación y Evidencias — PC12 / PC13 (Entrega T3)

**Aplicación:** `Registro de Entrada` (`pe.edu.upn.registroentrada`, versión `1.0.0`, `minSdk = 24`, `targetSdk = 36`)  
**Arquitectura:** MVVM en tres capas (*offline-first* con Room v3 + DataStore, sincronización REST hacia Cloud Firestore y geolocalización GPS/Google Maps Compose)

## 1. Artefactos de entrega (`entrega/`)

- `Informe_Proyecto Final_DESAP.docx` — Informe final actualizado (Sprints 0–6, PC05–PC13), validado con esquema OOXML (`All validations PASSED!`).
- `Informe_Proyecto Final_DESAP.pdf` — Versión PDF (43 páginas) exportada con LibreOffice y verificada visualmente en tablas, diagrama de Gantt y figuras 4–12.
- `RegistroEntrada-v1.0.0-debug.apk` — Compilación de depuración (`44 900 697` bytes / `42,8 MiB`).
- `RegistroEntrada-v1.0.0-release-unsigned.apk` — Compilación optimizada con R8 (`isMinifyEnabled = true`, `isShrinkResources = true`, desugaring Java 8+; `24 544 590` bytes / `23,4 MiB`).
- `evidencias-T3/` — Capturas verificadas en emulador `Medium_Phone (API 37)` y cronograma Gantt actualizado (`01_eventos_rest_sync.png` a `05_gantt_pc05_pc13.png`).

## 2. Resultados de pruebas y análisis estático (tras la última edición de código)

- **Pruebas unitarias JVM (`./gradlew :app:testDebugUnitTest`):** **48/48 en verde** en **13 suites** (`0` fallos, `0` errores, `0` omitidas):
  - `FirmadorQrTest` (4), `HasherTest` (3), `ValidadorDocumentoTest` (4)
  - `EventosApiServiceTest` (2), `EventoRepositoryImplTest` (4)
  - `EliminarEventoUseCaseTest` (3), `EliminarTipoEntradaUseCaseTest` (2), `EmitirEntradaUseCaseTest` (4), `EventosUseCasesTest` (6), `IniciarSesionUseCaseTest` (4), `RegistrarAccesoUseCaseTest` (7)
  - `EscanerViewModelTest` (2), `EventoFormViewModelTest` (3)
- **Pruebas instrumentadas (`./gradlew :app:connectedDebugAndroidTest` en `Medium_Phone (AVD) - 17`):** **7/7 en verde** en **2 clases** (`0` fallos, `0` errores, `0` omitidas):
  - `AppDatabaseTest` (4 pruebas de Room DAO en memoria)
  - `FlujoFuncionalComposeTest` (3 pruebas de interfaz Compose para formulario GPS/validación de coordenadas y flujo de escaneo)
- **Análisis estático Android Lint (`./gradlew :app:lintDebug`):** **0 errores**, **41 advertencias** (`app/build/reports/lint-results-debug.html`).
- **Compilación Release y R8 (`./gradlew :app:assembleRelease`):** exitosa tras ajustes en `app/proguard-rules.pro` (`minifyEnabled = true`, `shrinkResources = true`, reducción de `42,8 MiB` a `23,4 MiB`).

## 3. Evidencias funcionales observadas en emulador (`Medium_Phone API 37`)

1. **`01_eventos_rest_sync.png` — Catálogo REST Cloud Firestore:** sincronización en vivo con `cursos-upn-nicolas` (`listarEventos`), mostrando 5 eventos con íconos de respaldo en nube, badges de estado y ajuste correcto de títulos largos en `EventosScreen`.
2. **`02_evento_detalle_cloud.png` — Detalle Cloud + Acciones:** visualización de `Cloud ID: EVT-002` junto a los botones **Ver en Mapa** y **Actualizar Nube** (`EventoDetalleScreen`).
3. **`03_evento_form_gps.png` — Formulario con GPS:** captura real mediante `ProveedorUbicacionGps` (`FusedLocationProviderClient`) obteniendo coordenadas `-8.111598`, `-79.028700`, más accesos predefinidos (`Estadio Nacional`, `Convenciones Lima`) y validación de rango en `ValidadorCoordenadas`.
4. **`04_mapa_google_gps.png` — Google Maps Compose y permisos:** encabezado accesible (`Coords: -12.03150, -77.02580`), estado `✓ Permiso de ubicación concedido (Privacidad respetada)` y controles nativos de zoom/mi ubicación en `MapaEventoScreen`.
   - *Observación técnica transparente:* En Logcat se registró `StatusCode=INVALID_ARGUMENT` / `Authorization failure` al solicitar teselas de mapa para el SHA-1 de depuración con la clave declarada, por lo que las teselas cartográficas requieren habilitar **Maps SDK for Android** y registrar la huella SHA-1 en Google Cloud Console; toda la lógica de UI, marcador, coordenadas y permisos funciona correctamente.
