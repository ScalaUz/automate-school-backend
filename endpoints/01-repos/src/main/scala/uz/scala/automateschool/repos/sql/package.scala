package uz.scala.automateschool.repos

import java.time.ZonedDateTime

import eu.timepit.refined.types.numeric.NonNegInt
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import skunk.Codec
import skunk.codec.all._
import skunk.data.Type
import squants.Money
import squants.market.USD
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt

import uz.scala.syntax.refined.commonSyntaxAutoRefineV
import uz.scala.automateschool.EmailAddress
import uz.scala.automateschool.domain.enums.SubjectCategory
import uz.scala.automateschool.effects.IsUUID

package object sql {
  def identification[A: IsUUID]: Codec[A] = uuid.imap[A](IsUUID[A].uuid.get)(IsUUID[A].uuid.apply)

  val nes: Codec[NonEmptyString] = varchar.imap[NonEmptyString](identity(_))(_.value)
  val nni: Codec[NonNegInt] = int4.imap[NonNegInt](identity(_))(_.value)
  val posInt: Codec[PosInt] = int4.imap[PosInt](identity(_))(_.value)
  val email: Codec[EmailAddress] = varchar.imap[EmailAddress](identity(_))(_.value)
  val zdt: Codec[ZonedDateTime] = timestamptz.imap(_.toZonedDateTime)(_.toOffsetDateTime)
  val price: Codec[Money] = numeric.imap[Money](money => USD(money))(_.amount)
  val hash: Codec[PasswordHash[SCrypt]] =
    varchar.imap[PasswordHash[SCrypt]](PasswordHash[SCrypt])(identity)

  val category: Codec[SubjectCategory] =
    `enum`[SubjectCategory](_.value, SubjectCategory.withValueOpt, Type("subject_category"))
}
