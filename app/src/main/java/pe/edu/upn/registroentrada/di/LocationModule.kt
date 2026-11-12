package pe.edu.upn.registroentrada.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upn.registroentrada.core.location.ProveedorUbicacion
import pe.edu.upn.registroentrada.core.location.ProveedorUbicacionGps

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun proveerProveedorUbicacion(
        @ApplicationContext context: Context
    ): ProveedorUbicacion = ProveedorUbicacionGps(context)
}
