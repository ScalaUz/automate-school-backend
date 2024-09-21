package uz.scala.automateschool.domain.groups

import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.JsonCodec
import io.circe.refined._

import uz.scala.automateschool.domain.groups.GroupsInput.GroupInput

@JsonCodec
case class GroupsInput(
    level: PosInt,
    groups: List[GroupInput],
  )

object GroupsInput {
  @JsonCodec
  case class GroupInput(
      name: NonEmptyString,
      students: PosInt,
    )
}
