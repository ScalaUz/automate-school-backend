package uz.scala.automateschool.domain.teachers

import eu.timepit.refined.types.numeric.NonNegInt
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.JsonCodec
import io.circe.refined._

import uz.scala.automateschool.domain.TeacherId
import uz.scala.syntax.circe._

@JsonCodec
case class Teacher(
    id: TeacherId,
    name: NonEmptyString,
    workload: NonNegInt,
    createdAt: java.time.ZonedDateTime,
    updatedAt: Option[java.time.ZonedDateTime],
  )
