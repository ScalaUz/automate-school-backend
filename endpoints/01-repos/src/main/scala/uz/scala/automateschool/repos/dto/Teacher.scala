package uz.scala.automateschool.repos.dto

import eu.timepit.refined.types.numeric.NonNegInt
import eu.timepit.refined.types.string.NonEmptyString
import io.scalaland.chimney.dsl.TransformerOps

import uz.scala.automateschool.domain
import uz.scala.automateschool.domain.TeacherId

case class Teacher(
    id: TeacherId,
    name: NonEmptyString,
    workload: NonNegInt,
    createdAt: java.time.ZonedDateTime,
    updatedAt: Option[java.time.ZonedDateTime] = None,
    deletedAt: Option[java.time.ZonedDateTime] = None,
  ) {
  def toDomain: domain.teachers.Teacher =
    this.transformInto[domain.teachers.Teacher]
}
