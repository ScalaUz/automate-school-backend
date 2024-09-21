package uz.scala.automateschool.domain.timetable

import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.JsonCodec
import io.circe.refined._

import uz.scala.automateschool.domain.GroupId
import uz.scala.automateschool.domain.SubjectId
import uz.scala.automateschool.domain.TeacherId
import uz.scala.automateschool.domain.timetable.Timetable.Subject
import uz.scala.automateschool.domain.timetable.Timetable.Teacher
import uz.scala.syntax.circe._

@JsonCodec
case class Timetable(
    moment: Int,
    subject: Subject,
    teacher: Teacher,
    groupId: GroupId,
  )

object Timetable {
  @JsonCodec
  case class Subject(
      id: SubjectId,
      name: NonEmptyString,
    )
  @JsonCodec
  case class Teacher(
      id: TeacherId,
      name: NonEmptyString,
    )
}
