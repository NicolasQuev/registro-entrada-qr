package pe.edu.upn.registroentrada.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import pe.edu.upn.registroentrada.domain.model.Aforo
import pe.edu.upn.registroentrada.ui.theme.FuenteMono
import pe.edu.upn.registroentrada.ui.theme.coloresSenal

/*
 * Componentes reutilizables del sistema "Pase" (HT-02). Son "tontos": no
 * conocen ViewModel ni repositorios.
 */

/** Encabezado de sección con el tick de señalética. */
@Composable
fun TituloSenal(texto: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(width = 4.dp, height = 18.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.size(8.dp))
        Text(texto, style = MaterialTheme.typography.titleLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    onVolver: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(width = 4.dp, height = 20.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.size(10.dp))
                Column {
                    Text(titulo, style = MaterialTheme.typography.titleLarge)
                    if (subtitulo != null) {
                        Text(
                            subtitulo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        navigationIcon = {
            if (onVolver != null) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        actions = acciones,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun TarjetaApp(
    modifier: Modifier = Modifier,
    colorBanda: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            if (colorBanda != null) {
                Box(
                    Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .background(colorBanda)
                )
            }
            Column(Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
fun BotonPrimario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = MaterialTheme.shapes.small,
        contentPadding = ButtonDefaults.ContentPadding,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.size(10.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun CampoTextoValidado(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    soporte: String? = null,
    soloNumeros: Boolean = false,
    esPin: Boolean = false,
    habilitado: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        isError = error != null,
        enabled = habilitado,
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        supportingText = {
            when {
                error != null -> Text(error)
                soporte != null -> Text(soporte)
            }
        },
        visualTransformation = if (esPin) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (soloNumeros) KeyboardType.Number else KeyboardType.Text
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/** Sello de estado: contorno, mayúsculas y espaciado de credencial. */
@Composable
fun ChipEstado(texto: String, color: Color) {
    Text(
        text = texto.uppercase(),
        color = color,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .border(1.5.dp, color, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/** Código en monoespaciada sobre fondo tenue, para códigos y horas. */
@Composable
fun EtiquetaMono(texto: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(
            text = texto,
            fontFamily = FuenteMono,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

/** Barra de aforo segmentada, legible a distancia. */
@Composable
fun BarraAforo(aforo: Aforo, modifier: Modifier = Modifier) {
    val senal = coloresSenal()
    val color = when {
        aforo.completo -> senal.rechazo
        aforo.porcentaje >= 0.8f -> senal.atencion
        else -> senal.acceso
    }
    val segmentos = 24
    val llenos = (aforo.porcentaje.coerceIn(0f, 1f) * segmentos).roundToInt()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                "OCUPADO",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "${aforo.ocupados} / ${aforo.maximo}",
                fontFamily = FuenteMono,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(segmentos) { indice ->
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (indice < llenos) color else MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${(aforo.porcentaje * 100).roundToInt()}% ocupado",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (aforo.sobrecupo > 0) {
                Text(
                    "SOBRECUPO +${aforo.sobrecupo}",
                    style = MaterialTheme.typography.labelSmall,
                    color = senal.rechazo
                )
            }
        }
    }
}

/** Línea perforada que separa el talón de la entrada. */
@Composable
fun LineaPerforada(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp)) {
        val guion = 7.dp.toPx()
        val hueco = 6.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = color,
                start = Offset(x, size.height / 2f),
                end = Offset((x + guion).coerceAtMost(size.width), size.height / 2f),
                strokeWidth = size.height
            )
            x += guion + hueco
        }
    }
}

@Composable
fun EstadoVacio(
    mensaje: String,
    modifier: Modifier = Modifier,
    textoBoton: String? = null,
    onBoton: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = mensaje,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (textoBoton != null && onBoton != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBoton, shape = MaterialTheme.shapes.small) {
                Text(textoBoton, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun IndicadorCargando(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    textoConfirmar: String = "Confirmar"
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        shape = MaterialTheme.shapes.large,
        title = { Text(titulo, style = MaterialTheme.typography.titleLarge) },
        text = { Text(mensaje) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(textoConfirmar, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}
