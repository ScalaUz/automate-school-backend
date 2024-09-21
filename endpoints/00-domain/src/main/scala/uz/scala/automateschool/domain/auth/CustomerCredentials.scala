package uz.scala.automateschool.domain.auth

import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.JsonCodec
import io.circe.refined._

import uz.scala.automateschool.Phone

@JsonCodec
case class CustomerCredentials(phone: Phone, password: NonEmptyString)
