package uz.scala.automateschool.repos.dto

import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import io.scalaland.chimney.dsl.TransformerOps

import uz.scala.automateschool.domain
import uz.scala.automateschool.domain.GroupId

case class Group(
    id: GroupId,
    name: NonEmptyString,
    level: PosInt,
    students: PosInt,
    createdAt: java.time.ZonedDateTime,
    updatedAt: Option[java.time.ZonedDateTime] = None,
    deletedAt: Option[java.time.ZonedDateTime] = None,
  ) {
  def toDomain: domain.groups.Group =
    this.transformInto[domain.groups.Group]
}
