package server

import db.StarUnix
import db.UserDB
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Nave
import models.TipoUser
import models.mensajes.Request
import models.mensajes.Response
import models.mensajes.ResponseType
import models.mensajes.TypeRequest
import security.ManejadorTokens
import java.io.DataInputStream
import java.io.DataOutputStream
import javax.net.ssl.SSLSocket

private val json = Json

class GestorClientes(private val cliente: SSLSocket, private val userDB: UserDB, private val starUnix: StarUnix) :
    Runnable {

    // Canales
    private val salida = DataOutputStream(cliente.outputStream)
    private val entrada = DataInputStream(cliente.inputStream)

    private var tokenExpired = false

    override fun run() {
        val request = lecturaRequest()

        val permisos = comprobarToken(request)

        if (!tokenExpired) {
            when (request.type) {
                TypeRequest.LOGIN -> enviarToken(request)
                TypeRequest.SEND_NAVE -> introducirNave(request)
                TypeRequest.GET_LUKE -> enviarListado(permisos)
                TypeRequest.GET_BB8 -> enviarInformacion(permisos)
            }
        } else tokenExpiredSignal()
        cliente.close()
    }

    private fun enviarInformacion(permisos: Boolean) {
        println("\tEnviando informacion")
        if (permisos) {
            val informacion = starUnix.getInfoEspecifica()
            salida.writeUTF(json.encodeToString(Response(informacion, ResponseType.OK)))
        } else salida.writeUTF(json.encodeToString(Response("No tiene permisos", ResponseType.ERROR)))
    }

    private fun enviarListado(permisos: Boolean) {
        println("\tEnviando listado")
        if (permisos) {
            val listado = starUnix.getListaNaves()
            salida.writeUTF(json.encodeToString(Response(listado, ResponseType.OK)))
        } else salida.writeUTF(json.encodeToString(Response("No tiene permisos", ResponseType.ERROR)))
    }

    private fun introducirNave(request: Request<Nave>) {
        println("\tIntroduciendo nave")

        val registro = request.content

        starUnix.addNave(registro!!)

        salida.writeUTF(json.encodeToString(Response("$registro agregada", ResponseType.OK)))
    }

    private fun enviarToken(request: Request<Nave>) {
        println("\tComprobando usuario")

        val user = userDB.loginUser(request.content2!!, request.content3!!)

        val response = if (user == null) {
            println("\tUsuario no encontrado")
            Response(null, ResponseType.ERROR)
        } else {
            println("\tUsuario encontrado -> Creando Token")
            val token = ManejadorTokens.createToken(user.rol)
            Response(token, ResponseType.OK)
        }
        salida.writeUTF(json.encodeToString(response) + "\n")
    }

    private fun tokenExpiredSignal() {
        println("\tToken caducado")

        val response = Response("Token caducado", ResponseType.TOKEN_EXPIRED)
        salida.writeUTF(json.encodeToString(response) + "\n")
    }

    private fun comprobarToken(request: Request<Nave>): Boolean {
        println("\tComprobando token...")

        var funcionDisponible = true

        val token = request.token?.let { ManejadorTokens.decodeToken(it) }

        if (token != null) {
            if (token.getClaim("rol").toString().contains(TipoUser.USER.toString())) {
                funcionDisponible = false
            }
        } else if (request.type != TypeRequest.LOGIN) {
            tokenExpired = true
        }
        return funcionDisponible
    }


    private fun lecturaRequest(): Request<Nave> {
        println("\tProcesando request...")
        return json.decodeFromString(entrada.readUTF())
    }

}