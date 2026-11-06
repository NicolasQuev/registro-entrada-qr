package pe.edu.upn.registroentrada.data.remote.api

import pe.edu.upn.registroentrada.data.remote.dto.EventoDocumentDto
import pe.edu.upn.registroentrada.data.remote.dto.EventoRequestDto
import pe.edu.upn.registroentrada.data.remote.dto.FirestoreListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Contrato de Endpoints REST para el consumo con Retrofit.
 * Conecta con el backend en la nube (Cloud Firestore REST API v1).
 */
interface EventosApiService {

    /**
     * READ (Listar todos los eventos)
     * GET /v1/projects/{projectId}/databases/(default)/documents/eventos
     */
    @GET("v1/projects/{projectId}/databases/(default)/documents/eventos")
    suspend fun listarEventos(
        @Path("projectId") projectId: String,
        @Query("key") apiKey: String
    ): Response<FirestoreListResponse<EventoDocumentDto>>

    /**
     * READ (Obtener evento específico por ID)
     * GET /v1/projects/{projectId}/databases/(default)/documents/eventos/{documentId}
     */
    @GET("v1/projects/{projectId}/databases/(default)/documents/eventos/{documentId}")
    suspend fun obtenerEvento(
        @Path("projectId") projectId: String,
        @Path("documentId") documentId: String,
        @Query("key") apiKey: String
    ): Response<EventoDocumentDto>

    /**
     * CREATE (Crear nuevo evento en la nube)
     * POST /v1/projects/{projectId}/databases/(default)/documents/eventos
     */
    @POST("v1/projects/{projectId}/databases/(default)/documents/eventos")
    suspend fun crearEvento(
        @Path("projectId") projectId: String,
        @Query("documentId") documentId: String?,
        @Query("key") apiKey: String,
        @Body request: EventoRequestDto
    ): Response<EventoDocumentDto>

    /**
     * UPDATE (Actualizar datos de un evento en la nube)
     * PATCH /v1/projects/{projectId}/databases/(default)/documents/eventos/{documentId}
     */
    @PATCH("v1/projects/{projectId}/databases/(default)/documents/eventos/{documentId}")
    suspend fun actualizarEvento(
        @Path("projectId") projectId: String,
        @Path("documentId") documentId: String,
        @Query("key") apiKey: String,
        @Body request: EventoRequestDto
    ): Response<EventoDocumentDto>

    /**
     * DELETE (Eliminar evento de la nube)
     * DELETE /v1/projects/{projectId}/databases/(default)/documents/eventos/{documentId}
     */
    @DELETE("v1/projects/{projectId}/databases/(default)/documents/eventos/{documentId}")
    suspend fun eliminarEvento(
        @Path("projectId") projectId: String,
        @Path("documentId") documentId: String,
        @Query("key") apiKey: String
    ): Response<Unit>
}
