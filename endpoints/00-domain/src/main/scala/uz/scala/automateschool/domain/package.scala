package uz.scala.automateschool

import java.util.UUID

import scala.concurrent.duration.FiniteDuration

import cats.implicits.catsSyntaxEitherId
import derevo.cats.eqv
import derevo.cats.show
import derevo.derive
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.Decoder
import io.circe.Encoder
import io.estatico.newtype.Coercible
import io.estatico.newtype.macros.newtype
import io.estatico.newtype.ops.toCoercibleIdOps
import pureconfig.BasicReaders.finiteDurationConfigReader
import pureconfig.ConfigReader
import pureconfig.error.FailureReason
import squants.Money
import squants.market.USD
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt

import uz.scala.syntax.refined.commonSyntaxAutoRefineV
import uz.scala.automateschool.utils.uuid

package object domain {
  @derive(eqv, show, uuid)
  @newtype case class UserId(value: UUID)
  @derive(eqv, show, uuid)
  @newtype case class GroupId(value: UUID)
  @derive(eqv, show, uuid)
  @newtype case class TeacherId(value: UUID)
  @derive(eqv, show, uuid)
  @newtype case class SubjectId(value: UUID)
  @derive(eqv, show, uuid)
  @newtype case class CustomerId(value: UUID)
  @derive(eqv, show, uuid)
  @newtype case class CountryId(value: UUID)
  @derive(eqv, show, uuid)
  @newtype case class RegionId(value: UUID)
  @derive(eqv, show, uuid)
  @newtype case class CityId(value: UUID)
  @derive(eqv, show, uuid)
  @newtype case class MessageId(value: UUID)

  @newtype case class JwtAccessTokenKey(secret: NonEmptyString)

  object JwtAccessTokenKey {
    implicit val reader: ConfigReader[JwtAccessTokenKey] =
      ConfigReader.fromString[JwtAccessTokenKey](str =>
        JwtAccessTokenKey(str).asRight[FailureReason]
      )
  }

  @newtype case class TokenExpiration(value: FiniteDuration)

  object TokenExpiration {
    implicit val reader: ConfigReader[TokenExpiration] =
      finiteDurationConfigReader.map(duration => TokenExpiration(duration))
  }

  implicit val passwordHashEncoder: Encoder[PasswordHash[SCrypt]] =
    Encoder.encodeString.contramap(_.toString)
  implicit val passwordHashDecoder: Decoder[PasswordHash[SCrypt]] =
    Decoder.decodeString.map(PasswordHash[SCrypt])

  implicit def coercibleDecoder[A: Coercible[B, *], B: Decoder]: Decoder[A] =
    Decoder[B].map(_.coerce[A])

  implicit def coercibleEncoder[A: Coercible[B, *], B: Encoder]: Encoder[A] =
    Encoder[B].contramap(_.asInstanceOf[B])

  implicit val moneyDecoder: Decoder[Money] =
    Decoder[BigDecimal].map(USD.apply)

  implicit val moneyEncoder: Encoder[Money] =
    Encoder[BigDecimal].contramap(_.amount)
}
