package uz.scala.automateschool.algebras

import cats.MonadThrow
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps

import uz.scala.automateschool.domain.TeacherId
import uz.scala.automateschool.domain.teachers.Teacher
import uz.scala.automateschool.domain.teachers.TeacherInput
import uz.scala.automateschool.effects.GenUUID
import uz.scala.automateschool.repos.TeachersRepository
import uz.scala.automateschool.repos.dto
import uz.scala.automateschool.utils.ID
import uz.scala.effects.Calendar

trait TeachersAlgebra[F[_]] {
  def getTeachers: F[List[Teacher]]
  def create(teacherInput: TeacherInput): F[TeacherId]
}

object TeachersAlgebra {
  def make[F[_]: MonadThrow: Calendar: GenUUID](
      teachersRepository: TeachersRepository[F]
    ): TeachersAlgebra[F] =
    new Impl[F](teachersRepository)

  private class Impl[F[_]: MonadThrow: GenUUID: Calendar](
      teachersRepository: TeachersRepository[F]
    ) extends TeachersAlgebra[F] {
    override def getTeachers: F[List[Teacher]] = teachersRepository.getAll.map(_.map(_.toDomain))

    override def create(teacherInput: TeacherInput): F[TeacherId] =
      for {
        id <- ID.make[F, TeacherId]
        now <- Calendar[F].currentZonedDateTime
        teacher = dto.Teacher(
          id = id,
          name = teacherInput.name,
          workload = teacherInput.workload,
          createdAt = now,
        )
        _ <- teachersRepository.create(teacher)
      } yield id
  }
}
