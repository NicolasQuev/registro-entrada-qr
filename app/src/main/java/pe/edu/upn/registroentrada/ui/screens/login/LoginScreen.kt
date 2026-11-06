package pe.edu.upn.registroentrada.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.registroentrada.domain.model.Operador
import pe.edu.upn.registroentrada.domain.model.RolOperador
import pe.edu.upn.registroentrada.ui.components.BotonPrimario
import pe.edu.upn.registroentrada.ui.components.CampoTextoValidado
import pe.edu.upn.registroentrada.ui.components.EstadoVacio
import pe.edu.upn.registroentrada.ui.components.IndicadorCargando
import pe.edu.upn.registroentrada.ui.theme.FuenteMono

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(width = 6.dp, height = 34.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.size(12.dp))
                Column {
                    Text(
                        "REGISTRO DE ENTRADA",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "CONTROL DE ACCESO · OFFLINE",
                        fontFamily = FuenteMono,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(36.dp))
            Text("Personal de puerta", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            when {
                estado.cargando && estado.operadores.isEmpty() -> IndicadorCargando()
                estado.operadores.isEmpty() -> EstadoVacio("No hay operadores activos")
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(estado.operadores, key = { it.id }) { operador ->
                            CredencialOperador(
                                operador = operador,
                                seleccionado = estado.operadorSeleccionado?.id == operador.id,
                                onClick = { viewModel.seleccionar(operador) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            val seleccionado = estado.operadorSeleccionado
            if (seleccionado != null) {
                CampoTextoValidado(
                    valor = estado.pin,
                    onValorChange = viewModel::cambiarPin,
                    etiqueta = "PIN de ${seleccionado.nombre}",
                    error = estado.error,
                    soloNumeros = true,
                    esPin = true
                )
                Spacer(Modifier.height(14.dp))
                BotonPrimario(
                    texto = "Ingresar a puerta",
                    onClick = viewModel::iniciarSesion,
                    cargando = estado.cargando,
                    habilitado = estado.pin.length >= 4
                )
            } else {
                Text(
                    "Selecciona tu usuario para continuar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CredencialOperador(
    operador: Operador,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val borde = if (seleccionado) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .border(
                width = if (seleccionado) 2.dp else 1.dp,
                color = borde,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (seleccionado) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iniciales(operador.nombre),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(Modifier.size(14.dp))
            Column {
                Text(operador.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (operador.rol == RolOperador.ADMINISTRADOR) "ADMINISTRADOR" else "PORTERO",
                    fontFamily = FuenteMono,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun iniciales(nombre: String): String =
    nombre.trim().split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("").ifBlank { "?" }
