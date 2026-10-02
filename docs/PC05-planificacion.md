# UNIVERSIDAD PRIVADA DEL NORTE

**Facultad de Ingeniería — Escuela Profesional de Ingeniería de Sistemas**

| | |
|---|---|
| **Curso** | ______________________ (por completar) |
| **Docente** | ______________________ (por completar) |
| **Ciclo / Sección** | ______________________ |
| **Práctica de campo** | 05 — Planificación de sprints, historias de usuario, requerimientos no funcionales y diseño de arquitectura MVVM |
| **Proyecto** | Aplicación móvil **Registro de Entrada** (registro de ingreso a un local de eventos mediante códigos QR) |
| **Integrante** | Nicolás Quevedo |
| **Ciudad** | Trujillo — Perú |
| **Año** | 2026 |

```{=openxml}
<w:p><w:r><w:br w:type="page"/></w:r></w:p>
```

# Parte 1. Planificación del producto y de los sprints

## 1.1. Descripción del problema

Un local de eventos (conciertos, conferencias, fiestas, ferias) recibe público en la puerta y hoy el control de ingreso se hace con listas impresas o con hojas de cálculo. Esto genera cuatro problemas concretos:

1. **Suplantación y falsificación**: una entrada impresa o una captura de pantalla se puede copiar y usar más de una vez; nadie verifica que la persona sea la titular ni que la entrada no haya sido usada ya.
2. **Conteo impreciso del aforo**: el personal cuenta personas "a ojo", por lo que no se sabe con certeza cuántas personas están dentro del local ni cuándo está por alcanzarse el aforo máximo autorizado.
3. **Cero trazabilidad**: si ocurre un incidente, no existe un registro confiable de quién entró, a qué hora, por cuál puerta y quién lo autorizó.
4. **Dependencia de señal**: en un local lleno la red móvil o el wifi del proveedor falla justo en la hora de mayor ingreso, y cualquier solución "en la nube" se detiene en la puerta.

## 1.2. Descripción de la solución

**Registro de Entrada** es una aplicación Android que funciona en un teléfono o tablet del personal de puerta y permite:

- Administrar los **eventos** del local y sus **tipos de entrada** (general, preventa, VIP), con cupos y aforo máximo.
- Registrar **asistentes** y **emitir entradas** cuyo código QR contiene un identificador único y firmado.
- **Escanear el QR** con la cámara del dispositivo y decidir en menos de dos segundos si el ingreso se permite o se rechaza (entrada ya usada, anulada, de otro evento, falsificada o aforo agotado).
- Registrar **cada escaneo** (aceptado o rechazado) con fecha, hora, puerta y operador, para auditoría y reportes.
- Funcionar **100 % sin internet**: toda la información vive en el dispositivo (base de datos local Room + preferencias DataStore) y no existe backend en esta versión.

> **Decisión de arquitectura desde el día uno:** el alcance se define *offline-first*. Es una elección funcional (la puerta no puede depender de la red) y académica (la Práctica de Campo 07 exige persistencia local con DataStore, SQLite/Room y patrón Repository).

## 1.3. Objetivos

**Objetivo general**

Desarrollar una aplicación Android que registre y valide el ingreso de asistentes a un local de eventos mediante códigos QR, funcionando de forma completamente local (sin conexión a internet), con una arquitectura MVVM en capas y persistencia local auditable.

**Objetivos específicos**

1. Planificar el producto en sprints semanales, con historias de usuario, criterios de aceptación y requerimientos no funcionales verificables.
2. Diseñar la arquitectura MVVM (presentación, dominio y datos) y el modelo de datos local antes de escribir código.
3. Construir la interfaz y la navegación completas en Kotlin con Jetpack Compose, con validaciones de formularios.
4. Implementar la persistencia local con Room (SQLite), DataStore y el patrón Repository, con casos de uso transaccionales.
5. Verificar el comportamiento con pruebas unitarias de dominio y datos, y con pruebas de interfaz instrumentadas.

## 1.4. Alcance

| Dentro del alcance (esta versión) | Fuera del alcance (backlog futuro) |
|---|---|
| Administración local de eventos, tipos de entrada, asistentes y operadores | Venta de entradas en línea y pasarela de pagos |
| Emisión de entradas con QR firmado y anulación | Sincronización con servidor propio / backend REST |
| Escaneo de QR con cámara y registro de accesos con auditoría | Lector de QR físico (pistola USB por OTG) |
| Control de aforo en vivo y bloqueo por aforo | Múltiples locales y sedes con personal central |
| Historial, reportes básicos y exportación a CSV | Envío de entradas por correo o WhatsApp integrado |
| Preferencias de la app (tema, sonido, vibración) en DataStore | Reconocimiento facial / verificación de identidad |
| Un solo operador activo por dispositivo a la vez | Autenticación con servidor, recuperación de contraseña |

## 1.5. Equipo, roles y forma de trabajo

| Rol Scrum | Responsabilidad | Quién |
|---|---|---|
| Product Owner | Prioriza el backlog, define y acepta los criterios de aceptación | Nicolás Quevedo (rol asumido en esta práctica) |
| Scrum Master | Facilita la planificación, elimina impedimentos, cuida el tablero | Nicolás Quevedo |
| Desarrollador Android | Implementa UI, dominio y datos; escribe pruebas | Nicolás Quevedo |

**Supuestos de trabajo** (si el proyecto se presenta en equipo de 3, los roles se reparten y la velocidad se multiplica):

- Sprint de **una semana** (coincide con la entrega semanal de la práctica de campo).
- Capacidad comprometida: **24 a 27 puntos de historia por sprint**, calculados con un equipo de 3 integrantes a 8 horas semanales y velocidad inicial asumida de 26 puntos.
- Herramientas: Git + GitHub para el control de versiones, tablero de GitHub Projects (columnas: *Backlog*, *En sprint*, *En curso*, *En revisión*, *Hecho*), y este documento como fuente de verdad del backlog.
- Escala de estimación: Fibonacci modificada (1, 2, 3, 5, 8 puntos). Se usa *planning poker* informal y la referencia "3 puntos ≈ media jornada de trabajo".

## 1.6. Plan de sprints

Los cinco sprints del producto están alineados con las prácticas de campo del curso: la planificación se entrega en la **PC 05**, el sprint de interfaz y navegación se ejecuta en la **PC 06** y el sprint de persistencia local en la **PC 07**.

| Sprint | Semana / entrega | Meta del sprint (*sprint goal*) | Puntos | Historias comprometidas | Evidencia que se entrega |
|---|---|---|---|---|---|
| **Sprint 0** | Semana 5 — **PC 05** | Dejar el producto definido: backlog priorizado, criterios de aceptación, requerimientos no funcionales, arquitectura MVVM y modelo de datos local aprobados | 0 (trabajo de diseño) | Documento de planificación, diagramas de arquitectura y modelo entidad-relación, proyecto Android creado y versionado en Git | Este documento + repositorio en GitHub |
| **Sprint 1** | Semana 6 — **PC 06** | Tener la aplicación navegable de punta a punta en Jetpack Compose, con datos simulados, tema Material 3 y formularios validados | 24 | HU-01, HU-02, HU-05, HU-08, HU-10, HT-01 | Capturas del emulador, video corto del flujo, código del sprint en `main` |
| **Sprint 2** | Semana 7 — **PC 07** | Reemplazar los datos simulados por persistencia real: Room (SQLite) + DataStore + patrón Repository, con casos de uso y pruebas | 27 | HU-04, HU-07, HU-11, HU-23, HT-03, HT-04 | Evidencia de MVVM (capa por capa), esquema exportado de Room, pruebas unitarias e instrumentadas en verde |
| **Sprint 3** | Semana 8 | Registrar el ingreso real con la cámara: escaneo QR, validación de firma, transacción de acceso y control de aforo | 27 | HU-12, HU-13, HU-15, HU-16, HU-17, HU-18 | Video del escaneo en emulador y equipo físico, registro de accesos en la base |
| **Sprint 4** | Semana 9 | Cerrar la administración y la explotación de la información: historial, reportes, anulación de entradas y exportación | 22 | HU-03, HU-06, HU-09, HU-14, HU-19, HU-20, HU-21 | Reporte CSV exportado, pantallas de historial y filtros |
| **Sprint 5** | Semana 10 | Administración de operadores, respaldo local, accesibilidad y pulido final de la entrega | 16 | HU-22, HU-24, HT-02, HT-05 | APK final, manual de usuario breve, lista de verificación de RNF |

