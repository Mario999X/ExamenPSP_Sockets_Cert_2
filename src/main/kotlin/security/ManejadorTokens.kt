package security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import models.TipoUser
import java.util.*

object ManejadorTokens {

    private val properties = FicheroProperties.loadProperties()

    // Algoritmo
    private val algoritmo = Algorithm.HMAC256(properties.getProperty("secreto.jwt"))

    fun createToken(rol: TipoUser): String {
        val token: String = JWT.create()
            .withClaim("rol", rol.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + 60000))
            .sign(algoritmo)

        return token
    }

    fun decodeToken(token: String): DecodedJWT? {
        val verifier = JWT.require(algoritmo)
            .build()

        val tokenDecode = try {
            verifier.verify(token)
        }catch (_: Exception) {
            null
        }

        return tokenDecode
    }

}