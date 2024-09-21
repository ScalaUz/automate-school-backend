package uz.scala.automateschool.repos.dto

import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.JsonCodec
import io.circe.refined._

import uz.scala.automateschool.domain.SubjectId
import uz.scala.automateschool.domain.enums.SubjectCategory
import uz.scala.syntax.circe._

@JsonCodec
case class Subject(
    id: SubjectId,
    name: NonEmptyString,
    category: SubjectCategory,
  )
