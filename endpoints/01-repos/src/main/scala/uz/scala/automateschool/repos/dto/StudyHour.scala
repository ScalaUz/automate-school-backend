package uz.scala.automateschool.repos.dto

import eu.timepit.refined.types.numeric.PosInt

import uz.scala.automateschool.domain.SubjectId

case class StudyHour(
    subjectId: SubjectId,
    level: PosInt,
    hour: PosInt,
  )
