#!/usr/bin/env python3
"""Genera los diagramas SVG y PNG de la documentación de la Práctica de Campo 05.

Salida:
  docs/figuras/01-arquitectura-mvvm.svg|.png
  docs/figuras/02-estructura-paquetes.svg|.png
  docs/figuras/03-mapa-navegacion.svg|.png
  docs/figuras/04-modelo-datos-er.svg|.png
  docs/figuras/05-flujo-escaneo.svg|.png

Requiere: rsvg-convert (librsvg).  Uso: python3 scripts/diagramas.py
"""
from __future__ import annotations

import os
import subprocess
import sys

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DESTINO = os.path.join(RAIZ, "docs", "figuras")

FUENTE = "DejaVu Sans, Verdana, sans-serif"
MONO = "DejaVu Sans Mono, Consolas, monospace"

AZUL = "#1F3864"
AZUL2 = "#2E74B5"
AZUL_CLARO = "#DEEAF6"
GRIS = "#F2F2F2"
GRIS_BORDE = "#9AA5B1"
VERDE = "#1E6B34"
VERDE_CLARO = "#E3F4E7"
ROJO = "#B3261E"
ROJO_CLARO = "#FCE4E2"
NARANJA = "#A65B00"
NARANJA_CLARO = "#FFF2DC"
MORADO = "#5B2C6F"
MORADO_CLARO = "#F0E6F5"
TEXTO = "#1A1A1A"
BLANCO = "#FFFFFF"


def esc(s: str) -> str:
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


class Lienzo:
    """Constructor mínimo de SVG."""

    def __init__(self, ancho: int, alto: int):
        self.ancho = ancho
        self.alto = alto
        self.cuerpo: list[str] = []
        self._defs()

    def _defs(self) -> None:
        marcadores = []
        colores = {
            "azul": AZUL, "azul2": AZUL2, "verde": VERDE, "rojo": ROJO,
            "naranja": NARANJA, "gris": GRIS_BORDE, "morado": MORADO,
        }
        for nombre, color in colores.items():
            marcadores.append(
                f'<marker id="ar-{nombre}" viewBox="0 0 10 10" refX="9" refY="5" '
                f'markerWidth="6" markerHeight="6" orient="auto-start-reverse">'
                f'<path d="M 0 0 L 10 5 L 0 10 z" fill="{color}"/></marker>'
            )
        self.fondo_defs = "<defs>" + "".join(marcadores) + "</defs>"

    def rect(self, x, y, w, h, relleno=BLANCO, borde=AZUL, radio=10, grosor=1.6, opacidad=1.0):
        self.cuerpo.append(
            f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{radio}" ry="{radio}" '
            f'fill="{relleno}" stroke="{borde}" stroke-width="{grosor}" opacity="{opacidad}"/>'
        )

    def texto(self, x, y, s, tam=14, color=TEXTO, peso="normal", ancla="start",
              familia=FUENTE, cursiva=False, espaciado=None):
        estilo = f' font-style="italic"' if cursiva else ""
        esp = f' letter-spacing="{espaciado}"' if espaciado else ""
        self.cuerpo.append(
            f'<text x="{x}" y="{y}" font-family="{familia}" font-size="{tam}" '
            f'fill="{color}" font-weight="{peso}" text-anchor="{ancla}"{estilo}{esp}>{esc(s)}</text>'
        )

    def texto_vertical(self, x, y, s, tam=12, color=TEXTO, peso="normal", ancla="middle"):
        self.cuerpo.append(
            f'<text x="{x}" y="{y}" transform="rotate(-90 {x} {y})" font-family="{FUENTE}" '
            f'font-size="{tam}" fill="{color}" font-weight="{peso}" text-anchor="{ancla}">'
            f'{esc(s)}</text>'
        )

    def lineas(self, x, y, filas, tam=13, interlineado=19, color=TEXTO, familia=MONO,
               color_resalte=None):
        for i, fila in enumerate(filas):
            col = color
            if color_resalte and i < len(color_resalte) and color_resalte[i]:
                col = color_resalte[i]
            self.texto(x, y + i * interlineado, fila, tam=tam, color=col, familia=familia)

    def linea(self, x1, y1, x2, y2, color=AZUL, grosor=1.6, guion=False,
              flecha=None, doble_flecha=False):
        d = f' stroke-dasharray="6 5"' if guion else ""
        m1 = f' marker-start="url(#ar-{flecha})"' if (flecha and doble_flecha) else ""
        m2 = f' marker-end="url(#ar-{flecha})"' if flecha else ""
        self.cuerpo.append(
            f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{color}" '
            f'stroke-width="{grosor}"{d}{m1}{m2}/>'
        )

    def camino(self, puntos, color=AZUL, grosor=1.6, guion=False, flecha=None):
        d = "M " + " L ".join(f"{x} {y}" for x, y in puntos)
        dash = f' stroke-dasharray="6 5"' if guion else ""
        m = f' marker-end="url(#ar-{flecha})"' if flecha else ""
        self.cuerpo.append(
            f'<path d="{d}" fill="none" stroke="{color}" stroke-width="{grosor}"{dash}{m}/>'
        )

    def caja_etiquetada(self, x, y, w, h, titulo, filas, relleno=AZUL_CLARO, borde=AZUL2,
                        tam_titulo=15, tam_filas=13, separacion=6):
        """Caja con título centrado y filas de texto centradas."""
        self.rect(x, y, w, h, relleno=relleno, borde=borde)
        self.texto(x + w / 2, y + 24, titulo, tam=tam_titulo, peso="bold", color=borde,
                   ancla="middle")
        for i, fila in enumerate(filas):
            self.texto(x + w / 2, y + 24 + separacion + 22 + i * 19, fila, tam=tam_filas,
                       ancla="middle")

    def guardar(self, nombre: str) -> str:
        svg = (
            f'<svg xmlns="http://www.w3.org/2000/svg" width="{self.ancho}" '
            f'height="{self.alto}" viewBox="0 0 {self.ancho} {self.alto}">'
            f'{self.fondo_defs}'
            f'<rect x="0" y="0" width="{self.ancho}" height="{self.alto}" fill="{BLANCO}"/>'
            + "".join(self.cuerpo) + "</svg>"
        )
        ruta = os.path.join(DESTINO, nombre + ".svg")
        with open(ruta, "w", encoding="utf-8") as f:
            f.write(svg)
        subprocess.run(["rsvg-convert", "-z", "2", "-o",
                        os.path.join(DESTINO, nombre + ".png"), ruta], check=True)
        return ruta


