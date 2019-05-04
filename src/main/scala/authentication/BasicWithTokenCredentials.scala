package authentication

import akka.http.scaladsl.model.headers.{BasicHttpCredentials, HttpCredentials}

object BasicWithTokenCredentials {
  def apply(credentials: Option[HttpCredentials]): Option[BasicWithTokenCredentials] = {
    for {
      BasicHttpCredentials(username, passToken) <- credentials
      (password, token) <- splitLast(passToken, ':')
    } yield BasicWithTokenCredentials(username, password, token)
  }

  private def splitLast(string: String, splitter: Int) = {
    string.lastIndexOf(':') match {
      case -1 => None
      case index => Some(string.substring(0, index), string.substring(index + 1))
    }
  }
}

case class BasicWithTokenCredentials(username: String, password: String, token: String)
