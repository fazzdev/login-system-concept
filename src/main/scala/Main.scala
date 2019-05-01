import database.connection.PostgreSqlConnection
import model.User
import play.api.libs.json.Json

object Main extends App {
  val connection = new PostgreSqlConnection()

  val user = connection.find("Mark")

  implicit val userFormat = Json.format[User]
  println(Json.toJson(user))
}