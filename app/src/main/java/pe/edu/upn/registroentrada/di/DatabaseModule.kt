package pe.edu.upn.registroentrada.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upn.registroentrada.data.local.AppDatabase
import pe.edu.upn.registroentrada.data.local.dao.AsistenteDao
import pe.edu.upn.registroentrada.data.local.dao.EntradaDao
import pe.edu.upn.registroentrada.data.local.dao.EventoDao
import pe.edu.upn.registroentrada.data.local.dao.OperadorDao
import pe.edu.upn.registroentrada.data.local.dao.RegistroAccesoDao
import pe.edu.upn.registroentrada.data.local.dao.TipoEntradaDao
import pe.edu.upn.registroentrada.data.local.seed.DatosIniciales

/** Provee la base de datos Room y sus DAO (HT-03). */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun proveerBaseDeDatos(
        @ApplicationContext context: Context,
        datosIniciales: DatosIniciales
    ): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NOMBRE)
        .addCallback(datosIniciales)
        .addMigrations(*AppDatabase.MIGRACIONES)
        .build()

    @Provides
    fun proveerEventoDao(db: AppDatabase): EventoDao = db.eventoDao()

    @Provides
    fun proveerTipoEntradaDao(db: AppDatabase): TipoEntradaDao = db.tipoEntradaDao()

    @Provides
    fun proveerAsistenteDao(db: AppDatabase): AsistenteDao = db.asistenteDao()

    @Provides
    fun proveerEntradaDao(db: AppDatabase): EntradaDao = db.entradaDao()

    @Provides
    fun proveerRegistroAccesoDao(db: AppDatabase): RegistroAccesoDao = db.registroAccesoDao()

    @Provides
    fun proveerOperadorDao(db: AppDatabase): OperadorDao = db.operadorDao()
}
