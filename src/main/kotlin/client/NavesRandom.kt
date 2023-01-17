package client

import models.Nave
import models.TipoNave

object NavesRandom {

    fun init(): Nave {
        val tipo = getTipo()
        val salto = getSaltoHiper()
        return Nave(0, tipo, salto)
    }

    private fun getTipo(): TipoNave {
        val aleatorio: Int = (1..2).random()
        val tipoNave = if (aleatorio == 1) {
            TipoNave.X_WIND
        } else TipoNave.T_FIGHTER
        return tipoNave
    }

    private fun getSaltoHiper(): Boolean {
        val aleatorio: Int = (1..2).random()
        return aleatorio != 1
    }
}