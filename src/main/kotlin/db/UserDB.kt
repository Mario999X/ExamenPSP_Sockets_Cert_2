package db

import com.toxicbakery.bcrypt.Bcrypt
import models.Usuario
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class UserDB {
    private val registrosUsers = mutableMapOf<Int, Usuario>()
    private val id = AtomicInteger(0)

    private val lock = ReentrantLock()
    private val registerUser = lock.newCondition()
    private val loginUser = lock.newCondition()

    private var escritor = false
    private var lectores = AtomicInteger(0)

    fun registerUser(usuario: Usuario) {
        lock.withLock {
            while (lectores.toInt() > 0) {
                registerUser.await()
            }
            escritor = true

            id.incrementAndGet()

            usuario.id = id.toInt()
            registrosUsers[usuario.id] = usuario

            println("Usuario agregado -> $usuario")

            escritor = false
            loginUser.signalAll()
        }
    }

    fun loginUser(nombre: String, password: String): Usuario? {
        lock.withLock {
            while (escritor) {
                loginUser.await()
            }
            lectores.incrementAndGet()

            var user: Usuario? = null
            registrosUsers.forEach {
                if (it.value.nombre.equals(nombre, false) && Bcrypt.verify(password, it.value.password)) {
                    user = it.value
                    println("Usuario: $user")
                }
            }

            lectores.decrementAndGet()
            registerUser.signalAll()
            return user
        }
    }
}