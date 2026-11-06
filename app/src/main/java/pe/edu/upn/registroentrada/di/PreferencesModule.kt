package pe.edu.upn.registroentrada.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upn.registroentrada.data.preferences.ajustesDataStore

/** Provee el DataStore único de la aplicación (HT-04). */
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun proveerDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.ajustesDataStore
}
