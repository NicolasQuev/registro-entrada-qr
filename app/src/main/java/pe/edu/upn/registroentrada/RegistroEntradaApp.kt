package pe.edu.upn.registroentrada

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application del proyecto. Es el punto donde Hilt crea el grafo de
 * dependencias (base de datos, repositorios y servicios).
 */
@HiltAndroidApp
class RegistroEntradaApp : Application()
