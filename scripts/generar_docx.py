#!/usr/bin/env python3
"""Genera el .docx de la Práctica de Campo 05 a partir del Markdown del repositorio.

Pipeline:
  1. pandoc (los dos documentos Markdown → un .docx con tabla de contenido)
  2. python-docx (formato de página, estilos, tablas, ancho de figuras, pie de página)
  3. marca w:updateFields para que Word/LibreOffice actualicen el índice al abrir

Requiere: pypandoc-binary, python-docx  →  uv pip install pypandoc-binary python-docx
Uso:      python3 scripts/generar_docx.py
"""
from __future__ import annotations

import os
import re
import shutil
import subprocess
import sys
import zipfile

import pypandoc
from docx import Document
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Inches, Pt, RGBColor

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DOCS = [
    os.path.join(RAIZ, "docs", "PC05-planificacion.md"),
    os.path.join(RAIZ, "docs", "PC05-arquitectura-y-modelo-de-datos.md"),
]
ENTREGA = os.path.join(RAIZ, "entrega")
NOMBRE = "PC05-Registro-de-Entrada-Planificacion-y-Arquitectura.docx"
SALIDA = os.path.join(ENTREGA, NOMBRE)
TEMPORAL = os.path.join(RAIZ, ".build", "base.docx")

AZUL = RGBColor(0x1F, 0x38, 0x64)
AZUL2 = RGBColor(0x2E, 0x74, 0xB5)
GRIS_ENCABEZADO = "DEEAF6"


SALTO = '```{=openxml}\n<w:p><w:r><w:br w:type="page"/></w:r></w:p>\n```'


def leer_partes() -> tuple[str, str, str]:
    """Devuelve (carátula, cuerpo del documento 1, documento 2)."""
    with open(DOCS[0], encoding="utf-8") as archivo:
        parte1 = archivo.read()
    with open(DOCS[1], encoding="utf-8") as archivo:
        parte2 = archivo.read()
    corte = parte1.find("```{=openxml}")
    if corte < 0:
        raise SystemExit("No se encontró el salto de página que cierra la carátula.")
    return parte1[:corte], parte1[corte:], parte2


def encabezados_de(markdown: str) -> list[tuple[int, str]]:
    """Encabezados de nivel 1 a 3, ignorando los bloques de código."""
    lista: list[tuple[int, str]] = []
    dentro_de_codigo = False
    for linea in markdown.splitlines():
        if linea.startswith("```"):
            dentro_de_codigo = not dentro_de_codigo
            continue
        if dentro_de_codigo:
            continue
        coincidencia = re.match(r"^(#{1,3})\s+(.+?)\s*$", linea)
        if not coincidencia:
            continue
        texto = re.sub(r"[*`]", "", coincidencia.group(2)).strip()
        if texto.upper().startswith("UNIVERSIDAD") or texto.startswith("Contenido"):
            continue
        lista.append((len(coincidencia.group(1)), texto))
    return lista


ENCABEZADOS: list[tuple[int, str]] = []


def indice_markdown(paginas: dict[str, int]) -> str:
    """Índice estático (visible en cualquier programa, con número de página)."""
    filas = ["# Contenido", "", "| Sección | Página |", "|---|---|"]
    for nivel, texto in ENCABEZADOS:
        pagina = paginas.get(texto, "")
        if nivel == 1:
            filas.append(f"| **{texto}** | {pagina} |")
        elif nivel == 2:
            filas.append(f"| · {texto} | {pagina} |")
        else:
            filas.append(f"| – {texto} | {pagina} |")
    return "\n".join(filas) + "\n"


def construir_markdown(paginas: dict[str, int]) -> str:
    caratula, resto1, parte2 = leer_partes()
    return (caratula + "\n" + SALTO + "\n\n" + indice_markdown(paginas)
            + "\n" + SALTO + "\n\n" + resto1 + "\n\n" + parte2)


def paso_pandoc(markdown: str) -> None:
    os.makedirs(os.path.dirname(TEMPORAL), exist_ok=True)
    combinado = os.path.join(os.path.dirname(TEMPORAL), "combinado.md")
    with open(combinado, "w", encoding="utf-8") as archivo:
        archivo.write(markdown)
    pypandoc.convert_file(
        combinado,
        "docx",
        outputfile=TEMPORAL,
        format="gfm+raw_attribute",
        extra_args=[
            f"--resource-path={os.path.join(RAIZ, 'docs')}",
            "--highlight-style=tango",
            "--metadata=lang=es-PE",
            "--standalone",
        ],
    )


def mapa_de_paginas(pdf: str) -> dict[str, int]:
    """Ubica la página de cada encabezado leyendo el PDF ya compilado."""
    salida = subprocess.run(["pdftotext", "-layout", pdf, "-"],
                            capture_output=True, text=True, check=True).stdout
    documento = salida.split("\f")
    # Las páginas del índice repiten el encabezado de su tabla
    indice = {n for n, contenido in enumerate(documento, start=1)
              if "Sección" in contenido and "Página" in contenido}
    mapa: dict[str, int] = {}
    for _, titulo in ENCABEZADOS:
        for numero, contenido in enumerate(documento, start=1):
            if numero in indice:
                continue
            lineas = {linea.strip() for linea in contenido.splitlines()}
            if titulo in lineas or any(
                linea.startswith(titulo[:45]) and len(titulo) > 45 for linea in lineas
            ):
                mapa[titulo] = numero
                break
    return mapa