def titulo(c: Lienzo, texto: str, ancho: int, y=38) -> None:
    c.texto(ancho / 2, y, texto, tam=24, peso="bold", color=AZUL, ancla="middle")


# ---------------------------------------------------------------- figura 1
def figura_arquitectura() -> None:
    W, H = 1320, 960
    c = Lienzo(W, H)
    titulo(c, "Arquitectura MVVM en capas — Registro de Entrada", W)

    x0, ancho_main = 60, 830
    x_serv = 930
    ancho_serv = 330

    # Capa 1: UI Compose
    c.rect(x0, 70, ancho_main, 210, relleno="#FAFCFF", borde=AZUL2)
    c.texto(x0 + 16, 96, "CAPA DE PRESENTACIÓN · Interfaz (Jetpack Compose)", tam=15,
            peso="bold", color=AZUL2)
    pantallas = [
        ["LoginScreen", "InicioScreen", "EventosScreen", "AsistentesScreen"],
        ["EntradasScreen", "EscanerScreen", "HistorialScreen", "AjustesScreen"],
    ]
    for fi, fila in enumerate(pantallas):
        for co, nombre in enumerate(fila):
            bx = x0 + 22 + co * 200
            by = 118 + fi * 74
            c.rect(bx, by, 182, 56, relleno=BLANCO, borde=AZUL2, radio=8)
            c.texto(bx + 91, by + 26, nombre, tam=13, peso="bold", ancla="middle")
            c.texto(bx + 91, by + 45, "Composable sin estado" if fi == 0 else "recibe UiState",
                    tam=10, color="#555555", ancla="middle")

    # Capa 2: ViewModel
    c.rect(x0, 300, ancho_main, 150, relleno="#F5F9FF", borde=AZUL2)
    c.texto(x0 + 16, 326, "CAPA DE PRESENTACIÓN · ViewModel", tam=15, peso="bold", color=AZUL2)
    vms = ["LoginViewModel", "EventosViewModel", "EscanerViewModel", "HistorialViewModel"]
    for i, nombre in enumerate(vms):
        bx = x0 + 22 + i * 200
        c.rect(bx, 342, 182, 44, relleno=BLANCO, borde=AZUL2, radio=8)
        c.texto(bx + 91, 370, nombre, tam=13, ancla="middle")
    c.rect(x0 + 22, 396, ancho_main - 44, 42, relleno=AZUL_CLARO, borde=AZUL2, radio=8)
    c.texto(x0 + ancho_main / 2, 422,
            "UiState inmutable (data class / sealed) + StateFlow  ·  eventos de UI  ·  viewModelScope",
            tam=13, ancla="middle", color=AZUL)

    # Capa 3: Dominio
    c.rect(x0, 470, ancho_main, 165, relleno="#F7FBF7", borde=VERDE)
    c.texto(x0 + 16, 496, "CAPA DE DOMINIO (Kotlin puro, sin Android)", tam=15, peso="bold",
            color=VERDE)
    c.rect(x0 + 22, 512, 250, 108, relleno=BLANCO, borde=VERDE, radio=8)
    c.texto(x0 + 147, 538, "Modelos de dominio", tam=13, peso="bold", ancla="middle", color=VERDE)
    c.lineas(x0 + 34, 560, ["Evento", "Entrada", "RegistroAcceso", "Aforo, EstadoEntrada"],
             tam=11)
    c.rect(x0 + 290, 512, 250, 108, relleno=BLANCO, borde=VERDE, radio=8)
    c.texto(x0 + 415, 538, "Interfaces de repositorio", tam=13, peso="bold", ancla="middle",
            color=VERDE)
    c.lineas(x0 + 302, 560, ["EventoRepository", "EntradaRepository", "AccesoRepository",
                             "AsistenteRepository"], tam=11)
    c.rect(x0 + 558, 512, 250, 108, relleno=BLANCO, borde=VERDE, radio=8)
    c.texto(x0 + 683, 538, "Casos de uso", tam=13, peso="bold", ancla="middle", color=VERDE)
    c.lineas(x0 + 570, 560, ["RegistrarAccesoUseCase", "EmitirEntradaUseCase",
                             "ConsultarAforoUseCase", "ExportarReporteUseCase"], tam=11)

    # Capa 4: Datos
    c.rect(x0, 655, ancho_main, 205, relleno="#FFF9F4", borde=NARANJA)
    c.texto(x0 + 16, 681, "CAPA DE DATOS (implementa lo que declara el dominio)", tam=15,
            peso="bold", color=NARANJA)
    c.rect(x0 + 22, 698, 250, 142, relleno=BLANCO, borde=NARANJA, radio=8)
    c.texto(x0 + 147, 724, "Repositorios", tam=13, peso="bold", ancla="middle", color=NARANJA)
    c.lineas(x0 + 34, 746, ["*RepositoryImpl", "Mappers", "Entity ↔ Modelo", "Resultado<T>"],
             tam=11)
    c.rect(x0 + 290, 698, 250, 142, relleno=BLANCO, borde=NARANJA, radio=8)
    c.texto(x0 + 415, 724, "Room (SQLite)", tam=13, peso="bold", ancla="middle", color=NARANJA)
    c.lineas(x0 + 302, 746, ["AppDatabase", "6 entidades + DAOs", "Índices y FKs",
                             "@Transaction", "Flows reactivos"], tam=11)
    c.rect(x0 + 558, 698, 250, 142, relleno=BLANCO, borde=NARANJA, radio=8)
    c.texto(x0 + 683, 724, "DataStore Preferences", tam=13, peso="bold", ancla="middle",
            color=NARANJA)
    c.lineas(x0 + 570, 746, ["ajustes.preferences_pb", "Sesión del operador",
                             "Evento y puerta activos", "Tema, sonido, vibración"], tam=11)

    # Servicios de plataforma
    c.rect(x_serv, 300, ancho_serv, 580, relleno="#F8F6FB", borde=MORADO)
    c.texto(x_serv + ancho_serv / 2, 326, "SERVICIOS DE PLATAFORMA", tam=15, peso="bold",
            color=MORADO, ancla="middle")
    servicios = [
        ("CameraX + ML Kit", ["EscaneadorQr", "Flow<String> con", "el texto del QR"]),
        ("ZXing", ["GeneradorQr", "Bitmap del QR", "para mostrarlo"]),
        ("FirmadorQr", ["HMAC-SHA256", "truncado (8 bytes)", "RN-04 · RNF-13"]),
        ("Reloj / GeneradorId", ["Interfaces inyectables", "→ pruebas sin", "depender del sistema"]),
        ("ValidadorDocumento", ["DNI, correo,", "teléfono", "(validaciones de UI)"]),
    ]
    for i, (titulo_s, filas) in enumerate(servicios):
        by = 344 + i * 100
        c.rect(x_serv + 18, by, ancho_serv - 36, 86, relleno=BLANCO, borde=MORADO, radio=8)
        c.texto(x_serv + ancho_serv / 2, by + 24, titulo_s, tam=13, peso="bold",
                color=MORADO, ancla="middle")
        c.lineas(x_serv + 34, by + 44, filas, tam=11)
    c.lineas(x_serv + 34, 838, ["Todos se inyectan por interfaz",
                                "(inyección de dependencias)"], tam=11, familia=FUENTE,
             color=MORADO)

    # Flujo unidireccional (etiquetas giradas para no invadir las capas)
    c.linea(36, 120, 36, 860, color=AZUL2, grosor=2.4, flecha="azul2")
    c.texto_vertical(26, 470, "Intenciones del usuario (eventos de UI)", tam=12, color=AZUL2)
    c.linea(874, 120, 874, 860, color=VERDE, grosor=2.4, flecha="verde")
    c.texto_vertical(884, 470, "Estado expuesto (Flow / StateFlow)", tam=12, color=VERDE)

    # Conexiones entre capas
    for x_c in (200, 500, 780):
        c.linea(x_c, 280, x_c, 300, color=AZUL, flecha="azul")
        c.linea(x_c, 450, x_c, 470, color=AZUL, flecha="azul")
        c.linea(x_c, 635, x_c, 655, color=AZUL, flecha="azul")

    # Relación servicios ↔ capas
    c.linea(x_serv, 620, x0 + ancho_main, 620, color=MORADO, guion=True, flecha="morado")

    c.texto(x0, 900,
            "Regla de dependencias: las flechas apuntan hacia adentro. La presentación conoce al dominio; el dominio no conoce a Android; los datos implementan las interfaces del dominio.",
            tam=12, color=AZUL, cursiva=True)
    c.guardar("01-arquitectura-mvvm")


