package api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{BasicHttpCredentials, OAuth2BearerToken}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import database.connection.DbConnection
import model._
import org.mockito.ArgumentMatchers._
import org.mockito.MockitoSugar
import org.scalatest.{FunSuite, Matchers}
import play.api.libs.json.{Json, OFormat}

class RestAppTest extends FunSuite with Matchers with ScalatestRouteTest with MockitoSugar {
  private implicit val userFormat: OFormat[User] = Json.format[User]
  private implicit val tokenFormat: OFormat[Token] = Json.format[Token]
  private implicit val sessionFormat: OFormat[Session] = Json.format[Session]

  private def fixture = {
    val connection = mock[DbConnection]
    when(connection.users()).thenReturn(Vector(User("John"), User("Mark"), User("Bill"), User("Peter")))
    when(connection.user("John", "a8cfcd74832004951b4408cdb0a5dbcd8c7e52d43f7fe244bf720582e05241da")).thenReturn(Some(User("John")))
    when(connection.user("Mark", "d7cda0ca2c8586e512c425368fcb2bba62e81475bfceb4284f4906de8ec242bc")).thenReturn(Some(User("Mark")))
    when(connection.user("Bill", "e51783b4d7688ffba51a35d8c9f04041606c0d6fb00bb306fba0f2dcb7e1f890")).thenReturn(Some(User("Bill")))
    when(connection.user("Peter", "ea72c79594296e45b8c2a296644d988581f58cfac6601d122ed0a8bd7c02e8bf")).thenReturn(Some(User("Peter")))

    when(connection.isMate(User("John"), User("Mark"))).thenReturn(true)
    when(connection.isMate(User("John"), User("Bill"))).thenReturn(true)

    when(connection.user(Token("123456"))).thenReturn(None)

    connection
  }

  test("When authenticate for first user with shared secret, then a token should be returned") {
    // Arrange
    val connection = fixture
    when(connection.loggedInMate(User("John"))).thenReturn(Vector())

    val route = new RestApp(connection).route
    val validCredentials = BasicHttpCredentials("John", "John:123456")

    // Act
    Get("/authenticate") ~> addCredentials(validCredentials) ~> route ~> check {
      // Assert
      Json.parse(responseAs[String]).asOpt[Token] should matchPattern { case Some(Token(_, Some(_), Some(_))) => }
    }
  }

  test("When authenticate for second user with shared secret, then an invalid authentication") {
    // Arrange
    val connection = fixture
    when(connection.loggedInMate(User("John"))).thenReturn(Vector(User("Mark")))

    val route = new RestApp(connection).route
    val validCredentials = BasicHttpCredentials("John", "John:123456")

    // Act
    Get("/authenticate") ~> addCredentials(validCredentials) ~> route ~> check {
      // Assert
      status shouldEqual StatusCodes.Unauthorized
      responseAs[String] shouldEqual "The supplied authentication is invalid"
    }
  }

  test("When authenticate for second user with token of first user, then a token should be returned") {
    // Arrange
    val connection = fixture
    when(connection.session(Token("RandomToken"))).thenReturn(Some(Session(User("Mark"), Token("RandomToken"))))

    val route = new RestApp(connection).route
    val validCredentials = BasicHttpCredentials("John", "John:RandomToken")

    // Act
    Get("/authenticate") ~> addCredentials(validCredentials) ~> route ~> check {
      // Assert
      Json.parse(responseAs[String]).asOpt[Token] should matchPattern { case Some(Token(_, Some(_), Some(_))) => }
    }
  }

  test("When get /users, then retrieve all users") {
    // Arrange
    val connection = fixture
    when(connection.session(Token("RandomToken"))).thenReturn(Some(Session(User("Mark"), Token("RandomToken"))))

    val route = new RestApp(connection).route
    val validCredentials = OAuth2BearerToken("RandomToken")

    // Act
    Get("/users") ~> addCredentials(validCredentials) ~> route ~> check {
      // Assert
      Json.parse(responseAs[String]).as[List[User]] shouldEqual List(User("John"), User("Mark"), User("Bill"), User("Peter"))
    }
  }

  test("When get /secrets/1, then retrieve secret content") {
    // Arrange
    val connection = fixture
    val session = Session(User("Mark"), Token("RandomToken"))
    when(connection.session(Token("RandomToken"))).thenReturn(Some(session))
    when(connection.hasPermission(session, 1)).thenReturn(true)
    when(connection.secret(1)).thenReturn(Some(Secret(1, "This is a secret", User("Mark"))))

    val route = new RestApp(connection).route
    val validCredentials = OAuth2BearerToken("RandomToken")

    // Act
    Get("/secret/1") ~> addCredentials(validCredentials) ~> route ~> check {
      // Assert
      responseAs[String] shouldEqual "This is a secret"
    }
  }

  test("When post to /secrets, then create new secret content") {
    // Arrange
    val connection = fixture
    val session = Session(User("Mark"), Token("RandomToken"))
    when(connection.session(Token("RandomToken"))).thenReturn(Some(session))
    when(connection.addSecret("This is a secret", session.user)).thenReturn(Some(1))

    val route = new RestApp(connection).route
    val validCredentials = OAuth2BearerToken("RandomToken")

    // Act
    Post("/secrets", "This is a secret") ~> addCredentials(validCredentials) ~> route ~> check {
      // Assert
      responseAs[String] shouldEqual "1"
    }
  }

  test("When post /permissions/secrets/1, then create new permission") {

  }
}