def compilar(paginas: dict[str, int], etiqueta: str) -> None:
    print(f"   · compilando la versión {etiqueta}…")
    paso_pandoc(construir_markdown(paginas))
    doc = Document(TEMPORAL)
    formato_estilos(doc)
    formato_tablas(doc)
    formato_figuras(doc)
    formato_pagina(doc)
    doc.save(SALIDA)
    actualizar_campos(SALIDA)


def a_pdf() -> str:
    subprocess.run(
        ["soffice", "--headless", "--convert-to", "pdf", "--outdir",
         os.path.join(RAIZ, ".build"), SALIDA],
        check=True, capture_output=True,
    )
    return os.path.join(RAIZ, ".build", os.path.splitext(NOMBRE)[0] + ".pdf")


def fuente_estilo(estilo, nombre: str, tam: float, color: RGBColor | None = None,
                  negrita: bool | None = None) -> None:
    estilo.font.name = nombre
    # Fija también la fuente para caracteres asiáticos/complejos (Word lo exige)
    rpr = estilo.element.get_or_add_rPr()
    rfonts = rpr.find(qn("w:rFonts"))
    if rfonts is None:
        rfonts = OxmlElement("w:rFonts")
        rpr.append(rfonts)
    for atributo in ("w:ascii", "w:hAnsi", "w:cs", "w:eastAsia"):
        rfonts.set(qn(atributo), nombre)
    estilo.font.size = Pt(tam)
    if color is not None:
        estilo.font.color.rgb = color
    if negrita is not None:
        estilo.font.bold = negrita


def formato_estilos(doc: Document) -> None:
    fuente_estilo(doc.styles["Normal"], "Calibri", 11)
    doc.styles["Normal"].paragraph_format.space_after = Pt(6)
    doc.styles["Normal"].paragraph_format.line_spacing = 1.12

    encabezados = {
        "Title": ("Calibri", 26, AZUL, True),
        "Heading 1": ("Calibri", 17, AZUL, True),
        "Heading 2": ("Calibri", 14, AZUL2, True),
        "Heading 3": ("Calibri", 12, AZUL2, True),
        "Heading 4": ("Calibri", 11.5, AZUL2, True),
    }
    for nombre, (fuente, tam, color, negrita) in encabezados.items():
        if nombre in [s.name for s in doc.styles]:
            fuente_estilo(doc.styles[nombre], fuente, tam, color, negrita)

    for nombre in ("Source Code", "Verbatim Char", "Source Code Block"):
        if nombre in [s.name for s in doc.styles]:
            fuente_estilo(doc.styles[nombre], "Consolas", 9)

    if "Table Caption" in [s.name for s in doc.styles]:
        fuente_estilo(doc.styles["Table Caption"], "Calibri", 9.5, AZUL2)


def borde_celda(celda, color="B7C7E3", tam=6) -> None:
    tc_pr = celda._tc.get_or_add_tcPr()
    bordes = OxmlElement("w:tcBorders")
    for lado in ("top", "left", "bottom", "right"):
        el = OxmlElement(f"w:{lado}")
        el.set(qn("w:val"), "single")
        el.set(qn("w:sz"), str(tam))
        el.set(qn("w:color"), color)
        bordes.append(el)
    tc_pr.append(bordes)


def sombrear_celda(celda, color: str) -> None:
    tc_pr = celda._tc.get_or_add_tcPr()
    sombra = OxmlElement("w:shd")
    sombra.set(qn("w:val"), "clear")
    sombra.set(qn("w:color"), "auto")
    sombra.set(qn("w:fill"), color)
    tc_pr.append(sombra)


def formato_tablas(doc: Document) -> None:
    for tabla in doc.tables:
        tabla.alignment = WD_TABLE_ALIGNMENT.CENTER
        tabla.autofit = True

        # La tabla del índice usa columnas anchas: sección larga, página corta
        es_indice = tabla.rows and tabla.rows[0].cells[0].text.strip().lower().startswith("secci")
        if es_indice:
            tabla.autofit = False
            for fila in tabla.rows:
                fila.cells[0].width = Cm(15.4)
                fila.cells[1].width = Cm(1.8)

        # Encabezado repetido en cada página y con fondo
        primera = tabla.rows[0]
        tr_pr = primera._tr.get_or_add_trPr()
        repetir = OxmlElement("w:tblHeader")
        tr_pr.append(repetir)
        for celda in primera.cells:
            sombrear_celda(celda, GRIS_ENCABEZADO)
            for parrafo in celda.paragraphs:
                for run in parrafo.runs:
                    run.font.bold = True
                    run.font.size = Pt(9.5)
                    run.font.color.rgb = AZUL

        for fila in tabla.rows:
            for celda in fila.cells:
                borde_celda(celda)
                for parrafo in celda.paragraphs:
                    parrafo.paragraph_format.space_after = Pt(2)
                    parrafo.paragraph_format.line_spacing = 1.0
                    for run in parrafo.runs:
                        if not run.font.size:
                            run.font.size = Pt(9.5)


