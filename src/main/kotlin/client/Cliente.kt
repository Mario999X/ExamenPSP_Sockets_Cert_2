package client

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Nave
import models.mensajes.Request
import models.mensajes.Response
import models.mensajes.ResponseType
import models.mensajes.TypeRequest
import security.FicheroProperties
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.math.BigInteger
import java.net.InetAddress
import java.nio.file.Paths
import java.security.cert.X509Certificate
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

private val json = Json

// Informacion cliente y conexion
private lateinit var direccion: InetAddress
private lateinit var clienteFactory: SSLSocketFactory
private lateinit var servidor: SSLSocket
private const val PORT = 6969

private lateinit var request: Request<Nave>

private var salida = false

private var token: String? = null

fun main() {
    while (!salida) {
        if (token == null) {
            token = solicitarToken()
        } else {
            solicitud()
        }
    }
}

private fun solicitud() {
    // Menu
    println(
        """
            
        1.ENVIAR NAVE
        2.LUKE
        3.BB8
        4.SALIR
    """.trimIndent()
    )
    val opcion = readln().toInt()

    when (opcion) {
        1 -> {
            println("\nEnviando nave")
            request = Request(token, NavesRandom.init(), null, null, TypeRequest.SEND_NAVE)
        }

        2 -> {
            println("\nSolicitando informacion Luke")
            request = Request(token, null, null, null, TypeRequest.GET_LUKE)
        }

        3 -> {
            println("\nSolicitando informacion BB8")
            request = Request(token, null, null, null, TypeRequest.GET_BB8)
        }

        4 -> {
            println("\nSaliendo de la APP")
        }
    }

    if (opcion == 4) {
        salida = true
    } else {
        // Preparamos conexion
        prepararConexion()

        // Preparamos los canales
        val salida = DataOutputStream(servidor.outputStream)
        val entrada = DataInputStream(servidor.inputStream)

        // Enviamos el request
        println("\n Enviado: $request")
        salida.writeUTF(json.encodeToString(request) + "\n")

        // Esperamos la respuesta
        val response: Response<String> = json.decodeFromString(entrada.readUTF())
        println("\nRespuesta: ${response.content}")

        if (response.responseType == ResponseType.TOKEN_EXPIRED) token = null
    }
}

private fun solicitarToken(): String? {
    // Usuario
    val name = "Mario"
    val password = "Hola1"

    // Respuesta
    val response: String?

    // Preparamos Conexion
    prepararConexion()

    // Preparamos los canales
    val salida = DataOutputStream(servidor.outputStream)
    val entrada = DataInputStream(servidor.inputStream)

    // Request Login
    request = Request(null, null, name, password, TypeRequest.LOGIN)
    salida.writeUTF(json.encodeToString(request) + "\n")
    println("Se envio: $request")

    // Esperamos respuesta
    val responseToken: Response<String> = json.decodeFromString(entrada.readUTF())

    response = if (responseToken.content != null) {
        println("\tSe recibio token")
        responseToken.content
    } else {
        println("\tEl usuario no existe")
        null
    }
    return response
}

private fun prepararConexion() {
    val workingDir: String = System.getProperty("user.dir")

    val fichero =
        Paths.get(workingDir + File.separator + "clientCert" + File.separator + "llaveroPSP_Client.p12").toString()

    val properties = FicheroProperties.loadProperties()

    System.setProperty("javax.net.ssl.trustStore", fichero)
    System.setProperty("javax.net.ssl.trustStorePassword", properties.getProperty("clave.client"))

    direccion = InetAddress.getLocalHost()
    clienteFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
    servidor = clienteFactory.createSocket(direccion, PORT) as SSLSocket

    // Informacion sesion
    informacionSesion(servidor)
}

private fun informacionSesion(servidor: SSLSocket) {
    val sesion = servidor.session
    // SERVIDOR
    println(
        """
        -Servidor: ${sesion.peerHost}
        -Cifrado: ${sesion.cipherSuite}
        -Protocolo: ${sesion.protocol}
        -IDentificador: ${BigInteger(sesion.id)}
        -Creacion de la sesion: ${sesion.creationTime}
        
    """.trimIndent()
    )
    // CERTIFICADO
    val certificado = sesion.peerCertificates[0] as X509Certificate
    println(
        """
        -Propietario: ${certificado.subjectX500Principal}
        -Algoritmo: ${certificado.sigAlgName}
        -Tipo: ${certificado.type}
        -Emisor: ${certificado.issuerX500Principal}
        -Numero de serie: ${certificado.serialNumber}
        
    """.trimIndent()
    )
}