# ---------------------------------------------------------------- figura 2
def figura_paquetes() -> None:
    paquetes = [
        ("core/", AZUL, [
            "Reloj.kt · GeneradorId.kt",
            "FirmadorQr.kt · ValidadorDocumento.kt",
            "Resultado.kt",
        ]),
        ("data/", NARANJA, [
            "local/AppDatabase.kt · Convertidores.kt",
            "local/entity/  EventoEntity, TipoEntradaEntity,",
            "   AsistenteEntity, EntradaEntity,",
            "   RegistroAccesoEntity, OperadorEntity",
            "local/dao/  EventoDao, TipoEntradaDao, AsistenteDao,",
            "   EntradaDao, RegistroAccesoDao, OperadorDao",
            "local/relation/  EventoConTipos, EntradaConAsistente",
            "local/seed/DatosIniciales.kt",
            "preferences/  ClavesAjustes, SesionDataSource,",
            "   AjustesDataSource",
            "mapper/  Mappers.kt        repository/ *RepositoryImpl",
        ]),
        ("domain/", VERDE, [
            "model/  Evento, TipoEntrada, Asistente, Entrada,",
            "   RegistroAcceso, Operador, EstadoEntrada, Aforo",
            "repository/  interfaces (sin Android)",
            "usecase/  RegistrarAccesoUseCase, EmitirEntradaUseCase,",
            "   ConsultarAforoUseCase, ExportarReporteUseCase, …",
        ]),
        ("scanner/", MORADO, [
            "EscaneadorQr.kt  (CameraX + ML Kit · Flow<String>)",
            "GeneradorQr.kt   (ZXing · Bitmap del QR)",
        ]),
        ("di/", GRIS_BORDE, [
            "ModuloBaseDatos.kt · ModuloRepositorios.kt · ModuloApp.kt",
        ]),
        ("ui/", AZUL2, [
            "theme/  Color.kt, Type.kt, Theme.kt  (Material 3)",
            "components/  BotonPrimario, CampoTextoValidado,",
            "   TarjetaEvento, ChipEstado, BarraAforo, EstadoVacio",
            "navigation/  RutasRegistroEntrada.kt, GrafoNavegacion.kt",
            "screens/  login/, inicio/, eventos/, asistentes/, entradas/,",
            "   escaner/, historial/, reportes/, ajustes/",
        ]),
    ]
    alto_filas = sum(len(f) for _, _, f in paquetes)
    H = 170 + alto_filas * 21 + len(paquetes) * 34 + 60
    W = 1320
    c = Lienzo(W, H)
    titulo(c, "Estructura de paquetes del proyecto Android", W)

    c.rect(60, 70, 1200, H - 130, relleno="#FCFCFD", borde=GRIS_BORDE, radio=12)
    c.rect(84, 88, 430, 46, relleno=AZUL, borde=AZUL, radio=8)
    c.texto(100, 118, "pe.edu.upn.registroentrada", tam=16, peso="bold", color=BLANCO,
            familia=MONO)

    y = 172
    for nombre, color, filas in paquetes:
        c.linea(120, y - 16, 120, y + len(filas) * 21 - 6, color=color, grosor=2)
        c.linea(120, y - 16, 146, y - 16, color=color, grosor=2)
        c.texto(156, y, nombre, tam=15, peso="bold", color=color, familia=MONO)
        c.lineas(146, y + 26, filas, tam=12, interlineado=21)
        y += len(filas) * 21 + 34

    c.texto(84, H - 52,
            "Regla: ui/ → domain/ ← data/   ·   domain/ no importa android.*   ·   cada capa expone interfaces, no implementaciones",
            tam=12, color=AZUL, cursiva=True)
    c.guardar("02-estructura-paquetes")


