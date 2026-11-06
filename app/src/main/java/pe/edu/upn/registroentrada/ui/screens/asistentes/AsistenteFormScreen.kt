package pe.edu.upn.registroentrada.ui.screens.asistentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.registroentrada.ui.components.BarraSuperior
import pe.edu.upn.registroentrada.ui.components.BotonPrimario
import pe.edu.upn.registroentrada.ui.components.CampoTextoValidado
import pe.edu.upn.registroentrada.ui.components.TituloSenal

@Composable
fun AsistenteFormScreen(
    onVolver: () -> Unit,
    onCreado: () -> Unit,
    viewModel: AsistenteFormViewModel = hiltViewModel()
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSuperior(titulo = "Nuevo asistente", onVolver = onVolver) }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TituloSenal("Identificación")
            CampoTextoValidado(
                valor = estado.documento,
                onValorChange = viewModel::cambiarDocumento,
                etiqueta = "Documento (DNI, 8 dígitos)",
                error = estado.errorDocumento,
                soloNumeros = true
            )
            CampoTextoValidado(
                valor = estado.nombres,
                onValorChange = viewModel::cambiarNombres,
                etiqueta = "Nombres",
                error = estado.errorNombres
            )
            CampoTextoValidado(
                valor = estado.apellidos,
                onValorChange = viewModel::cambiarApellidos,
                etiqueta = "Apellidos",
                error = estado.errorApellidos
            )
            Spacer(Modifier.height(4.dp))
            TituloSenal("Contacto (opcional)")
            CampoTextoValidado(
                valor = estado.correo,
                onValorChange = viewModel::cambiarCorreo,
                etiqueta = "Correo",
                error = estado.errorCorreo
            )
            CampoTextoValidado(
                valor = estado.telefono,
                onValorChange = viewModel::cambiarTelefono,
                etiqueta = "Teléfono",
                error = estado.errorTelefono,
                soloNumeros = true
            )
            estado.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(12.dp))
            BotonPrimario(
                texto = "Guardar asistente",
                onClick = { viewModel.guardar(onCreado) },
                cargando = estado.guardando,
                habilitado = !estado.guardando
            )
        }
    }
}
