package uz.scala.automateschool

import cats.effect.Async
import cats.effect.Resource
import skunk.Session

import uz.scala.automateschool.repos._

case class Repositories[F[_]](
    users: UsersRepository[F],
    subjects: SubjectsRepository[F],
    teachers: TeachersRepository[F],
  )
object Repositories {
  def make[F[_]: Async](
      implicit
      session: Resource[F, Session[F]]
    ): Repositories[F] =
    Repositories(
      users = UsersRepository.make[F],
      subjects = SubjectsRepository.make[F],
      teachers = TeachersRepository.make[F],
    )
}
