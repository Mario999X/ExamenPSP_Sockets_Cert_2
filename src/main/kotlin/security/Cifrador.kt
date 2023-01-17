package security

import com.toxicbakery.bcrypt.Bcrypt


private const val SALT = 12

object Cifrador {

    fun cifraPassword(password: String): ByteArray {
        return Bcrypt.hash(password, SALT)
    }
}