**Total del backlog: 116 puntos de historia** (99 puntos en historias de usuario + 17 puntos en historias técnicas).

### 1.6.1. Detalle del Sprint 1 (el que se ejecuta en la PC 06)

- **Sprint goal**: la aplicación se puede recorrer completa en el emulador con datos de ejemplo, sin base de datos todavía, y cada formulario valida sus campos antes de continuar.
- **Selección de historias**: HU-01 (crear evento), HU-02 (listar y buscar eventos), HU-05 (registrar asistente), HU-08 (ver el QR de una entrada), HU-10 (iniciar sesión como operador con PIN) y HT-01 (estructura del proyecto y tema Material 3).
- **Entregable**: pantallas implementadas en Compose, navegación funcionando, `UiState` por pantalla y validaciones visibles; el origen de datos son repositorios "falsos" en memoria (`RepositorioEventosFalso`), para no bloquear el sprint con la capa de datos.
- **Riesgo asumido**: los datos no sobreviven al cierre de la app. Es intencional: la persistencia entra en el Sprint 2.

### 1.6.2. Detalle del Sprint 2 (el que se ejecuta en la PC 07)

- **Sprint goal**: toda la información de eventos, entradas y accesos queda guardada en el dispositivo y sobrevive al cierre de la aplicación, con el patrón Repository de por medio.
- **Selección de historias**: HU-04 (tipos de entrada), HU-07 (emitir entrada con QR), HU-11 (seleccionar evento activo y puerta), HU-23 (preferencias), HT-03 (base de datos Room, DAOs y migraciones) y HT-04 (DataStore para preferencias).
- **Entregable (evidencia de arquitectura MVVM)**: diagrama de capas implementado, paquetes `data/`, `domain/` y `ui/` reales, `AppDatabase` con esquema exportado (`schemas/`), DAOs con consultas verificadas, repositorios con mappers entidad↔dominio, preferencias en DataStore y pruebas que pasan.
- **Criterio de aceptación del sprint**: cerrar la app, volver a abrirla y ver el mismo evento, el mismo asistente y la misma entrada con su código QR.

## 1.7. Historia del producto vista por el usuario (épicas)

| Épica | Nombre | Valor de negocio | Puntos |
|---|---|---|---|
| EP-01 | Gestión de eventos y tipos de entrada | Permite al local organizar su cartelera y sus cupos sin depender de un sistema externo | 16 |
| EP-02 | Asistentes y emisión de entradas | Reemplaza la lista impresa por una entrada digital verificable | 22 |
| EP-03 | Acceso y escaneo en puerta | Es el corazón del producto: decide el ingreso en menos de 2 segundos y sin internet | 29 |
| EP-04 | Aforo y control de capacidad | Evita multas y accidentes por exceso de aforo | 8 |
| EP-05 | Historial y reportes | Convierte los escaneos en información para el organizador | 11 |
| EP-06 | Administración y respaldo | Permite operar con varios porteros y no perder la información | 13 |

# Parte 2. Historias de usuario y criterios de aceptación

