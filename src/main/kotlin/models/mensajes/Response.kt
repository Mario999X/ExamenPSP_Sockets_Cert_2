package models.mensajes

import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    val content: T?,
    val responseType: ResponseType
)

enum class ResponseType { OK, ERROR, TOKEN_EXPIRED }