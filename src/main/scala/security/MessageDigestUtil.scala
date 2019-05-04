package security

import java.math.BigInteger
import java.nio.charset.StandardCharsets._
import java.security.MessageDigest

object MessageDigestUtil {
  def sha256(string: String) = {
    val digest = MessageDigest.getInstance("SHA-256").digest(string.getBytes(UTF_8))
    String.format("%032x", new BigInteger(1, digest))
  }
}