def formato_figuras(doc: Document) -> None:
    ancho_maximo = Inches(6.4)
    for forma in doc.inline_shapes:
        proporcion = forma.height / forma.width if forma.width else 1
        if forma.width > ancho_maximo:
            forma.width = ancho_maximo
            forma.height = int(ancho_maximo * proporcion)
        # Centrar el párrafo y la leyenda de la figura
        parrafo = forma._inline.getparent().getparent()
        while parrafo is not None and parrafo.tag != qn("w:p"):
            parrafo = parrafo.getparent()
        if parrafo is not None:
            for p in doc.paragraphs:
                if p._p is parrafo:
                    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                    break
    for parrafo in doc.paragraphs:
        texto = parrafo.text.strip()
        if texto.startswith("Figura "):
            parrafo.alignment = WD_ALIGN_PARAGRAPH.CENTER
            for run in parrafo.runs:
                run.font.size = Pt(9.5)
                run.font.italic = True
                run.font.color.rgb = RGBColor(0x44, 0x44, 0x44)


def campo(parrafo, instruccion: str) -> None:
    run = parrafo.add_run()
    inicio = OxmlElement("w:fldChar")
    inicio.set(qn("w:fldCharType"), "begin")
    run._r.append(inicio)
    run2 = parrafo.add_run()
    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = instruccion
    run2._r.append(instr)
    run3 = parrafo.add_run()
    fin = OxmlElement("w:fldChar")
    fin.set(qn("w:fldCharType"), "end")
    run3._r.append(fin)


def formato_pagina(doc: Document) -> None:
    for seccion in doc.sections:
        seccion.page_width = Cm(21.0)
        seccion.page_height = Cm(29.7)
        seccion.top_margin = Cm(2.4)
        seccion.bottom_margin = Cm(2.2)
        seccion.left_margin = Cm(2.4)
        seccion.right_margin = Cm(2.2)

        pie = seccion.footer
        parrafo = pie.paragraphs[0] if pie.paragraphs else pie.add_paragraph()
        parrafo.text = ""
        parrafo.alignment = WD_ALIGN_PARAGRAPH.CENTER
        run = parrafo.add_run(
            "Registro de Entrada · Práctica de Campo 05 · Nicolás Quevedo — UPN          Página "
        )
        run.font.size = Pt(8.5)
        run.font.color.rgb = RGBColor(0x66, 0x66, 0x66)
        campo(parrafo, "PAGE")
        for r in parrafo.runs:
            r.font.size = Pt(8.5)
            r.font.color.rgb = RGBColor(0x66, 0x66, 0x66)


def actualizar_campos(ruta: str) -> None:
    """Marca el documento para que Word/LibreOffice actualicen el índice al abrirlo."""
    temporal = ruta + ".tmp"
    with zipfile.ZipFile(ruta, "r") as origen, zipfile.ZipFile(temporal, "w", zipfile.ZIP_DEFLATED) as destino:
        for item in origen.infolist():
            datos = origen.read(item.filename)
            if item.filename == "word/settings.xml" and b"updateFields" not in datos:
                xml = datos.decode("utf-8")
                xml = xml.replace(
                    "</w:settings>",
                    '<w:updateFields w:val="true"/></w:settings>',
                )
                datos = xml.encode("utf-8")
            destino.writestr(item, datos)
    shutil.move(temporal, ruta)


def main() -> int:
    global ENCABEZADOS
    os.makedirs(ENTREGA, exist_ok=True)
    _, cuerpo1, parte2 = leer_partes()
    ENCABEZADOS = encabezados_de(cuerpo1 + "\n" + parte2)
    print(f"1/4 {len(ENCABEZADOS)} secciones para el índice.")
    print("2/4 Primera compilación (para medir la paginación)…")
    compilar({}, "borrador")
    paginas = mapa_de_paginas(a_pdf())
    faltantes = [texto for _, texto in ENCABEZADOS if texto not in paginas]
    print(f"3/4 Índice calculado: {len(paginas)}/{len(ENCABEZADOS)} secciones ubicadas.")
    if faltantes:
        print("   · sin página detectada: " + "; ".join(faltantes[:8]))
    compilar(paginas, "final")
    paginas2 = mapa_de_paginas(a_pdf())
    if paginas2 != paginas:
        print("   · la paginación se movió; recompilando con las páginas corregidas…")
        compilar(paginas2, "ajustada")
        paginas3 = mapa_de_paginas(a_pdf())
        print(f"4/4 Estabilizado: {paginas3 == paginas2} "
              f"(coincidencias {len(paginas3)}/{len(ENCABEZADOS)})")
    else:
        print("4/4 Paginación estable tras el índice.")
    print(f"Listo: {SALIDA}")
    print(f"Tamaño: {os.path.getsize(SALIDA) / 1024 / 1024:.2f} MB")
    return 0


if __name__ == "__main__":
    sys.exit(main())
