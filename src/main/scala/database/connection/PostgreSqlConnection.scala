package database.connection

import cats.effect.{ContextShift, IO}
import doobie._
import doobie.implicits._
import model.{Session, Token, User}

import scala.concurrent.ExecutionContext

class PostgreSqlConnection {
  private implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  private val transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:postgres", "postgres", "postgres")

  private def runSync[T](query: ConnectionIO[T]): T =
    query.transact(transactor).unsafeRunSync()

  def users(): Vector[User] =
    runSync(sql"select username, password from authuser".query[User].to[Vector])

  def user(username: String, password: String): Option[User] =
    runSync(sql"select username, password from authuser where username = $username and password = $password".query[User].option)

  def user(token: Token): Option[User] =
    runSync(sql"select username from session where token = ${token.token}".query[String].option)
      .map(username => User(username))

  def loggedInUser(): Vector[User] =
    runSync(sql"select username from session".query[String].to[Vector])
      .map(username => User(username))

  def mate(currentUser: User): Vector[User] =
    (runSync(sql"select username from mate where mate = ${currentUser.username}".query[String].to[Vector]) ++
      runSync(sql"select mate from mate where username = ${currentUser.username}".query[String].to[Vector]))
      .distinct
      .map(username => User(username))

  def isMate(currentUser: User, otherUser: User): Boolean =
    mate(currentUser).contains(otherUser)

  def loggedInMate(currentUser: User): Vector[User] = {
    val mates = mate(currentUser)
    loggedInUser().collect { case otherUser: User if mates.contains(otherUser) => otherUser }
  }

  def addSession(session: Session): Int = {
    val Token(token, Some(issuedDate), Some(expiredDate)) = session.token
    val User(username, _) = session.user
    
    runSync(sql"insert into session (token, issueddate, expireddate, username) values ($token, ${issuedDate.toInstant}, ${expiredDate.toInstant}, $username)".update.run)
  }
}