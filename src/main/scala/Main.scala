import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.HttpCredentials
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import akka.stream.ActorMaterializer
import authentication.{BasicWithTokenCredentials, SharedSecretGenerator}
import database.connection.PostgreSqlConnection
import model.{Session, Token, User}
import play.api.libs.json.{Json, OFormat}
import security.MessageDigestUtil._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Main extends App {
  private val connection = new PostgreSqlConnection()
  private val (host, port) = ("localhost", 9000)
  private val realm = "asgardian area"

  private implicit val system: ActorSystem = ActorSystem("auth-system")
  private implicit val executor: ExecutionContext = system.dispatcher
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val userFormat: OFormat[User] = Json.format[User]
  private implicit val tokenFormat: OFormat[Token] = Json.format[Token]

  private def basicAuthenticator(rawCredentials: Option[HttpCredentials])(basicCredentials: Credentials): Option[User] = {
    // ideally, this logic uses Credentials.Provided(id).verify("password") to avoid timing attack
    BasicWithTokenCredentials(rawCredentials).flatMap { value =>
      val BasicWithTokenCredentials(username, password, token) = value

      def isSharedSecretToken = SharedSecretGenerator.isCurrentSharedSecret(token)

      connection.user(username, sha256(password)).flatMap { loginUser =>
        connection.user(Token(token)) match {
          case Some(mateUser) if connection.isMate(loginUser, mateUser) => Some(loginUser)
          case _ if isSharedSecretToken && connection.loggedInMate(loginUser).isEmpty => Some(loginUser) // First person to log in
          case _ => None
        }
      }
    }
  }

  private def tokenAuthenticator(credentials: Credentials): Option[Session] = ???

  private def createNewToken(user: User) = {
    val newToken = Token()
    val newSession = Session(newToken, user)
    connection.addSession(newSession)

    complete(HttpEntity(ContentTypes.`application/json`, Json.toJson(newToken).toString()))
  }

  private def allUsers(session: Session) =
    complete(HttpEntity(ContentTypes.`application/json`, Json.toJson(connection.users()).toString()))

  private def currentUser(session: Session) =
    complete(HttpEntity(ContentTypes.`application/json`, Json.toJson(connection.user(session.token)).toString()))

  // TODO add test for /authenticate
  private def route =
    path("authenticate") {
      get {
        extractCredentials { credentials => authenticateBasic(realm, basicAuthenticator(credentials))(createNewToken) }
      }
    } ~
      path("users") {
        get {
          authenticateOAuth2(realm, tokenAuthenticator)(allUsers)
        }
      } ~
      path("users" / "current") {
        get {
          authenticateOAuth2(realm, tokenAuthenticator)(currentUser)
        }
      }


  val bindingFuture = Http().bindAndHandle(route, host, port)
  bindingFuture.onComplete {
    case Success(serverBinding) => println(s"listening to ${serverBinding.localAddress}")
    case Failure(error) => println(s"error: ${error.getMessage}")
  }
}