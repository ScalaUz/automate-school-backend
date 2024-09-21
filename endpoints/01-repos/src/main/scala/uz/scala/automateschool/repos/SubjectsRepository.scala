package uz.scala.automateschool.repos

import cats.data.NonEmptyList
import cats.effect.Async
import cats.effect.Resource
import cats.implicits.toFunctorOps
import skunk._

import uz.scala.automateschool.domain.SubjectId
import uz.scala.automateschool.domain.TeacherId
import uz.scala.automateschool.repos.sql.SubjectsSql
import uz.scala.skunk.syntax.all._

trait SubjectsRepository[F[_]] {
  def getAll: F[List[dto.Subject]]
  def create(group: dto.Subject): F[Unit]
  def insertStudyHours(studyHours: NonEmptyList[dto.StudyHour]): F[Unit]
  def teachersSubjects: F[Map[TeacherId, List[dto.Subject]]]
  def studyHours: F[List[dto.StudyHour]]
  def createTeachersSubject(teacherId: TeacherId, subjectId: SubjectId): F[Unit]
}

object SubjectsRepository {
  def make[F[_]: Async](
      implicit
      session: Resource[F, Session[F]]
    ): SubjectsRepository[F] = new SubjectsRepository[F] {
    override def getAll: F[List[dto.Subject]] =
      SubjectsSql.select.all

    override def create(group: dto.Subject): F[Unit] =
      SubjectsSql.insert.execute(group)

    override def insertStudyHours(studyHours: NonEmptyList[dto.StudyHour]): F[Unit] = {
      val list = studyHours.toList
      SubjectsSql.insertStudyHours(list).execute(list)
    }

    override def studyHours: F[List[dto.StudyHour]] =
      SubjectsSql.selectStudyHours.all

    override def createTeachersSubject(teacherId: TeacherId, subjectId: SubjectId): F[Unit] =
      SubjectsSql.insertTeachersSubjects.execute(teacherId *: subjectId *: EmptyTuple)

    override def teachersSubjects: F[Map[TeacherId, List[dto.Subject]]] =
      SubjectsSql.teachersSubjects.all.map(_.groupMap(_._1)(_._2))
  }
}
