package pe.edu.upn.registroentrada.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upn.registroentrada.data.repository.AccesoRepositoryImpl
import pe.edu.upn.registroentrada.data.repository.AjustesRepositoryImpl
import pe.edu.upn.registroentrada.data.repository.AsistenteRepositoryImpl
import pe.edu.upn.registroentrada.data.repository.EntradaRepositoryImpl
import pe.edu.upn.registroentrada.data.repository.EventoRepositoryImpl
import pe.edu.upn.registroentrada.data.repository.OperadorRepositoryImpl
import pe.edu.upn.registroentrada.domain.repository.AccesoRepository
import pe.edu.upn.registroentrada.domain.repository.AjustesRepository
import pe.edu.upn.registroentrada.domain.repository.AsistenteRepository
import pe.edu.upn.registroentrada.domain.repository.EntradaRepository
import pe.edu.upn.registroentrada.domain.repository.EventoRepository
import pe.edu.upn.registroentrada.domain.repository.OperadorRepository

/**
 * Une las interfaces del dominio con sus implementaciones de la capa de
 * datos. Es la regla de dependencias hacia adentro (7.1).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun eventoRepository(impl: EventoRepositoryImpl): EventoRepository

    @Binds
    @Singleton
    abstract fun asistenteRepository(impl: AsistenteRepositoryImpl): AsistenteRepository

    @Binds
    @Singleton
    abstract fun entradaRepository(impl: EntradaRepositoryImpl): EntradaRepository

    @Binds
    @Singleton
    abstract fun accesoRepository(impl: AccesoRepositoryImpl): AccesoRepository

    @Binds
    @Singleton
    abstract fun operadorRepository(impl: OperadorRepositoryImpl): OperadorRepository

    @Binds
    @Singleton
    abstract fun ajustesRepository(impl: AjustesRepositoryImpl): AjustesRepository
}