**Formato**: cada historia se escribe como `Como <rol>, quiero <funcionalidad> para <beneficio>`, con identificador `HU-xx`, épica, prioridad MoSCoW (Must / Should / Could / Won't), estimación en puntos y sprint asignado. Debajo de cada historia se listan sus **criterios de aceptación en formato Dado / Cuando / Entonces**, que son los que se verifican para darla por terminada (`Hecho`).

## EP-01 · Gestión de eventos y tipos de entrada

### HU-01 · Crear un evento

| Campo | Valor |
|---|---|
| **Historia** | Como **administrador del local**, quiero **crear un evento con su nombre, lugar, fecha, hora y aforo máximo**, para **tenerlo disponible y poder emitir entradas para él**. |
| **Prioridad** | Must |
| **Estimación** | 5 puntos |
| **Sprint** | Sprint 1 (interfaz y validaciones con datos simulados) → se cierra con persistencia en el Sprint 2 |

**Criterios de aceptación**

- **Dado** que estoy en la pantalla "Eventos", **cuando** toco el botón "+ Nuevo evento", **entonces** se abre el formulario de creación con los campos nombre, lugar, fecha de inicio, fecha de fin y aforo máximo.
- **Dado** que dejo el nombre vacío, **cuando** intento guardar, **entonces** veo el mensaje "El nombre del evento es obligatorio" sobre el campo y el botón de guardar permanece deshabilitado.
- **Dado** que escribo un aforo máximo igual a cero, negativo o con letras, **cuando** confirmo el campo, **entonces** la app muestra "El aforo debe ser un número mayor a 0" y no permite guardar.
- **Dado** que la fecha de fin es anterior a la fecha de inicio, **cuando** intento guardar, **entonces** veo "La fecha de fin no puede ser anterior a la fecha de inicio".
- **Dado** que completo todos los campos válidos, **cuando** guardo, **entonces** el evento aparece en la lista de eventos y se muestra un aviso "Evento creado" (Snackbar).

### HU-02 · Consultar y buscar eventos

| Campo | Valor |
|---|---|
| **Historia** | Como **administrador del local**, quiero **ver la lista de eventos y buscar por nombre**, para **encontrar rápido el evento con el que voy a trabajar**. |
| **Prioridad** | Must |
| **Estimación** | 3 puntos |
| **Sprint** | Sprint 1 |

**Criterios de aceptación**

- **Dado** que existen eventos registrados, **cuando** abro la pantalla "Eventos", **entonces** veo una tarjeta por evento con nombre, lugar, fecha y aforo.
- **Dado** que hay eventos de distintas fechas, **cuando** entran a la lista, **entonces** se muestran ordenados del más próximo al más antiguo y los eventos ya finalizados aparecen con la etiqueta "Finalizado".
- **Dado** que escribo "concierto" en la barra de búsqueda, **cuando** se actualiza el texto, **entonces** la lista se filtra en tiempo real y solo muestra los eventos cuyo nombre contiene ese texto (sin distinguir mayúsculas).
- **Dado** que la búsqueda no encuentra coincidencias, **cuando** termino de escribir, **entonces** veo el estado vacío con el mensaje "No se encontraron eventos" y un botón "Limpiar búsqueda".
- **Dado** que no existe ningún evento registrado, **cuando** abro la pantalla, **entonces** veo el estado vacío con el mensaje "Aún no hay eventos" y el botón "Nuevo evento".

### HU-03 · Editar o cancelar un evento

| Campo | Valor |
|---|---|
| **Historia** | Como **administrador del local**, quiero **editar los datos de un evento o marcarlo como cancelado**, para **corregir errores y evitar que se emitan entradas para un evento que ya no se realizará**. |
| **Prioridad** | Should |
| **Estimación** | 3 puntos |
| **Sprint** | Sprint 4 |

**Criterios de aceptación**

- **Dado** que abro el detalle de un evento, **cuando** toco "Editar", **entonces** se abre el formulario con los datos precargados y las mismas validaciones de HU-01.
- **Dado** que el aforo máximo es menor a la cantidad de personas ya registradas, **cuando** confirmo la edición, **entonces** la app advierte "El nuevo aforo es menor a los ingresos registrados" y pide confirmación explícita.
- **Dado** que un evento tiene entradas emitidas, **cuando** lo marco como "Cancelado", **entonces** se pide confirmación, el evento queda con la etiqueta "Cancelado" y no puede seleccionarse como evento activo de puerta.
- **Dado** que un evento está cancelado, **cuando** intento escanear una entrada de ese evento, **entonces** el resultado es "Rechazado: evento cancelado" y el intento queda en el historial.

### HU-04 · Definir tipos de entrada con cupo y precio

| Campo | Valor |
|---|---|
| **Historia** | Como **administrador del local**, quiero **definir tipos de entrada (general, preventa, VIP) con su precio y cupo**, para **controlar cuántas entradas de cada tipo se pueden emitir**. |
| **Prioridad** | Must |
| **Estimación** | 5 puntos |
| **Sprint** | Sprint 2 |

**Criterios de aceptación**

- **Dado** que estoy en el detalle de un evento, **cuando** agrego un tipo de entrada, **entonces** puedo ingresar nombre, precio (≥ 0) y cupo (≥ 1).
- **Dado** que ya existe un tipo llamado "VIP" en el evento, **cuando** intento crear otro con el mismo nombre, **entonces** veo "Ya existe un tipo de entrada con ese nombre" y no se guarda (restricción única `eventoId + nombre`).
- **Dado** que un tipo de entrada tiene cupo 50 y ya se emitieron 50 entradas de ese tipo, **cuando** intento emitir una más, **entonces** la app bloquea la emisión con "Se agotó el cupo de este tipo de entrada".
- **Dado** que elimino un tipo de entrada sin entradas emitidas, **cuando** confirmo, **entonces** desaparece de la lista; **y dado** que sí tiene entradas emitidas, **entonces** la app no permite eliminarlo y ofrece "Anular las entradas vigentes primero".

## EP-02 · Asistentes y emisión de entradas

### HU-05 · Registrar un asistente

| Campo | Valor |
|---|---|
| **Historia** | Como **administrador del local**, quiero **registrar a un asistente con su documento, nombres y correo**, para **emitirle una entrada y poder identificarlo en la puerta**. |
| **Prioridad** | Must |
| **Estimación** | 5 puntos |
| **Sprint** | Sprint 1 |

**Criterios de aceptación**

- **Dado** que estoy en la pantalla "Asistentes", **cuando** toco "+ Nuevo asistente", **entonces** veo el formulario con documento, nombres, apellidos, correo (opcional) y teléfono (opcional).
- **Dado** que escribo un documento de 8 dígitos numéricos (DNI), **cuando** confirmo, **entonces** el campo es válido; **y dado** que escribo letras o menos de 8 dígitos, **entonces** veo "El documento debe tener 8 dígitos numéricos".
- **Dado** que escribo un correo sin `@` o sin dominio, **cuando** confirmo el campo, **entonces** veo "Ingresa un correo válido (ejemplo@dominio.com)".
- **Dado** que el documento ya está registrado en el dispositivo, **cuando** intento guardar, **entonces** veo "Ese documento ya está registrado para <nombres del asistente>" con la opción "Ver asistente".
- **Dado** que todos los campos obligatorios son válidos, **cuando** guardo, **entonces** el asistente aparece en la lista y se muestra "Asistente registrado".

### HU-06 · Buscar un asistente y ver su historial

| Campo | Valor |
|---|---|
| **Historia** | Como **administrador del local**, quiero **buscar asistentes por documento o apellido y ver sus entradas**, para **resolver consultas en la puerta**. |
| **Prioridad** | Should |
| **Estimación** | 3 puntos |
| **Sprint** | Sprint 4 |

**Criterios de aceptación**

- **Dado** que existen asistentes registrados, **cuando** escribo un documento o apellido en la barra de búsqueda, **entonces** la lista filtra en tiempo real por documento, nombres o apellidos.
- **Dado** que abro la ficha de un asistente, **cuando** carga la pantalla, **entonces** veo sus datos y la lista de sus entradas con evento, tipo de entrada y estado (Válida / Usada / Anulada).
- **Dado** que un asistente no tiene entradas, **cuando** abro su ficha, **entonces** veo "Sin entradas registradas" y el botón "Emitir entrada".

### HU-07 · Emitir una entrada con código QR

| Campo | Valor |
|---|---|
| **Historia** | Como **administrador del local**, quiero **emitir una entrada para un asistente y un evento**, para **que ingrese presentando su código QR**. |
| **Prioridad** | Must |
| **Estimación** | 8 puntos |
| **Sprint** | Sprint 2 |

**Criterios de aceptación**

- **Dado** que elijo un evento con tipos de entrada definidos, **cuando** toco "Emitir entrada", **entonces** la app me pide seleccionar el asistente y el tipo de entrada.
- **Dado** que confirmo la emisión, **cuando** termina la operación, **entonces** se crea una entrada con estado **VÁLIDA**, un código único de 36 caracteres (UUID v4), su firma y la fecha de emisión.
- **Dado** que el asistente ya tiene una entrada en estado **VÁLIDA** para el mismo evento, **cuando** intento emitir otra, **entonces** la app lo impide con "Este asistente ya tiene una entrada válida para el evento" y ofrece ver la entrada existente.
- **Dado** que se emite la entrada, **cuando** abro su detalle, **entonces** veo el código QR renderizado, el código en texto para dictarlo y la opción "Compartir".
- **Dado** que la emisión ocurre en el dispositivo, **cuando** reviso la entrada emitida, **entonces** su código y firma no coinciden con los de ninguna otra entrada (unicidad garantizada por índice único en la base de datos).
- **Dado** que el aforo máximo del evento ya está completo con entradas válidas, **cuando** intento emitir una entrada nueva, **entonces** la app advierte "El aforo del evento ya está completo" y pide confirmación para emitir en sobrecupo.

### HU-08 · Ver y compartir la entrada (QR en pantalla)

| Campo | Valor |
|---|---|
| **Historia** | Como **administrador del local**, quiero **ver el código QR de una entrada en pantalla y compartirlo**, para **entregárselo al asistente sin imprimir nada**. |
| **Prioridad** | Must |
| **Estimación** | 3 puntos |
| **Sprint** | Sprint 1 (interfaz; el QR se genera desde datos de ejemplo y en el Sprint 2 se genera desde la base de datos) |

**Criterios de aceptación**

- **Dado** que abro el detalle de una entrada, **cuando** carga la pantalla, **entonces** el QR se dibuja en un área de al menos 250 dp, con fondo claro y buen contraste, y el código visible en texto monoespaciado.
- **Dado** que el QR está en pantalla, **cuando** aumento el brillo al máximo automáticamente, **entonces** la pantalla mantiene el brillo alto mientras está visible y lo restaura al salir.
- **Dado** que toco "Compartir", **cuando** se abre la hoja del sistema, **entonces** puedo compartir la imagen del QR y el texto "Entrada para <evento> — código <código>".
- **Dado** que la entrada está usada o anulada, **cuando** abro su detalle, **entonces** el QR se muestra atenuado con la marca de agua "USADA" o "ANULADA".

### HU-09 · Anular una entrada

| Campo | Valor |
|---|---|
| **Historia** | Como **administrador del local**, quiero **anular una entrada emitida por error o reportada como perdida**, para **que ya no permita el ingreso**. |
| **Prioridad** | Should |
| **Estimación** | 3 puntos |
| **Sprint** | Sprint 4 |

**Criterios de aceptación**

- **Dado** que abro una entrada en estado **VÁLIDA**, **cuando** toco "Anular", **entonces** se pide confirmación indicando el nombre del asistente y el evento.
- **Dado** que confirmo la anulación, **cuando** termina la operación, **entonces** la entrada queda en estado **ANULADA**, no cuenta para el aforo ocupado y su QR deja de ser aceptado.
- **Dado** que una entrada ya fue usada, **cuando** intento anularla, **entonces** la app lo impide con "No se puede anular una entrada ya usada; registra una incidencia".

## EP-03 · Acceso y escaneo en puerta

### HU-10 · Iniciar sesión como operador con PIN

| Campo | Valor |
|---|---|
| **Historia** | Como **portero u operador de puerta**, quiero **iniciar sesión con mi usuario y PIN**, para **que todo ingreso quede registrado a mi nombre**. |
| **Prioridad** | Must |
| **Estimación** | 5 puntos |
| **Sprint** | Sprint 1 (pantalla y validaciones) → validación real contra los operadores guardados en el Sprint 2 |

**Criterios de aceptación**

- **Dado** que abro la app y no hay sesión iniciada, **cuando** carga la aplicación, **entonces** se muestra la pantalla de inicio de sesión con el listado de operadores activos.
- **Dado** que selecciono un operador, **cuando** ingreso un PIN incorrecto, **entonces** veo "PIN incorrecto, intento 1 de 3" y el acceso no se concede.
- **Dado** que fallo el PIN tres veces consecutivas, **cuando** intento un cuarto intento, **entonces** el acceso se bloquea por 60 segundos y se muestra el tiempo restante.
- **Dado** que ingreso el PIN correcto, **cuando** se valida, **entonces** entro a la pantalla de inicio y el nombre del operador aparece en la barra superior.
- **Dado** que hay una sesión activa, **cuando** cierro y vuelvo a abrir la app, **entonces** no se vuelve a pedir el PIN (la sesión se recuerda en las preferencias del dispositivo).
- **Dado** que el operador no tiene rol de administrador, **cuando** navego por la app, **entonces** no veo las opciones de administración (eventos, asistentes, operadores) ni puedo acceder a ellas por navegación directa.

### HU-11 · Seleccionar el evento activo y la puerta

| Campo | Valor |
|---|---|
| **Historia** | Como **portero**, quiero **elegir el evento que estoy atendiendo y el identificador de mi puerta**, para **que los ingresos queden registrados en el evento y la puerta correctos**. |
| **Prioridad** | Must |
| **Estimación** | 3 puntos |
| **Sprint** | Sprint 2 |

**Criterios de aceptación**

- **Dado** que inicio sesión, **cuando** no hay evento activo seleccionado, **entonces** la app pide elegir un evento de la lista de eventos programados o en curso.
- **Dado** que elijo un evento, **cuando** se confirma, **entonces** el evento activo se guarda en las preferencias del dispositivo y se muestra en la barra superior.
- **Dado** que hay un evento activo guardado, **cuando** cierro y abro la app, **entonces** el mismo evento y la misma puerta siguen seleccionados.
- **Dado** que cambio el evento activo, **cuando** estoy en la pantalla de escaneo, **entonces** el cambio se aplica al siguiente escaneo y queda avisado con un mensaje en pantalla.

### HU-12 · Escanear el QR y validar la entrada

| Campo | Valor |
|---|---|
| **Historia** | Como **portero**, quiero **escanear el código QR de una entrada con la cámara**, para **saber de inmediato si la persona puede ingresar**. |
| **Prioridad** | Must |
| **Estimación** | 8 puntos |
| **Sprint** | Sprint 3 |

**Criterios de aceptación**

- **Dado** que estoy en la pantalla de escaneo, **cuando** apunto la cámara a un QR válido del evento activo, **entonces** en menos de 2 segundos veo el resultado "PERMITIDO" con el nombre del asistente, el tipo de entrada y el tipo "GENERAL"/"VIP".
- **Dado** que escaneo un QR cuyo código no existe en la base de datos local, **cuando** se procesa, **entonces** veo "RECHAZADO — entrada no encontrada en este dispositivo".
- **Dado** que escaneo un QR cuya firma no coincide con la clave del dispositivo, **cuando** se procesa, **entonces** veo "RECHAZADO — código falsificado o de otro dispositivo" y el intento queda registrado.
- **Dado** que escaneo una entrada **VÁLIDA** de otro evento, **cuando** se procesa, **entonces** veo "RECHAZADO — la entrada es para el evento <nombre>" y no se registra el ingreso.
- **Dado** que escaneo un QR ya escaneado en los últimos 5 segundos (rebote de la cámara), **cuando** se procesa, **entonces** la app ignora la repetición y no registra un segundo intento.
- **Dado** que no hay cámara disponible o el permiso está denegado, **cuando** abro la pantalla de escaneo, **entonces** veo "Necesito permiso de cámara para escanear" con un botón "Abrir ajustes" y la opción de validar por código manual (HU-15).

### HU-13 · Recibir respuesta clara e inmediata del escaneo

| Campo | Valor |
|---|---|
| **Historia** | Como **portero**, quiero **un aviso claro e inmediato del resultado del escaneo**, para **decidir el ingreso sin mirar la pantalla con detalle**. |
| **Prioridad** | Must |
| **Estimación** | 3 puntos |
| **Sprint** | Sprint 3 |

**Criterios de aceptación**

- **Dado** que el resultado es permitido, **cuando** aparece en pantalla, **entonces** la pantalla se pinta de verde con un icono de check, el texto "PERMITIDO" en tipografía grande y un sonido corto de confirmación (si el sonido está activado en las preferencias).
- **Dado** que el resultado es rechazado, **cuando** aparece en pantalla, **entonces** la pantalla se pinta de rojo con icono de alerta, el motivo del rechazo en texto y una vibración (si está activada en las preferencias).
- **Dado** que el sonido y la vibración están desactivados en los ajustes, **cuando** ocurre un escaneo, **entonces** el resultado se muestra solo con color, icono y texto.
- **Dado** que hay conexión a internet o no, **cuando** ocurre el escaneo, **entonces** el resultado se muestra igual (la validación es local).

### HU-14 · Registrar el ingreso sin conexión a internet

| Campo | Valor |
|---|---|
| **Historia** | Como **portero**, quiero **que la app funcione sin internet en la puerta**, para **no detener el ingreso cuando la red del local falla**. |
| **Prioridad** | Must |
| **Estimación** | 2 puntos |
| **Sprint** | Sprint 4 (verificación formal del requisito dentro del sprint de cierre) |

**Criterios de aceptación**

- **Dado** que el dispositivo está en modo avión, **cuando** registro un ingreso, **entonces** la operación se completa igual y el registro queda guardado.
- **Dado** que la app no tiene permiso de internet declarado en el manifiesto, **cuando** reviso los registros del sistema, **entonces** no hay intentos de conexión de la aplicación.
- **Dado** que el dispositivo se queda sin batería durante la operación de puerta, **cuando** se vuelve a encender la app, **entonces** los accesos registrados antes de apagarse siguen en el historial (transacciones confirmadas en la base de datos).

### HU-15 · Validar una entrada por código manual

| Campo | Valor |
|---|---|
| **Historia** | Como **portero**, quiero **escribir el código de la entrada a mano cuando el QR no se puede leer**, para **no dejar varada a la persona en la puerta**. |
| **Prioridad** | Should |
| **Estimación** | 3 puntos |
| **Sprint** | Sprint 3 |

**Criterios de aceptación**

- **Dado** que estoy en la pantalla de escaneo, **cuando** toco "Ingresar código manualmente", **entonces** se abre un campo de texto con teclado alfanumérico.
- **Dado** que ingreso un código exacto existente, **cuando** confirmo, **entonces** se ejecuta la misma validación que el escaneo y se muestra el mismo resultado.
- **Dado** que ingreso un código inexistente, **cuando** confirmo, **entonces** veo "No se encontró ninguna entrada con ese código" y el intento queda en el historial.
- **Dado** que el código ingresado corresponde a una entrada de otro evento, **cuando** confirmo, **entonces** el resultado es rechazado con el motivo "entrada de otro evento".

### HU-16 · Impedir que una entrada se use dos veces

| Campo | Valor |
|---|---|
| **Historia** | Como **administrador del local**, quiero **que una entrada solo permita un ingreso**, para **evitar que la misma entrada se comparta con otra persona**. |
| **Prioridad** | Must |
| **Estimación** | 5 puntos |
| **Sprint** | Sprint 3 |

**Criterios de aceptación**

- **Dado** que una entrada está en estado **VÁLIDA** y se escanea correctamente en el evento activo, **cuando** se confirma el ingreso, **entonces** la entrada pasa a estado **USADA** y se guarda la fecha y hora de uso.
- **Dado** que la misma entrada se escanea otra vez, **cuando** se procesa, **entonces** el resultado es "RECHAZADO — la entrada ya fue usada a las hh:mm del dd/MM/yyyy" y no se cuentan dos ingresos para el aforo.
- **Dado** que una entrada está anulada, **cuando** se escanea, **entonces** el resultado es "RECHAZADO — entrada anulada" y no cambia su estado.
- **Dado** que ocurren dos escaneos válidos simultáneos del mismo código, **cuando** se procesan, **entonces** solo uno se registra como permitido y el segundo es rechazado porque la lectura y la actualización ocurren dentro de la misma transacción de base de datos.

## EP-04 · Aforo y control de capacidad

### HU-17 · Ver el aforo en vivo

| Campo | Valor |
|---|---|
| **Historia** | Como **administrador del local**, quiero **ver cuántas personas ingresaron y cuánto aforo queda disponible**, para **tomar decisiones durante el evento**. |
| **Prioridad** | Must |
| **Estimación** | 3 puntos |
| **Sprint** | Sprint 3 |

**Criterios de aceptación**

- **Dado** que hay un evento activo, **cuando** abro la pantalla de inicio, **entonces** veo una barra de aforo con "Ocupado: N / Máximo: M" y el porcentaje consumido.
- **Dado** que se registra un ingreso permitido, **cuando** vuelvo a la pantalla de inicio, **entonces** el contador de ocupados se actualizó sin necesidad de refrescar manualmente.
- **Dado** que el aforo supera el 80 %, **cuando** veo la barra, **entonces** se muestra en color de advertencia; **y dado** que llega al 100 %, **entonces** se muestra en rojo con el texto "Aforo completo".
- **Dado** que quiero el detalle, **cuando** entro a "Ver detalle", **entonces** veo ocupados, entradas válidas emitidas, entradas usadas, intentos rechazados y cupo libre por tipo de entrada.

### HU-18 · Bloquear el ingreso cuando el aforo está completo

| Campo | Valor |
|---|---|
| **Historia** | Como **administrador del local**, quiero **que la app impida registrar más ingresos cuando se alcanza el aforo máximo**, para **cumplir con la capacidad autorizada del local**. |
| **Prioridad** | Must |
| **Estimación** | 5 puntos |
| **Sprint** | Sprint 3 |

**Criterios de aceptación**

- **Dado** que la cantidad de ingresos permitidos alcanzó el aforo máximo, **cuando** se escanea una entrada válida, **entonces** el resultado es "RECHAZADO — aforo completo" y la entrada conserva su estado **VÁLIDA** (no se quema).
- **Dado** que el administrador está presente, **cuando** toca "Autorizar excepción" e ingresa su PIN, **entonces** el ingreso se registra como permitido y en el registro queda la observación "Excepción autorizada por <operador>".
- **Dado** que el aforo está completo, **cuando** el administrador autoriza una excepción, **entonces** el contador de ocupados puede superar el máximo y la barra lo muestra como "Sobrecupo: +N".
- **Dado** que se anula una entrada usada no se libera aforo, pero **cuando** se registra una **salida** (funcionalidad futura) o se corrige un registro, **entonces** el aforo se recalcula sobre los registros permitidos vigentes.

## EP-05 · Historial y reportes

### HU-19 · Consultar el historial de accesos

| Campo | Valor |
|---|---|
| **Historia** | Como **administrador del local**, quiero **ver todos los intentos de acceso con sus filtros**, para **auditar qué pasó en la puerta**. |
| **Prioridad** | Should |
| **Estimación** | 5 puntos |
| **Sprint** | Sprint 4 |

**Criterios de aceptación**

- **Dado** que existen registros de acceso, **cuando** abro "Historial", **entonces** veo la lista ordenada del más reciente al más antiguo con hora, resultado, puerta, código escaneado y operador.
- **Dado** que aplico los filtros de fecha, puerta, resultado u operador, **cuando** los confirmo, **entonces** la lista se actualiza solo con los registros que cumplen todos los filtros activos.
- **Dado** que hay más de 50 registros, **cuando** hago scroll, **entonces** se cargan por páginas de 50 sin bloquear la interfaz y sin perder la posición.
- **Dado** que un registro corresponde a un rechazo, **cuando** lo abro, **entonces** veo el motivo completo y, si el código existe, el nombre del asistente y el evento.
- **Dado** que no hay registros para los filtros elegidos, **cuando** aplico, **entonces** veo el estado vacío "No hay registros para estos filtros".
- **Dado** que reviso el historial, **cuando** intento editar o eliminar un registro, **entonces** la app no ofrece esas acciones (el historial es inmutable).

### HU-20 · Ver el resumen del evento

| Campo | Valor |
|---|---|
| **Historia** | Como **organizador del evento**, quiero **ver un resumen de resultados al terminar la jornada**, para **saber cómo se comportó el ingreso**. |
| **Prioridad** | Should |
| **Estimación** | 3 puntos |
| **Sprint** | Sprint 4 |

**Criterios de aceptación**

- **Dado** que el evento tiene registros, **cuando** abro "Reportes", **entonces** veo el total de entradas emitidas, el total de ingresos permitidos, el total de rechazos agrupados por motivo y el porcentaje de ocupación del aforo.
- **Dado** que el evento tuvo ingresos, **cuando** veo el reporte, **entonces** se muestra un gráfico de barras simple de ingresos por hora.
- **Dado** que el evento no tiene registros, **cuando** abro "Reportes", **entonces** veo "Todavía no hay datos para este evento".
- **Dado** que hay varios operadores, **cuando** veo el resumen, **entonces** se muestra el desglose de ingresos por operador y por puerta.

### HU-21 · Exportar el reporte a CSV

| Campo | Valor |
|---|---|
| **Historia** | Como **organizador del evento**, quiero **exportar los registros de acceso a un archivo CSV**, para **trabajarlos en Excel y entregarlos al cliente del local**. |
| **Prioridad** | Could |
| **Estimación** | 3 puntos |
| **Sprint** | Sprint 4 |

**Criterios de aceptación**

- **Dado** que estoy en "Reportes", **cuando** toco "Exportar CSV", **entonces** se pide confirmación porque el archivo contiene datos personales.
- **Dado** que confirmo la exportación, **cuando** termina, **entonces** se genera un archivo `accesos_<evento>_<fecha>.csv` con encabezados y se ofrece compartirlo con el sistema.
- **Dado** que el CSV contiene nombres, documentos y correos, **cuando** se exporta, **entonces** los datos viajan solo en el archivo local elegido por el usuario y no se envían a ningún servidor.
- **Dado** que el archivo tiene 20 000 filas, **cuando** se exporta, **entonces** la operación se hace en segundo plano, la interfaz sigue respondiendo y se muestra una barra de progreso.

## EP-06 · Administración y respaldo

### HU-22 · Gestionar operadores y roles

| Campo | Valor |
|---|---|
| **Historia** | Como **administrador del local**, quiero **crear operadores, asignarles un rol y activarlos o desactivarlos**, para **controlar quién puede registrar ingresos y quién puede administrar**. |
| **Prioridad** | Could |
| **Estimación** | 5 puntos |
| **Sprint** | Sprint 5 |

**Criterios de aceptación**

- **Dado** que estoy en "Operadores", **cuando** creo un operador, **entonces** ingreso nombre, usuario único, PIN (mínimo 4 dígitos) y rol (Administrador / Portero).
- **Dado** que el usuario ya existe, **cuando** guardo, **entonces** veo "Ese usuario ya está en uso".
- **Dado** que creo el operador, **cuando** reviso la base de datos, **entonces** el PIN se guarda como hash SHA-256 con sal y nunca en texto plano.
- **Dado** que desactivo a un operador, **cuando** ese operador intenta iniciar sesión, **entonces** no aparece en la lista de operadores activos y su sesión guardada se invalida.
- **Dado** que soy el único administrador, **cuando** intento desactivarme o cambiar mi rol, **entonces** la app lo impide con "Debe existir al menos un administrador activo".

### HU-23 · Configurar las preferencias de la aplicación

| Campo | Valor |
|---|---|
| **Historia** | Como **operador**, quiero **configurar el tema, el sonido y la vibración del escaneo**, para **trabajar cómodo en la puerta**. |
| **Prioridad** | Could |
| **Estimación** | 3 puntos |
| **Sprint** | Sprint 2 |

**Criterios de aceptación**

- **Dado** que estoy en "Ajustes", **cuando** cambio el tema a claro u oscuro, **entonces** la interfaz cambia de inmediato y la preferencia se conserva al cerrar y abrir la app.
- **Dado** que el sistema está en modo oscuro y elijo "Seguir al sistema", **cuando** cambio el tema del teléfono, **entonces** la app acompaña el cambio.
- **Dado** que desactivo el sonido de escaneo, **cuando** escaneo un QR, **entonces** no suena ninguna confirmación.
- **Dado** que cierro la aplicación y la vuelvo a abrir, **cuando** entro a Ajustes, **entonces** todas las preferencias elegidas siguen marcadas tal como las dejé.
- **Dado** que las preferencias se guardan con DataStore, **cuando** reviso el almacenamiento del dispositivo, **entonces** las preferencias están en `datastore/ajustes.preferences_pb` y no en tablas de la base de datos relacional.

### HU-24 · Respaldar y restaurar la información local

| Campo | Valor |
|---|---|
| **Historia** | Como **administrador del local**, quiero **exportar e importar un respaldo de toda la información**, para **mover los datos a otro dispositivo o recuperarlos si se pierde el teléfono**. |
| **Prioridad** | Could |
| **Estimación** | 5 puntos |
| **Sprint** | Sprint 5 |

**Criterios de aceptación**

- **Dado** que estoy en "Ajustes → Respaldo", **cuando** toco "Crear respaldo", **entonces** se genera un archivo `.json` con eventos, tipos de entrada, asistentes, entradas, operadores y registros de acceso, más la fecha y la versión del esquema.
- **Dado** que el respaldo se crea, **cuando** lo reviso, **entonces** no contiene ningún PIN en texto plano.
- **Dado** que importo un respaldo en un dispositivo con datos, **cuando** confirmo "Reemplazar datos actuales", **entonces** la app avisa que la operación es irreversible y, si acepto, reemplaza los datos dentro de una sola transacción.
- **Dado** que el archivo de respaldo está corrupto o tiene otra versión de esquema, **cuando** intento importarlo, **entonces** veo "El archivo no es válido para esta versión de la aplicación" y no se modifica nada.

# Parte 3. Historias técnicas

Estas historias no las pide un usuario final, pero habilitan a las demás. Se estiman y se planifican igual que las funcionales.

### HT-01 · Estructurar el proyecto y el tema Material 3

| Campo | Valor |
|---|---|
| **Descripción** | Como **desarrollador**, quiero **un proyecto Android en Kotlin con Jetpack Compose, catálogo de versiones y tema Material 3 (colores, tipografía, formas)**, para **que todas las pantallas compartan el mismo lenguaje visual y las dependencias estén fijadas**. |
| **Estimación / Sprint** | 3 puntos / Sprint 1 |

**Criterios de aceptación**

- **Dado** el proyecto, **cuando** reviso la raíz, **entonces** existe `gradle/libs.versions.toml` con todas las versiones declaradas y `app/build.gradle.kts` consumiéndolas.
- **Dado** que abro la app, **cuando** reviso el tema, **entonces** existen `Color.kt`, `Type.kt` y `Theme.kt` con esquema de color claro y oscuro, y ninguna pantalla define colores literales.
- **Dado** que compilo el proyecto, **cuando** ejecuto `./gradlew assembleDebug`, **entonces** la compilación termina sin errores ni advertencias de API obsoleta.

### HT-02 · Construir el catálogo de componentes reutilizables

| Campo | Valor |
|---|---|
| **Descripción** | Como **desarrollador**, quiero **un conjunto de componentes Compose reutilizables (botón primario, campo de texto con error, tarjeta de evento, chip de estado, barra de aforo, diálogo de confirmación, estado vacío)**, para **no repetir código y mantener consistencia**. |
| **Estimación / Sprint** | 3 puntos / Sprint 1 (se amplía en el Sprint 5) |

**Criterios de aceptación**

- **Dado** cualquier formulario, **cuando** reviso el código, **entonces** los campos usan `CampoTextoValidado` con soporte de mensaje de error y estado deshabilitado.
- **Dado** cualquier pantalla con estados de entrada, **cuando** reviso el código, **entonces** usa `EstadoVacio` y `IndicadorCargando` en lugar de textos sueltos.
- **Dado** un componente, **cuando** reviso su firma, **entonces** no tiene dependencias de ViewModel ni de repositorios (componentes "tontos" y reutilizables).

### HT-03 · Implementar la base de datos local con Room

| Campo | Valor |
|---|---|
| **Descripción** | Como **desarrollador**, quiero **implementar las entidades, DAOs, relaciones, índices y migraciones de la base de datos local Room**, para **persistir toda la operación del local**. |
| **Estimación / Sprint** | 5 puntos / Sprint 2 (evidencia central de la PC 07) |

**Criterios de aceptación**

- **Dado** el código, **cuando** ejecuto la compilación, **entonces** se exporta el esquema en `app/schemas/…/1.json` y existen las seis tablas del modelo de datos.
- **Dado** el esquema, **cuando** reviso las claves foráneas, **entonces** están declaradas con `onDelete` explícito y los índices existen en `codigo`, `documento`, `(eventoId, estado)` y `(eventoId, fechaHora)`.
- **Dado** un cambio futuro del modelo, **cuando** subo la versión de la base, **entonces** existe una `Migration` probada (nunca `fallbackToDestructiveMigration` en producción).
- **Dado** el conjunto de pruebas instrumentadas, **cuando** ejecuto `./gradlew connectedDebugAndroidTest`, **entonces** las pruebas de DAO (insertar, consultar, actualizar estado, borrar en cascada) pasan en el emulador.

### HT-04 · Implementar las preferencias con DataStore

| Campo | Valor |
|---|---|
| **Descripción** | Como **desarrollador**, quiero **guardar la sesión del operador, el evento activo, la puerta, el tema y las opciones de escaneo en DataStore Preferences**, para **separar la configuración de la aplicación de los datos de negocio**. |
| **Estimación / Sprint** | 3 puntos / Sprint 2 |

**Criterios de aceptación**

- **Dado** el código, **cuando** reviso el paquete `data/preferences`, **entonces** existe un `DataStore<Preferences>` único a nivel de aplicación (extensión `Context.ajustesDataStore`), sin crear varias instancias.
- **Dado** cualquier preferencia, **cuando** la leo, **entonces** se expone como `Flow` de tipo `AjustesUsuario` (no como `Preferences` crudo) y los errores de lectura se manejan con `catch` emitiendo el valor por defecto.
- **Dado** una preferencia no configurada, **cuando** se lee, **entonces** devuelve el valor por defecto definido en el código, sin lanzar excepción.

### HT-05 · Accesibilidad, rendimiento y calidad

| Campo | Valor |
|---|---|
| **Descripción** | Como **desarrollador**, quiero **aplicar las prácticas de accesibilidad, rendimiento y calidad definidas en los requerimientos no funcionales**, para **que la aplicación sea usable y verificable**. |
| **Estimación / Sprint** | 3 puntos / Sprint 5 |

**Criterios de aceptación**

- **Dado** que recorro la app con TalkBack activado, **cuando** llego a cada botón e icono, **entonces** se anuncia una descripción útil (`contentDescription`) y no "botón sin etiqueta".
- **Dado** el listado de registros, **cuando** hago scroll en un evento con 20 000 registros, **entonces** la lista se mantiene fluida usando claves estables (`key`) y paginación.
- **Dado** el proyecto, **cuando** ejecuto `./gradlew lint`, **entonces** no hay errores de severidad *Error* ni advertencias críticas pendientes.

# Parte 4. Requerimientos no funcionales

Los requerimientos no funcionales se redactan con una **métrica verificable** y con la **forma de verificación** que se usará en el sprint de cierre. Un requerimiento sin número no se puede aprobar ni rechazar.

## 4.1. Rendimiento y eficiencia

| ID | Requerimiento | Métrica / valor objetivo | Cómo se verifica |
|---|---|---|---|
| RNF-01 | Tiempo de respuesta del escaneo | ≥ 95 % de los escaneos muestran el resultado en **≤ 2 s** desde que el QR está encuadrado | 20 escaneos cronometrados en el emulador y en el equipo físico, registrando la marca de tiempo en el log |
| RNF-02 | Arranque de la aplicación | **≤ 3 s** en frío hasta ver la primera pantalla útil; **≤ 1 s** para abrir la pantalla de escaneo | `adb shell am start -W` y medición con la herramienta *Startup* del perfilador |
| RNF-03 | Volumen de datos soportado | **5 000 entradas** y **20 000 registros de acceso** sin degradar la interfaz | Prueba con datos sembrados masivamente + validación de índices |
| RNF-04 | Búsqueda de un código de entrada | Consulta por `codigo` (índice único) en **≤ 50 ms** | `EXPLAIN QUERY PLAN` + medición con datos sembrados |
| RNF-05 | Exportación de reportes | CSV de 20 000 filas en **≤ 5 s**, con la interfaz respondiendo (sin *ANR*) | Cronómetro y observación de la interfaz durante la exportación |
| RNF-06 | Escaneo repetido | Ignorar lecturas del mismo código dentro de **5 s** | 10 lecturas seguidas del mismo QR y verificación de un solo registro |
| RNF-07 | Fluidez de listas | Listas largas con *scroll* sin *frames* perdidos visibles | Inspección con *Layout Inspector* / perfilador de rendimiento |

## 4.2. Disponibilidad y operación sin conexión

| ID | Requerimiento | Métrica / valor objetivo | Cómo se verifica |
|---|---|---|---|
| RNF-08 | Funcionamiento offline | **100 %** de las operaciones de puerta funcionan en modo avión | Prueba funcional completa (emitir, escanear, registrar, reportar) con la red desactivada |
| RNF-09 | Sin dependencia de terceros | La app no declara permiso `INTERNET` en esta versión | Revisión del `AndroidManifest.xml` |
| RNF-10 | Resistencia a interrupciones | Ningún registro confirmado se pierde ante cierre forzado o batería agotada | Cierre con `adb shell am force-stop` durante el escaneo y verificación en la base |
| RNF-11 | Persistencia de sesión | La sesión del operador, el evento activo y la puerta sobreviven al cierre de la app | Cerrar y abrir la app tras cada cambio |

## 4.3. Seguridad y privacidad

| ID | Requerimiento | Métrica / valor objetivo | Cómo se verifica |
|---|---|---|---|
| RNF-12 | Protección de credenciales | **0** PIN almacenados en texto plano; todos con hash SHA-256 + sal | Inspección de la base de datos con `Database Inspector` y de los archivos de preferencias |
| RNF-13 | Códigos QR no falsificables | Todo QR se valida con firma **HMAC-SHA256** truncada (8 bytes) y clave local | Prueba negativa: se genera un QR con código inventado y otro dispositivo lo rechaza |
| RNF-14 | Datos personales mínimos en el QR | El QR contiene **solo** versión, código y firma; **ningún** nombre, documento ni correo | Decodificación del QR generado y revisión de su contenido |
| RNF-15 | Protección de datos personales (Ley 29733, Perú) | Los datos del asistente son de uso interno, se guardan solo en el dispositivo y la exportación exige confirmación | Revisión de la pantalla de exportación y del contenido de los archivos generados |
| RNF-16 | Bloqueo ante intentos fallidos | Tras **3 PIN** incorrectos, bloqueo de **60 s** | Prueba manual de 4 intentos consecutivos |
| RNF-17 | Trazabilidad de accesos | **100 %** de los escaneos (permitidos y rechazados) quedan registrados con fecha, hora, puerta y operador | Comparación del número de escaneos realizados contra los registros en la base |
| RNF-18 | Integridad del historial | El historial de accesos es **inmutable**: la app no ofrece editarlo ni eliminarlo | Revisión de la interfaz y de los DAOs (no existen consultas de UPDATE/DELETE sobre `registros_acceso`) |

## 4.4. Usabilidad y experiencia de usuario

| ID | Requerimiento | Métrica / valor objetivo | Cómo se verifica |
|---|---|---|---|
| RNF-19 | Rapidez de operación en puerta | Un ingreso se resuelve en **≤ 3 s** incluyendo el escaneo | Prueba con 10 ingresos consecutivos con cronómetro |
| RNF-20 | Alta de una entrada | Registrar asistente y emitir su entrada en **≤ 6 pasos** y **≤ 2 min** | Conteo de interacciones en la prueba de usabilidad |
| RNF-21 | Mensajes claros en español | **100 %** de los textos de la interfaz en español (Perú), incluidos los errores de validación | Revisión de `strings.xml`: ningún texto literal en los composables |
| RNF-22 | Diseño consistente | Todas las pantallas usan el tema Material 3 y los componentes compartidos | Revisión de código: sin colores ni tipografías literales en pantallas |
| RNF-23 | Confirmación de acciones destructivas | Toda acción irreversible (anular, cancelar, importar respaldo) pide confirmación | Prueba manual de cada acción destructiva |
| RNF-24 | Aprendizaje sin manual | Un operador nuevo completa un registro de prueba **sin ayuda** tras 5 minutos de uso | Prueba con un tercero y observación |

## 4.5. Compatibilidad y portabilidad

| ID | Requerimiento | Métrica / valor objetivo | Cómo se verifica |
|---|---|---|---|
| RNF-25 | Versiones de Android soportadas | Android 7.0 (API 24) a Android 16 (API 36), `minSdk 24` / `targetSdk 36` | Ejecución en dos AVD (API 24 y API 36) y en el equipo físico |
| RNF-26 | Tipos de pantalla | Teléfonos y tablets de 4.5" a 10", orientación vertical y horizontal en la pantalla de escaneo | Prueba en 3 perfiles de AVD distintos |
| RNF-27 | Adaptación a modo oscuro y a la barra del sistema | La interfaz respeta el tema del sistema y no queda contenido bajo las barras | Revisión visual en modo claro y oscuro |
| RNF-28 | Independencia del hardware de cámara | El escaneo funciona con cámara trasera, delantera (opcional) y cámara virtual del emulador | Prueba en emulador con imagen de prueba y en equipo físico |
| RNF-29 | Instalación | APK de depuración **≤ 30 MB** y sin dependencias de servicios de Google obligatorios para la operación | Revisión del tamaño del APK y de la lista de dependencias |

## 4.6. Mantenibilidad y calidad del software

| ID | Requerimiento | Métrica / valor objetivo | Cómo se verifica |
|---|---|---|---|
| RNF-30 | Arquitectura en capas | **0** referencias desde el dominio hacia el framework Android (sin `import android.*` en `domain/`) | Revisión de código y prueba unitaria de JVM que instancia los casos de uso sin Android |
| RNF-31 | Cobertura de pruebas del núcleo | **≥ 60 %** de cobertura en `domain/` y `data/` (casos de uso, validaciones, repositorios, DAOs) | Reporte de cobertura de las pruebas unitarias e instrumentadas |
| RNF-32 | Pruebas automatizadas presentes | Al menos **1 prueba por caso de uso** y **1 por DAO**, todas en verde | `./gradlew test` y `./gradlew connectedDebugAndroidTest` |
| RNF-33 | Convenciones de código | Código en Kotlin con estilo oficial (`ktlint`/`detekt`) y sin *warnings* de compilación | Ejecución del analizador en el pipeline local |
| RNF-34 | Versionado y trazabilidad del código | Todo cambio entra por *commit* con mensaje descriptivo y el repositorio refleja los sprints | Revisión del historial de Git (`git log --oneline`) |
| RNF-35 | Separación de configuración y datos | Las preferencias están en **DataStore** y los datos de negocio en **Room**, sin mezclarse | Revisión del esquema de la base y del archivo de preferencias |
| RNF-36 | Documentación técnica | Diagrama de arquitectura, modelo de datos y decisiones de diseño documentados y actualizados | Revisión de `docs/` en el repositorio |

## 4.7. Accesibilidad

| ID | Requerimiento | Métrica / valor objetivo | Cómo se verifica |
|---|---|---|---|
| RNF-37 | Contraste de color | Cumplir **WCAG 2.1 AA** (4.5:1 en texto normal, 3:1 en texto grande) | Revisión con la herramienta de contraste de Android Studio |
| RNF-38 | Áreas táctiles | Todo elemento interactivo mide **≥ 48 × 48 dp** | Revisión en *Layout Inspector* |
| RNF-39 | Escalado de texto | La interfaz no se rompe con el tamaño de fuente del sistema al **200 %** | Prueba con la fuente del sistema al máximo |
| RNF-40 | Lectura con TalkBack | Todo botón, icono, campo e imagen informativa tiene etiqueta accesible | Recorrido completo con TalkBack activado |
| RNF-41 | Resultado del escaneo accesible | El resultado no depende solo del color: siempre hay icono + texto + aviso sonoro | Prueba con el filtro de daltonismo y sin sonido |
| RNF-42 | Indicadores de error | Los errores de formulario se anuncian junto al campo y como aviso accesible | Prueba con TalkBack en el formulario de evento y de asistente |

## 4.8. Operación y despliegue del local

| ID | Requerimiento | Métrica / valor objetivo | Cómo se verifica |
|---|---|---|---|
| RNF-43 | Duración de la sesión de puerta | La cámara se libera al salir de la pantalla de escaneo; ninguna sesión de 4 h agota la batería del dispositivo | Prueba de 4 h con registro del consumo de batería |
| RNF-44 | Uso con una sola mano y en condiciones de poca luz | El escaneo funciona en luz baja y con el operador sosteniendo el teléfono con una mano | Prueba en un ambiente oscuro y revisión de la disposición de los controles |
| RNF-45 | Recuperación ante llenado del almacenamiento | Si el almacenamiento se agota, la app avisa con un mensaje claro en lugar de cerrarse | Prueba de escritura con el almacenamiento limitado |
| RNF-46 | Instalación y puesta en marcha sin soporte técnico | El APK se instala en un equipo nuevo y queda operativo en **≤ 10 min** con el respaldo importado | Prueba de instalación limpia con cronómetro |

# Parte 5. Reglas de negocio

Las reglas de negocio son independientes de la implementación y se traducen en validaciones del dominio (casos de uso) y en restricciones de la base de datos.

| ID | Regla de negocio | Dónde se aplica |
|---|---|---|
| RN-01 | Una entrada solo permite **un** ingreso: al registrarse un ingreso permitido pasa de VÁLIDA a USADA y no vuelve a la vida | Caso de uso `RegistrarAccesoUseCase` (transacción) |
| RN-02 | No se permite emitir entradas que superen el **cupo del tipo de entrada** | `EmitirEntradaUseCase` + consulta de conteo por tipo |
| RN-03 | Un asistente no puede tener **dos entradas VÁLIDAS para el mismo evento** | `EmitirEntradaUseCase` + regla de unicidad lógica |
| RN-04 | Una entrada solo es válida si su **firma** corresponde a la clave local **y** el evento coincide con el evento activo | `ValidarEntradaUseCase` |
| RN-05 | El aforo ocupado se cuenta con los **registros de acceso con resultado PERMITIDO y vigentes**, no con las entradas emitidas | `ConsultarAforoUseCase` (consulta de conteo) |
| RN-06 | Las entradas **ANULADAS** no permiten ingreso y **no ocupan aforo** | `ValidarEntradaUseCase` |
| RN-07 | Una entrada **USADA** no puede anularse; se registra como incidencia | Caso de uso de anulación |
| RN-08 | El **historial de accesos es inmutable**: todo escaneo, permitido o rechazado, se conserva | DAO de registros (solo `INSERT` y `SELECT`) |
| RN-09 | Cuando el aforo está completo, la entrada **no se quema** al rechazarse; permanece VÁLIDA para otro momento | `RegistrarAccesoUseCase` |
| RN-10 | Solo un operador con rol **ADMINISTRADOR** puede autorizar excepciones de aforo, anular entradas o importar respaldos | Comprobación de rol en el ViewModel y en el caso de uso |
| RN-11 | Un evento **CANCELADO** no puede ser evento activo ni aceptar ingresos | `SeleccionarEventoActivoUseCase` y `ValidarEntradaUseCase` |
| RN-12 | Debe existir siempre **al menos un operador administrador activo** | Caso de uso de operadores |
| RN-13 | La **puerta** y el **operador** que registran el acceso son obligatorios y no editables después | Inserción del registro de acceso |
| RN-14 | El aforo máximo del evento **no puede reducirse** por debajo de la cantidad de personas ya registradas | Edición de evento |
| RN-15 | El **PIN** es de mínimo 4 dígitos y nunca se guarda en texto plano | Caso de uso de operadores (hash + sal) |

# Parte 6. Definiciones, riesgos y trazabilidad

## 6.1. Definición de Preparado (*Definition of Ready*)

Una historia puede entrar al sprint cuando cumple todo lo siguiente:

1. Tiene identificador, rol, funcionalidad y beneficio redactados.
2. Tiene criterios de aceptación en formato Dado/Cuando/Entonces, sin ambigüedades.
3. Está estimada en puntos y priorizada con MoSCoW.
4. Tiene identificados los datos que necesita (tablas o claves de DataStore).
5. Cabe en un sprint sin depender de otra historia no terminada.
6. Tiene una forma de demostrarse (pantalla, video, prueba o archivo).

## 6.2. Definición de Terminado (*Definition of Done*)

Una historia está terminada cuando **todo** lo siguiente es verdad:

1. El código está compilado sin errores (`./gradlew assembleDebug`) y sin *warnings* nuevos.
2. Cumple todos sus criterios de aceptación, verificados por una persona distinta de quien lo escribió (o por el mismo autor con evidencia en video).
3. Tiene pruebas automatizadas que pasan (unitarias para dominio/datos; instrumentadas si toca base de datos o UI crítica).
4. Los errores y estados vacíos están contemplados: sin conexión, sin datos, permiso denegado y datos inválidos.
5. Todos los textos están en `strings.xml`, en español.
6. El historial de Git incluye el *commit* con el identificador de la historia (`feat(HU-07): emitir entrada con QR`).
7. El documento de planificación se actualizó si cambió algún criterio.

## 6.3. Riesgos y mitigación

| Riesgo | Probabilidad | Impacto | Mitigación |
|---|---|---|---|
| El emulador no cuenta con Google Play Services y el escaneo QR no funciona en las pruebas | Media | Alto | Usar la variante de escaneo con biblioteca embebida (ZXing) como respaldo y probar siempre en el equipo físico |
| La cámara del emulador no enfoca bien un QR en pantalla | Alta | Medio | Permitir la validación por código manual (HU-15) y usar una imagen de QR fija en el emulador |
| Cambios en el modelo de datos durante el Sprint 2 rompen la base de datos | Media | Medio | Definir el modelo en la PC 05, exportar el esquema y escribir migraciones probadas |
| Incompatibilidad de versiones entre Kotlin, AGP y Compose | Media | Alto | Catálogo de versiones en `libs.versions.toml` fijado y documentado; no actualizar dependencias a mitad de sprint |
| El alcance crece (reportes, respaldo, operadores) | Alta | Medio | Prioridad MoSCoW: lo *Could* se recorta primero; el núcleo es escanear y registrar |
| El tiempo semanal disponible es menor al planificado | Media | Medio | Sprints de una semana con alcance fijo y renegociación al final de cada semana; velocidad real medida, no supuesta |
| Pérdida del dispositivo con los datos del evento | Baja | Alto | Respaldo exportable (HU-24) y recordatorio de respaldo después de cada evento |
| Manejo inadecuado de datos personales de los asistentes | Media | Alto | Datos mínimos, almacenamiento local, confirmación en exportaciones y referencia a la Ley 29733 |

## 6.4. Matriz de trazabilidad (historia → sprint → pantalla → fuente de datos)

| Historia | Sprint | Pantalla (Compose) | Fuente de datos |
|---|---|---|---|
| HU-01, HU-02, HU-03 | S1 / S2 / S4 | `EventosScreen`, `EventoDetalleScreen` | Room: `eventos` |
| HU-04 | S2 | `EventoDetalleScreen` | Room: `tipos_entrada` |
| HU-05, HU-06 | S1 / S4 | `AsistentesScreen`, `AsistenteDetalleScreen` | Room: `asistentes` |
| HU-07, HU-08, HU-09 | S1 / S2 / S4 | `EntradasScreen`, `EntradaDetalleScreen` | Room: `entradas` |
| HU-10, HU-22 | S1 / S5 | `LoginScreen`, `OperadoresScreen` | Room: `operadores` + DataStore (sesión) |
| HU-11 | S2 | `SeleccionEventoScreen` | DataStore: `evento_activo_id`, `puerta_activa` |
| HU-12, HU-13, HU-15, HU-16 | S3 | `EscanerScreen` | Room: `entradas` + `registros_acceso` |
| HU-17, HU-18 | S3 | `InicioScreen` (barra de aforo) | Room: `eventos` + `registros_acceso` |
| HU-19, HU-20, HU-21 | S4 | `HistorialScreen`, `ReportesScreen` | Room: `registros_acceso` |
| HU-14 | S4 | Transversal | Room (operación offline) |
| HU-23 | S2 | `AjustesScreen` | DataStore: tema, sonido, vibración |
| HU-24 | S5 | `RespaldoScreen` | Room + archivo `.json` local |

