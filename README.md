# Registro de Entrada

Aplicación móvil Android para el **registro de ingreso a un local de eventos mediante códigos QR**.
Funciona 100 % sin internet (offline-first): base de datos local con Room (SQLite) y preferencias
con DataStore. Es el proyecto de prácticas de campo del curso.

## Estado por práctica de campo

| Práctica | Estado | Entregable |
|---|---|---|
| **PC 05** — Planificación de sprints, historias de usuario, criterios de aceptación, requerimientos no funcionales, arquitectura MVVM y modelo de datos | Terminada | `entrega/PC05-Registro-de-Entrada-Planificacion-y-Arquitectura.docx` y los documentos de `docs/` |
| **PC 06** — Sprint de interfaz y navegación en Kotlin/Jetpack Compose con validaciones y componentes principales | Pendiente | — |
| **PC 07** — Sprint de persistencia local con DataStore, SQLite/Room y patrón Repository; evidencia de MVVM | Pendiente | — |

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
- Asistentes y emisión de entradas con código QR firmado (HMAC-SHA256, sin datos personales dentro del QR).
- Escaneo en puerta con CameraX + ML Kit, registro de cada intento (permitido o rechazado) para auditoría.
- Control de aforo en vivo, historial con filtros y exportación a CSV.
- Operadores con rol (administrador o portero) y sesión recordada en DataStore.

Fuera de alcance en esta versión: venta de entradas en línea, backend, lector QR físico y múltiples locales.
