package uz.scala.automateschool.domain.groups

import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.JsonCodec
import io.circe.refined._

import uz.scala.automateschool.domain.GroupId
import uz.scala.syntax.circe._

@JsonCodec
case class Group(
    id: GroupId,
    name: NonEmptyString,
    level: PosInt,
    students: PosInt,
    createdAt: java.time.ZonedDateTime,
    updatedAt: Option[java.time.ZonedDateTime] = None,
  )
