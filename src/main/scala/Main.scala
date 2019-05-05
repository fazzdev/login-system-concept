import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import api.RestApp
import database.connection.PostgreSqlConnection

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object Main extends App {
  private implicit val system: ActorSystem = ActorSystem("auth-system")
  private implicit val executor: ExecutionContext = system.dispatcher
  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  private val connection = new PostgreSqlConnection()
  private val restApp = new RestApp(connection)
  private val (host, port) = ("localhost", 9000)

  private val bindingFuture = Http().bindAndHandle(restApp.route, host, port)
  bindingFuture.onComplete {
    case Success(serverBinding) => println(s"listening to ${serverBinding.localAddress}")
    case Failure(error) => println(s"error: ${error.getMessage}")
  }
}