# ---------------------------------------------------------------- figura 3
def figura_navegacion() -> None:
    W, H = 1340, 860
    c = Lienzo(W, H)
    titulo(c, "Mapa de navegación (Navigation Compose)", W)

    def caja(x, y, w, h, nombre, sub, color, relleno):
        c.rect(x, y, w, h, relleno=relleno, borde=color, radio=10)
        c.texto(x + w / 2, y + 28, nombre, tam=14, peso="bold", ancla="middle", color=color,
                familia=MONO)
        for i, s in enumerate(sub):
            c.texto(x + w / 2, y + 48 + i * 16, s, tam=11, ancla="middle", color="#444444")

    caja(60, 90, 230, 74, "login", ["Inicio de sesión del", "operador con PIN (HU-10)"],
         AZUL, AZUL_CLARO)
    caja(360, 90, 250, 74, "inicio", ["Panel: evento activo,", "aforo, accesos de hoy"],
         AZUL, AZUL_CLARO)
    caja(690, 90, 250, 74, "seleccion-evento", ["Elegir evento y puerta", "(HU-11 · DataStore)"],
         NARANJA, NARANJA_CLARO)
    caja(1010, 90, 270, 74, "eventos", ["Lista + búsqueda de", "eventos (HU-02)"], AZUL,
         AZUL_CLARO)

    # barra inferior
    c.rect(60, 240, 1220, 92, relleno="#F4F7FB", borde=AZUL2, radio=12)
    c.texto(76, 266, "Barra de navegación inferior (pantalla principal)", tam=13, peso="bold",
            color=AZUL2)
    pestanas = ["Inicio", "Eventos", "ESCANEAR", "Historial", "Ajustes"]
    for i, p in enumerate(pestanas):
        bx = 90 + i * 240
        destacado = p == "ESCANEAR"
        c.rect(bx, 280, 200, 40, relleno=VERDE if destacado else BLANCO,
               borde=VERDE if destacado else AZUL2, radio=8)
        c.texto(bx + 100, 306, p, tam=13, peso="bold", ancla="middle",
                color=BLANCO if destacado else AZUL2)

    # fila 3
    caja(60, 400, 250, 96, "evento/{id}", ["Detalle y tipos de", "entrada (HU-03, HU-04)",
                                          "→ entrada/{id}"], AZUL, BLANCO)
    caja(360, 400, 250, 96, "evento/nuevo", ["Formulario de", "creación (HU-01)"], AZUL, BLANCO)
    caja(660, 400, 250, 96, "escaner", ["Cámara + resultado", "superpuesto (HU-12 a HU-16)",
                                        "→ entrada manual"], VERDE, VERDE_CLARO)
    caja(960, 400, 320, 96, "entrada/{entradaId}", ["QR en pantalla, código en texto,",
                                                    "anular, compartir (HU-07 a HU-09)"],
         AZUL, BLANCO)

    # fila 4
    caja(60, 580, 250, 92, "asistentes", ["Lista + búsqueda", "(HU-05, HU-06)"], AZUL, BLANCO)
    caja(360, 580, 250, 92, "asistente/{id}", ["Ficha del asistente y", "sus entradas (HU-06)"],
         AZUL, BLANCO)
    caja(660, 580, 250, 92, "historial", ["Registros de acceso", "con filtros (HU-19)"], AZUL,
         BLANCO)
    caja(960, 580, 320, 92, "reportes", ["Resumen, aforo y exportación", "CSV (HU-20, HU-21)"],
         AZUL, BLANCO)

    # fila 5
    caja(60, 740, 250, 84, "ajustes", ["Tema, sonido,", "vibración (HU-23)"], AZUL, BLANCO)
    caja(360, 740, 250, 84, "operadores", ["Roles y PIN", "(HU-22)"], NARANJA, NARANJA_CLARO)
    caja(660, 740, 250, 84, "respaldo", ["Exportar e importar", "(HU-24)"], NARANJA,
         NARANJA_CLARO)

    # flechas
    c.linea(290, 128, 356, 128, color=AZUL, flecha="azul")
    c.linea(610, 128, 686, 128, color=AZUL, flecha="azul")
    c.linea(940, 128, 1006, 128, color=AZUL, flecha="azul")
    c.linea(1130, 164, 1130, 196, color=AZUL, flecha="azul")
    c.linea(500, 164, 500, 196, color=AZUL, flecha="azul")
    c.texto(516, 190, "al terminar: abre Escanear", tam=11, color=AZUL)

    c.linea(150, 340, 150, 396, color=AZUL, flecha="azul")
    c.linea(300, 340, 470, 396, color=AZUL, flecha="azul")
    c.linea(830, 340, 780, 396, color=VERDE, flecha="verde")
    c.linea(1030, 340, 1050, 396, color=AZUL, flecha="azul")
    c.linea(230, 340, 190, 576, color=AZUL, flecha="azul")
    c.linea(700, 340, 760, 576, color=AZUL, flecha="azul")
    c.linea(1090, 340, 1090, 576, color=AZUL, flecha="azul")
    c.linea(140, 340, 170, 736, color=AZUL, flecha="azul")
    c.linea(280, 820, 356, 800, color=NARANJA, flecha="naranja")
    c.linea(500, 822, 656, 822, color=NARANJA, flecha="naranja")
    c.linea(1110, 496, 1110, 576, color=VERDE, flecha="verde", guion=True)
    c.linea(400, 496, 380, 576, color=AZUL, flecha="azul", guion=True)

    c.texto(60, 852,
            "Verde = flujo de puerta (operador)   ·   Naranja = solo administrador   ·   Línea discontinua = navegación secundaria desde el detalle",
            tam=12, color=AZUL, cursiva=True)
    c.guardar("03-mapa-navegacion")


