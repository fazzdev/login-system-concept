package authentication

object SharedSecretGenerator {
  // Ideally, this is not a static shared secret but a proper shared secret and hashed with time (like google authenticator)
  def isCurrentSharedSecret(sharedSecret: String): Boolean = sharedSecret == "123456"
}
