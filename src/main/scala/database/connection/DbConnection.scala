package database.connection

import model.{Secret, Session, Token, User}

trait DbConnection {
  def users(): Vector[User]
  def user(username: String, password: String): Option[User]
  def user(token: Token): Option[User]
  def loggedInUser(): Vector[User]
  def mate(currentUser: User): Vector[User]
  def isMate(currentUser: User, otherUser: User): Boolean
  def loggedInMate(currentUser: User): Vector[User]
  def addSession(session: Session): Option[Int]
  def session(token: Token): Option[Session]
  def addSecret(content: String, user: User): Option[Int]
  def secret(id: Int): Option[Secret]
  def secrets(session: Session): Vector[Int]
  def addPermission(secretid: Int, user: User): Option[Int]
  def hasPermission(session: Session, secretid: Int): Boolean
}
