package uz.scala.automateschool.domain.teachers

import eu.timepit.refined.types.numeric.NonNegInt
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.JsonCodec
import io.circe.refined._

@JsonCodec
case class TeacherInput(
    name: NonEmptyString,
    workload: NonNegInt,
  )
