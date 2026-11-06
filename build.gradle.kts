// Build raíz del proyecto "Registro de Entrada".
// Solo declara los plugins; cada módulo los aplica con `alias(...)`.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
