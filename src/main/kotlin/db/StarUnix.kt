package db

import models.Nave
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class StarUnix {

    private val registrosNaves = mutableMapOf<Int, Nave>()
    private val id = AtomicInteger(0)
    private val misiles = AtomicInteger(0)

    private val lock = ReentrantLock()
    private val registerNave = lock.newCondition()
    private val lecturaInfo = lock.newCondition()

    private var escritor = false
    private var lectores = AtomicInteger(0)


    fun addNave(nave: Nave) {
        lock.withLock {
            while (lectores.toInt() > 0) {
                registerNave.await()
            }
            escritor = true

            id.incrementAndGet()

            misiles.addAndGet(nave.misilesProtonicos)

            nave.id = id.toInt()
            registrosNaves[id.toInt()] = nave

            println("Nave agregada -> $nave")

            escritor = false
            lecturaInfo.signalAll()
        }
    }

    fun getListaNaves(): String {
        lock.withLock {
            while (escritor) {
                lecturaInfo.await()
            }
            lectores.incrementAndGet()

            val listado = registrosNaves.values.toString()

            println("Enviando listado compelto: $listado")

            lectores.decrementAndGet()
            registerNave.signalAll()

            return listado
        }
    }

    fun getInfoEspecifica(): String {
        lock.withLock {
            while (escritor) {
                lecturaInfo.await()
            }
            lectores.incrementAndGet()

            val mensaje = "Informacion: Total naves: ${registrosNaves.size} | Total misiles: $misiles"

            println("Enviando mensaje: $mensaje")

            lectores.decrementAndGet()
            registerNave.signalAll()

            return mensaje
        }
    }
}