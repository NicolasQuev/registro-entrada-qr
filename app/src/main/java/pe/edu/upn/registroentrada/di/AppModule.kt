package pe.edu.upn.registroentrada.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upn.registroentrada.core.FirmadorQr
import pe.edu.upn.registroentrada.core.FirmadorQrHmac
import pe.edu.upn.registroentrada.core.GeneradorId
import pe.edu.upn.registroentrada.core.GeneradorIdUuid
import pe.edu.upn.registroentrada.core.Hasher
import pe.edu.upn.registroentrada.core.Reloj
import pe.edu.upn.registroentrada.core.RelojSistema
import pe.edu.upn.registroentrada.data.local.seed.DatosIniciales

/** Provee los servicios transversales del dominio. */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Clave local para firmar los QR (RNF-13). En una versión de producción
     * debería vivir en un almacén cifrado (EncryptedSharedPreferences o el
     * Keystore de Android); aquí se deriva de una constante del build.
     */
    private const val SEMILLA_CLAVE_QR = "pe.edu.upn.registroentrada:clave-qr:v1"

    @Provides
    @Singleton
    fun proveerReloj(): Reloj = RelojSistema()

    @Provides
    @Singleton
    fun proveerGeneradorId(): GeneradorId = GeneradorIdUuid()

    @Provides
    @Singleton
    fun proveerFirmadorQr(): FirmadorQr =
        FirmadorQrHmac(Hasher.sha256(SEMILLA_CLAVE_QR).toByteArray(Charsets.UTF_8))

    @Provides
    @Singleton
    fun proveerDatosIniciales(reloj: Reloj): DatosIniciales = DatosIniciales(reloj)
}
