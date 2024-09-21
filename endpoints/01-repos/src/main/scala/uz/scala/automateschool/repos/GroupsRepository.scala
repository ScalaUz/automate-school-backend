package uz.scala.automateschool.repos

import cats.data.NonEmptyList
import cats.data.OptionT
import cats.effect.Async
import cats.effect.Resource
import skunk._

import uz.scala.automateschool.domain.GroupId
import uz.scala.automateschool.exception.AError
import uz.scala.automateschool.repos.sql.GroupsSql
import uz.scala.skunk.syntax.all._

trait GroupsRepository[F[_]] {
  def getAll: F[List[dto.Group]]
  def create(groups: NonEmptyList[dto.Group]): F[Unit]
  def update(id: GroupId)(update: dto.Group => dto.Group): F[Unit]
  def delete(groupId: GroupId): F[Unit]
  def findById(id: GroupId): F[Option[dto.Group]]
}

object GroupsRepository {
  def make[F[_]: Async](
      implicit
      session: Resource[F, Session[F]]
    ): GroupsRepository[F] = new GroupsRepository[F] {
    override def getAll: F[List[dto.Group]] =
      GroupsSql.select.all

    override def create(groups: NonEmptyList[dto.Group]): F[Unit] = {
      val list = groups.toList
      GroupsSql.insert(list).execute(list)
    }

    override def update(id: GroupId)(update: dto.Group => dto.Group): F[Unit] =
      OptionT(findById(id))
        .semiflatMap(g => GroupsSql.update.execute(update(g)))
        .getOrRaise(AError.Internal("Group not found"))

    override def delete(groupId: GroupId): F[Unit] =
      GroupsSql.delete.execute(groupId)

    override def findById(id: GroupId): F[Option[dto.Group]] =
      GroupsSql.selectById.queryOption(id)
  }
}
