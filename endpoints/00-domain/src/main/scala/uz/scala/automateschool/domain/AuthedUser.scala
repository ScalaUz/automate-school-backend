package uz.scala.automateschool.domain

import java.util.UUID

import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.JsonCodec
import io.circe.refined._

import uz.scala.automateschool.Phone
import uz.scala.automateschool.domain.enums.Role

@JsonCodec
sealed trait AuthedUser {
  val _id: UUID
  val name: NonEmptyString
  val role: Role
  val login: String
}

object AuthedUser {
  @JsonCodec
  case class User(
      id: UserId,
      name: NonEmptyString,
      phone: Phone,
    ) extends AuthedUser {
    override val _id: UUID = id.value
    override val login: String = phone.value
    override val role: Role = Role.Admin
  }
}
