package uz.scala.automateschool.repos.dto

import uz.scala.automateschool.domain.GroupId
import uz.scala.automateschool.domain.SubjectId
import uz.scala.automateschool.domain.TeacherId

case class Lesson(
    groupId: GroupId,
    teacherId: TeacherId,
    subjectId: SubjectId,
    weekday: String,
    moment: Int,
  )
