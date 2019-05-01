package database.connection

import doobie._
import doobie.implicits._
import cats.effect.{ContextShift, IO}
import model.User

import scala.concurrent.ExecutionContext

class PostgreSqlConnection {
  private implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  private val transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:postgres", "postgres", "postgres")

  def runSync[T](query: ConnectionIO[T]): T =
    query.transact(transactor).unsafeRunSync()

  def users(): List[User] =
    runSync(sql"select username, password from authuser".query[User].to[List])

  def findUser(username: String): Option[User] =
    runSync(sql"select username, password from authuser where username = $username".query[User].option)
}