# ---------------------------------------------------------------- figura 4
def figura_modelo_datos() -> None:
    W, H = 1560, 1120
    c = Lienzo(W, H)
    titulo(c, "Modelo entidad-relación de la base de datos local (Room / SQLite)", W)

    def entidad(x, y, w, nombre, campos, color, relleno):
        alto = 46 + len(campos) * 20 + 12
        c.rect(x, y, w, alto, relleno=relleno, borde=color, radio=10)
        c.rect(x, y, w, 34, relleno=color, borde=color, radio=10)
        c.rect(x, y + 20, w, 14, relleno=color, borde=color, radio=0)
        c.texto(x + w / 2, y + 23, nombre, tam=15, peso="bold", color=BLANCO, ancla="middle",
                familia=MONO)
        for i, campo in enumerate(campos):
            c.texto(x + 14, y + 58 + i * 20, campo, tam=12.5, familia=MONO, color=TEXTO)
        return x, y, w, alto

    e_eventos = entidad(560, 90, 430, "eventos", [
        "id  INTEGER  PK",
        "nombre  TEXT  NOT NULL",
        "descripcion  TEXT  NULL",
        "lugar  TEXT  NOT NULL",
        "fecha_inicio  INTEGER (índice)",
        "fecha_fin  INTEGER",
        "aforo_maximo  INTEGER  > 0",
        "estado  TEXT  (BORRADOR…CANCELADO)",
        "creado_en  INTEGER",
    ], AZUL, AZUL_CLARO)

    e_tipos = entidad(70, 120, 400, "tipos_entrada", [
        "id  INTEGER  PK",
        "evento_id  FK → eventos(id) CASCADE",
        "nombre  TEXT  único por evento",
        "precio  REAL  ≥ 0",
        "cupo  INTEGER  ≥ 1",
        "color  TEXT  #RRGGBB",
    ], VERDE, VERDE_CLARO)

    e_asistentes = entidad(70, 620, 400, "asistentes", [
        "id  INTEGER  PK",
        "documento  TEXT  único (índice)",
        "nombres  TEXT  NOT NULL",
        "apellidos  TEXT  NOT NULL",
        "correo  TEXT  NULL",
        "telefono  TEXT  NULL",
        "creado_en  INTEGER",
    ], VERDE, VERDE_CLARO)

    e_entradas = entidad(560, 430, 430, "entradas", [
        "id  INTEGER  PK",
        "codigo  TEXT  único (índice)",
        "firma  TEXT  HMAC-SHA256",
        "evento_id  FK → eventos(id) CASCADE",
        "tipo_entrada_id  FK → tipos_entrada(id)",
        "asistente_id  FK → asistentes(id) CASCADE",
        "estado  TEXT  (VALIDA|USADA|ANULADA)",
        "emitida_en  INTEGER",
        "usada_en  INTEGER  NULL",
        "índice (evento_id, estado)",
    ], AZUL, AZUL_CLARO)

    e_registros = entidad(1050, 560, 470, "registros_acceso", [
        "id  INTEGER  PK",
        "entrada_id  FK → entradas(id) SET NULL",
        "evento_id  FK → eventos(id) CASCADE",
        "codigo_escaneado  TEXT",
        "resultado  TEXT  (PERMITIDO, YA_USADA,",
        "      NO_EXISTE, OTRO_EVENTO, ANULADA,",
        "      FIRMA_INVALIDA, AFORO_LLENO…)",
        "puerta  TEXT  NOT NULL",
        "operador_id  FK → operadores(id)",
        "fecha_hora  INTEGER  (índice)",
        "observacion  TEXT  NULL",
    ], ROJO, ROJO_CLARO)

    e_operadores = entidad(1050, 120, 440, "operadores", [
        "id  INTEGER  PK",
        "nombre  TEXT  NOT NULL",
        "usuario  TEXT  único (índice)",
        "pin_hash  TEXT  SHA-256 + sal",
        "pin_sal  TEXT",
        "rol  TEXT  (ADMINISTRADOR|PORTERO)",
        "activo  INTEGER  (0|1)",
        "creado_en  INTEGER",
    ], MORADO, MORADO_CLARO)

    # ---- relaciones
    def rel(puntos, etiqueta, color=AZUL, lx=0, ly=0):
        c.camino(puntos, color=color, grosor=2)
        c.texto(lx, ly, etiqueta, tam=12, color=color, peso="bold")

    rel([(474, 200), (516, 200), (516, 258), (556, 258)], "1 : N", VERDE, 478, 192)
    rel([(760, 348), (760, 426)], "1 : N", AZUL, 766, 390)
    rel([(270, 296), (270, 392), (600, 392), (600, 426)], "1 : N", VERDE, 282, 384)
    rel([(474, 660), (556, 660)], "1 : N", VERDE, 502, 652)
    rel([(990, 300), (1012, 300), (1012, 620), (1046, 620)], "1 : N", AZUL, 1018, 320)
    rel([(900, 690), (900, 762), (1046, 762)], "1 : N", AZUL, 906, 754)
    rel([(1270, 320), (1270, 556)], "1 : N", MORADO, 1276, 340)

    # nota
    c.rect(70, 900, 900, 152, relleno="#F7F9FC", borde=GRIS_BORDE, radio=10)
    c.texto(90, 928, "Notas de integridad y notación", tam=14, peso="bold", color=AZUL)
    notas = [
        "· Cardinalidad 1 : N — una fila de la tabla padre admite muchas filas de la tabla hija;",
        "  la flecha apunta siempre a la tabla hija (eventos → entradas, entradas → registros_acceso).",
        "· Claves foráneas con acción explícita: CASCADE (datos que no tienen sentido solos),",
        "  RESTRICT (tipo de entrada y operador con historial), SET NULL (entrada desconocida).",
        "· El aforo ocupado se calcula con COUNT sobre registros_acceso (resultado = PERMITIDO).",
        "· registros_acceso es inmutable: los DAOs solo exponen INSERT y SELECT (RN-08).",
    ]
    c.lineas(90, 952, notas, tam=12, interlineado=19, familia=FUENTE)

    c.rect(1000, 900, 520, 152, relleno="#FFF9F4", borde=NARANJA, radio=10)
    c.texto(1020, 928, "Fuera de la base de datos (DataStore)", tam=14, peso="bold",
            color=NARANJA)
    c.lineas(1020, 952, [
        "· operador_activo_id · operador_activo_nombre",
        "· evento_activo_id · puerta_activa · tema_app",
        "· sonido_escaneo · vibracion_escaneo",
        "· intentos_pin_fallidos · bloqueo_hasta · ultimo_respaldo",
    ], tam=11.5, interlineado=21, familia=FUENTE)

    c.guardar("04-modelo-datos-er")


