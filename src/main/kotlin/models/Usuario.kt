package models

data class Usuario(
    var id: Int = 0,
    val nombre: String,
    val password: ByteArray,
    val rol: TipoUser


) {
    override fun toString(): String {
        return "Usuario(id=$id, nombre='$nombre', password=${password}, rol=$rol)"
    }
}

enum class TipoUser { USER, ADMIN }
