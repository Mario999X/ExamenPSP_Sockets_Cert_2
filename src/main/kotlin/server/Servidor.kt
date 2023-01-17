package server

import db.StarUnix
import db.UserDB
import models.TipoUser
import models.Usuario
import security.Cifrador
import security.FicheroProperties
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.Executors
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLServerSocketFactory
import javax.net.ssl.SSLSocket

// Informacion del servidor
private const val PORT = 6969

private lateinit var serverFactory: SSLServerSocketFactory
private lateinit var servidor: SSLServerSocket

fun main() {
    // Cliente
    var client: SSLSocket

    // Pool de hilos
    val pool = Executors.newFixedThreadPool(10)

    // Preparamos la DB de usuarios
    val userDB = UserDB()

    val users = listOf(
        Usuario(0, "Mario", Cifrador.cifraPassword("Hola1"), TipoUser.ADMIN),
        Usuario(0, "Alysys", Cifrador.cifraPassword("Hola2"), TipoUser.USER)
    )

    repeat(users.size) {
        userDB.registerUser(users[it])
    }

    // Preparamos la DB de registros de naves
    val starUnix = StarUnix()

    println("Arrancamos servidor...")

    prepararConexion()

    println("Servidor esperando...")

    while (true) {
        client = servidor.accept() as SSLSocket
        println("Peticion de cliente -> " + client.inetAddress + " --- " + client.port)

        val gc = GestorClientes(client, userDB, starUnix)
        pool.execute(gc)
    }
}

private fun prepararConexion() {
    val workingDir: String = System.getProperty("user.dir")

    val fichero = Paths.get(workingDir + File.separator + "cert" + File.separator + "llaveroPSP_Server.p12").toString()

    val properties = FicheroProperties.loadProperties()

    System.setProperty("javax.net.ssl.keyStore", fichero)
    System.setProperty("javax.net.ssl.keyStorePassword", properties.getProperty("clave.server"))

    serverFactory = SSLServerSocketFactory.getDefault() as SSLServerSocketFactory
    servidor = serverFactory.createServerSocket(PORT) as SSLServerSocket
}