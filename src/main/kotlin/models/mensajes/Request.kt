package models.mensajes

import kotlinx.serialization.Serializable

@Serializable
data class Request<T>(
    val token: String?,
    val content: T?,
    val content2: String?,
    val content3: String?,
    val type: TypeRequest
)

enum class TypeRequest { LOGIN, SEND_NAVE, GET_LUKE, GET_BB8 } // Los usuarios normales solo podran enviar naves.
