package uz.scala.automateschool.algebras

import cats.MonadThrow
import cats.implicits._

import uz.scala.automateschool.domain.SubjectId
import uz.scala.automateschool.domain.subjects.SubjectInput
import uz.scala.automateschool.effects.GenUUID
import uz.scala.automateschool.repos.SubjectsRepository
import uz.scala.automateschool.repos.dto
import uz.scala.automateschool.utils.ID
import uz.scala.effects.Calendar

trait SubjectsAlgebra[F[_]] {
  def create(subject: SubjectInput): F[SubjectId]
  def getSubjects: F[List[dto.Subject]]
}

object SubjectsAlgebra {
  def make[F[_]: MonadThrow: Calendar: GenUUID](
      subjectsRepository: SubjectsRepository[F]
    ): SubjectsAlgebra[F] =
    new Impl[F](subjectsRepository)

  private class Impl[F[_]: MonadThrow: GenUUID: Calendar](
      subjectsRepository: SubjectsRepository[F]
    ) extends SubjectsAlgebra[F] {
    override def create(subjectInput: SubjectInput): F[SubjectId] =
      for {
        id <- ID.make[F, SubjectId]
        subject = dto.Subject(
          id = id,
          name = subjectInput.name,
          category = subjectInput.category,
        )
        _ <- subjectsRepository.create(subject)
      } yield id

    override def getSubjects: F[List[dto.Subject]] =
      subjectsRepository.getAll
  }
}
