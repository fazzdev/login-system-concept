import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import database.connection.PostgreSqlConnection
import model.User
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Main extends App {
  private val connection = new PostgreSqlConnection()
  private val (host, port) = ("localhost", 9000)

  private implicit val system: ActorSystem = ActorSystem("auth-system")
  private implicit val executor: ExecutionContext = system.dispatcher
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val userFormat = Json.format[User]

  def route =
    path("users") {
      get {
        complete(HttpEntity(ContentTypes.`application/json`, Json.toJson(connection.users()).toString()))
      }
    } ~
      path("users" / "current") {
        get {
          complete(HttpEntity(ContentTypes.`application/json`, Json.toJson(connection.findUser("Mark")).toString()))
        }
      }

  val bindingFuture = Http().bindAndHandle(route, host, port)
  bindingFuture.onComplete {
    case Success(serverBinding) => println(s"listening to ${serverBinding.localAddress}")
    case Failure(error) => println(s"error: ${error.getMessage}")
  }
}