package pe.edu.upn.registroentrada.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pe.edu.upn.registroentrada.data.local.entity.AsistenteEntity
import pe.edu.upn.registroentrada.data.local.entity.EntradaEntity
import pe.edu.upn.registroentrada.data.local.entity.EventoEntity
import pe.edu.upn.registroentrada.data.local.entity.TipoEntradaEntity

/**
 * Pruebas instrumentadas de los DAO de Room (HT-03). Se ejecutan en el
 * emulador con una base de datos en memoria:
 *   ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun crearBaseDeDatos() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun cerrarBaseDeDatos() {
        db.close()
    }

    private fun instante(minuto: Long) = Instant.parse("2026-02-01T20:00:00Z").plusSeconds(minuto * 60)

    private suspend fun sembrarEventoConEntrada(): Long {
        val eventoId = db.eventoDao().insertar(
            EventoEntity(
                nombre = "Concierto",
                descripcion = null,
                lugar = "Arena",
                fechaInicio = instante(0).toEpochMilli(),
                fechaFin = instante(180).toEpochMilli(),
                aforoMaximo = 100,
                estado = "PROGRAMADO",
                creadoEn = instante(0).toEpochMilli()
            )
        )
        val tipoId = db.tipoEntradaDao().insertar(
            TipoEntradaEntity(
                eventoId = eventoId,
                nombre = "General",
                precio = 20.0,
                cupo = 50,
                color = "#2E74B5"
            )
        )
        val asistenteId = db.asistenteDao().insertar(
            AsistenteEntity(
                documento = "12345678",
                nombres = "Ana",
                apellidos = "Quispe",
                correo = null,
                telefono = null,
                creadoEn = instante(0).toEpochMilli()
            )
        )
        db.entradaDao().insertar(
            EntradaEntity(
                codigo = "uuid-1",
                firma = "firma-1",
                eventoId = eventoId,
                tipoEntradaId = tipoId,
                asistenteId = asistenteId,
                estado = "VALIDA",
                emitidaEn = instante(10).toEpochMilli(),
                usadaEn = null
            )
        )
        return eventoId
    }

    @Test
    fun insertaYObservaEventos() = runBlocking {
        sembrarEventoConEntrada()

        val eventos = db.eventoDao().observarTodos().first()

        assertEquals(1, eventos.size)
        assertEquals("Concierto", eventos.single().nombre)
    }

    @Test
    fun consultaEntradaPorCodigoConNombresDeJoin() = runBlocking {
        sembrarEventoConEntrada()

        val fila = db.entradaDao().buscarPorCodigo("uuid-1")

        assertNotNull(fila)
        assertEquals("Ana Quispe", fila!!.nombreAsistente)
        assertEquals("General", fila.nombreTipoEntrada)
        assertEquals("Concierto", fila.nombreEvento)
    }

    @Test
    fun cuentaEntradasVigentesYPorEstado() = runBlocking {
        val eventoId = sembrarEventoConEntrada()

        assertEquals(1, db.entradaDao().contarVigentesPorTipo(1))
        assertEquals(1, db.entradaDao().contarPorEventoYEstado(eventoId, "VALIDA"))
        assertTrue(db.entradaDao().existeValida(1, eventoId))
    }

    @Test
    fun elDocumentoDeAsistenteEsUnico() = runBlocking {
        db.asistenteDao().insertar(
            AsistenteEntity(
                documento = "87654321",
                nombres = "Luis",
                apellidos = "Ramos",
                correo = null,
                telefono = null,
                creadoEn = instante(0).toEpochMilli()
            )
        )

        var fallo = false
        try {
            db.asistenteDao().insertar(
                AsistenteEntity(
                    documento = "87654321",
                    nombres = "Otro",
                    apellidos = "Ramos",
                    correo = null,
                    telefono = null,
                    creadoEn = instante(1).toEpochMilli()
                )
            )
        } catch (_: android.database.sqlite.SQLiteConstraintException) {
            fallo = true
        }
        assertTrue("El índice único del documento debió impedir el duplicado", fallo)
    }
}
