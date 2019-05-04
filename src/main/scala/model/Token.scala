package model

import java.time.{OffsetDateTime, ZoneOffset}
import java.util.UUID.randomUUID

object Token {
  def apply(): Token = {
    val token = randomUUID.toString
    val issuedDate = OffsetDateTime.now(ZoneOffset.UTC)
    val expiredDate = issuedDate.plusDays(1)

    Token(token, Some(issuedDate), Some(expiredDate))
  }
}

case class Token(token: String, issuedDate: Option[OffsetDateTime] = None, expiredDate: Option[OffsetDateTime] = None)
