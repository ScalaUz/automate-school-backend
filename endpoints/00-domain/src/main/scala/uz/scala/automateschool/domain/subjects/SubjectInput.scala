package uz.scala.automateschool.domain.subjects

import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.JsonCodec
import io.circe.refined._

import uz.scala.automateschool.domain.enums.SubjectCategory

@JsonCodec
case class SubjectInput(
    name: NonEmptyString,
    category: SubjectCategory,
  )
