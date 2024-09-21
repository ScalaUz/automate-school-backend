package uz.scala.automateschool.repos.dto

import eu.timepit.refined.types.string.NonEmptyString
import io.scalaland.chimney.dsl.TransformerOps
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt

import uz.scala.automateschool.Phone
import uz.scala.automateschool.domain.UserId

case class User(
    id: UserId,
    name: NonEmptyString,
    phone: Phone,
    password: PasswordHash[SCrypt],
    createdAt: java.time.ZonedDateTime,
    updatedAt: Option[java.time.ZonedDateTime] = None,
    deletedAt: Option[java.time.ZonedDateTime] = None,
  ) {
  def toDomain: uz.scala.automateschool.domain.AuthedUser.User =
    this.transformInto[uz.scala.automateschool.domain.AuthedUser.User]
}
