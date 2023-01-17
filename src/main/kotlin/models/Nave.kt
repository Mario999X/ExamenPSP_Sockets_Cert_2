package models

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Nave(
    var id: Int = 0,
    val tipoNave: TipoNave,
    val hiperEspacio: Boolean,
    val misilesProtonicos: Int = (10..20).random(),
    val fechaCreacion: String = LocalDate.now().toString()

) {
    override fun toString(): String {
        return "Nave(id=$id, tipoNave=$tipoNave, hiperEspacio=$hiperEspacio, misilesProtonicos=$misilesProtonicos, fechaCreacion='$fechaCreacion')"
    }
}

enum class TipoNave { X_WIND, T_FIGHTER }
