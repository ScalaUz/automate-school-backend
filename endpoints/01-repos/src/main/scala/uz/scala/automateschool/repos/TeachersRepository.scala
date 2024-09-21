package uz.scala.automateschool.repos

import cats.data.OptionT
import cats.effect.Async
import cats.effect.Resource
import skunk._

import uz.scala.automateschool.domain.TeacherId
import uz.scala.automateschool.exception.AError
import uz.scala.automateschool.repos.sql.TeachersSql
import uz.scala.skunk.syntax.all._

trait TeachersRepository[F[_]] {
  def getAll: F[List[dto.Teacher]]
  def create(group: dto.Teacher): F[Unit]
  def update(id: TeacherId)(update: dto.Teacher => dto.Teacher): F[Unit]
  def delete(groupId: TeacherId): F[Unit]
  def findById(id: TeacherId): F[Option[dto.Teacher]]
}

object TeachersRepository {
  def make[F[_]: Async](
      implicit
      session: Resource[F, Session[F]]
    ): TeachersRepository[F] = new TeachersRepository[F] {
    override def getAll: F[List[dto.Teacher]] =
      TeachersSql.select.all

    override def create(group: dto.Teacher): F[Unit] =
      TeachersSql.insert.execute(group)

    override def update(id: TeacherId)(update: dto.Teacher => dto.Teacher): F[Unit] =
      OptionT(findById(id))
        .semiflatMap(g => TeachersSql.update.execute(update(g)))
        .getOrRaise(AError.Internal("Teacher not found"))

    override def delete(groupId: TeacherId): F[Unit] =
      TeachersSql.delete.execute(groupId)

    override def findById(id: TeacherId): F[Option[dto.Teacher]] =
      TeachersSql.selectById.queryOption(id)
  }
}
