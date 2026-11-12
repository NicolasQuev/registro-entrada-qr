# Reglas de ProGuard del proyecto Registro de Entrada.
# Preserva firmas genéricas, anotaciones y DTOs/modelos usados por Retrofit, Gson y Room.
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses

# DTOs de Firestore REST API y campos serializados con Gson
-keep class pe.edu.upn.registroentrada.data.remote.dto.** { *; }
-keep interface pe.edu.upn.registroentrada.data.remote.api.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Entidades de Room para consultas e índices
-keep class pe.edu.upn.registroentrada.data.local.entity.** { *; }