# ---------------------------------------------------------------- figura 5
def figura_flujo_escaneo() -> None:
    W, H = 1400, 1000
    c = Lienzo(W, H)
    titulo(c, "Flujo de validación de un escaneo en la puerta (secuencia)", W)

    carriles = [
        (140, "Portero"),
        (420, "EscanerScreen", "Compose"),
        (720, "EscanerViewModel", "StateFlow"),
        (1010, "RegistrarAcceso", "UseCase"),
        (1290, "Room", "DAO + @Transaction"),
    ]
    for x, nombre, *resto in carriles:
        c.rect(x - 120, 70, 240, 58, relleno=AZUL_CLARO, borde=AZUL, radio=8)
        c.texto(x, 95, nombre, tam=14, peso="bold", ancla="middle", familia=MONO, color=AZUL)
        if resto:
            c.texto(x, 114, resto[0], tam=11, ancla="middle", color="#444444")
        c.linea(x, 128, x, 900, color=GRIS_BORDE, grosor=1.4, guion=True)

    pasos = [
        (170, 140, 420, "1. Apunta la cámara al QR", AZUL),
        (420, 190, 720, "2. alEscanear(textoQr) — CameraX + ML Kit", AZUL),
        (720, 240, 1010, "3. invoke(texto, eventoActivo, puerta, operadorId)", AZUL),
        (1010, 300, 1290, "4. consulta la entrada por código (índice único)", NARANJA),
        (1290, 350, 1010, "5. entrada + conteo de ingresos del evento", NARANJA),
    ]
    for x1, y, x2, texto, color in pasos:
        c.linea(x1, y, x2, y, color=color, grosor=1.8, flecha="naranja" if color == NARANJA else "azul")
        mitad = (x1 + x2) / 2
        c.texto(mitad, y - 10, texto, tam=12, ancla="middle", color=color)

    c.rect(700, 380, 640, 150, relleno="#F7F9FC", borde=AZUL, radio=10)
    c.texto(720, 406, "6. Validaciones en orden (dominio)", tam=13, peso="bold", color=AZUL)
    c.lineas(720, 428, [
        "formato RE|1|código|firma → HMAC-SHA256 coincide",
        "entrada existe · pertenece al evento activo · estado",
        "aforo: COUNT(PERMITIDO) < aforo_maximo · evento no cancelado",
    ], tam=11.5, interlineado=19, familia=FUENTE)

    c.linea(720, 540, 1290, 540, color=VERDE, grosor=2, flecha="verde")
    c.texto(1005, 532, "7. transacción atómica: estado → USADA, usada_en, INSERT del registro",
            tam=12, ancla="middle", color=VERDE)

    c.rect(700, 570, 640, 120, relleno=VERDE_CLARO, borde=VERDE, radio=10)
    c.texto(1020, 596, "PERMITIDO", tam=15, peso="bold", ancla="middle", color=VERDE)
    c.lineas(720, 622, [
        "Entrada VÁLIDA → USADA · se inserta el registro con resultado PERMITIDO",
        "Se muestran asistente y tipo de entrada · el aforo consumido sube en 1",
    ], tam=11.5, interlineado=19, familia=FUENTE)

    c.rect(60, 570, 600, 220, relleno=ROJO_CLARO, borde=ROJO, radio=10)
    c.texto(360, 596, "RECHAZADO (la entrada NO se quema, salvo si ya estaba usada)",
            tam=13, peso="bold", ancla="middle", color=ROJO)
    c.lineas(80, 622, [
        "FIRMA_INVALIDA  · el QR es de otro dispositivo o inventado",
        "NO_EXISTE       · el código no está en esta base de datos",
        "OTRO_EVENTO     · la entrada pertenece a otro evento",
        "YA_USADA        · ya ingresó (se muestra la hora del ingreso)",
        "ANULADA         · la entrada fue anulada por el administrador",
        "AFORO_LLENO     · queda VÁLIDA y puede autorizarse excepción",
        "EVENTO_CANCELADO· el evento no admite ingresos",
    ], tam=11.5, interlineado=21, familia=FUENTE)

    c.linea(720, 700, 420, 700, color=ROJO, grosor=1.8, flecha="rojo")
    c.texto(570, 692, "8. se registra el intento (RN-08, RNF-17)", tam=12, ancla="middle",
            color=ROJO)
    c.linea(1010, 780, 720, 780, color=AZUL, grosor=1.8, flecha="azul")
    c.texto(865, 772, "UiState con el resultado", tam=12, ancla="middle", color=AZUL)

    c.rect(60, 820, 1280, 90, relleno="#F7F9FC", borde=AZUL, radio=10)
    c.linea(140, 850, 420, 850, color=VERDE, grosor=2, flecha="verde")
    c.texto(160, 842, "verde: icono + PERMITIDO + sonido", tam=12, color=VERDE)
    c.linea(420, 885, 140, 885, color=ROJO, grosor=2, flecha="rojo")
    c.texto(160, 877, "rojo: icono + motivo + vibración", tam=12, color=ROJO)
    c.texto(1120, 866,
            "Todo el paso 6 y el 7 ocurren en una sola transacción: dos escaneos del mismo código no pueden registrar dos ingresos.",
            tam=11.5, color=AZUL, ancla="middle")
    c.guardar("05-flujo-escaneo")


def main() -> int:
    os.makedirs(DESTINO, exist_ok=True)
    figura_arquitectura()
    figura_paquetes()
    figura_navegacion()
    figura_modelo_datos()
    figura_flujo_escaneo()
    for archivo in sorted(os.listdir(DESTINO)):
        if archivo.endswith(".png"):
            ruta = os.path.join(DESTINO, archivo)
            print(f"{archivo:34s} {os.path.getsize(ruta) / 1024:8.1f} KB")
    return 0


if __name__ == "__main__":
    sys.exit(main())
