package uz.scala.automateschool.repos.sql

import skunk._
import skunk.implicits._

import uz.scala.automateschool.domain.SubjectId
import uz.scala.automateschool.domain.TeacherId
import uz.scala.automateschool.repos.dto.StudyHour
import uz.scala.automateschool.repos.dto.Subject

private[repos] object SubjectsSql extends Sql[SubjectId] {
  private val codec = (id *: nes *: category).to[Subject]
  private val studyHoursCodec = (id *: posInt *: posInt).to[StudyHour]

  val insert: Command[Subject] =
    sql"""INSERT INTO subjects VALUES ($codec)""".command

  val select: Query[Void, Subject] =
    sql"""SELECT * FROM subjects""".query(codec)

  def insertStudyHours(studyHours: List[StudyHour]): Command[studyHours.type] =
    sql"""INSERT INTO study_hours VALUES ${studyHoursCodec.values.list(studyHours)}""".command

  val selectStudyHours: Query[Void, StudyHour] =
    sql"""SELECT * FROM study_hours""".query(studyHoursCodec)

  val teachersSubjects: Query[Void, TeacherId *: Subject *: EmptyTuple] =
    sql"""SELECT ts.id, s.* FROM subjects s
         INNER JOIN teachers_subjects ts ON s.id = ts.subject_id
       """.query(TeachersSql.id *: codec)

  val insertTeachersSubjects: Command[TeacherId *: SubjectId *: EmptyTuple] =
    sql"""INSERT INTO teachers_subjects VALUES (${TeachersSql.id}, $id)""".command
}
