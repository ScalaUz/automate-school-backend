package uz.scala.automateschool.repos

import cats.data.OptionT
import cats.effect.Async
import cats.effect.Resource
import skunk._

import uz.scala.automateschool.Phone
import uz.scala.automateschool.domain.UserId
import uz.scala.automateschool.exception.AError
import uz.scala.automateschool.repos.dto.User
import uz.scala.automateschool.repos.sql.UsersSql
import uz.scala.skunk.syntax.all._

trait UsersRepository[F[_]] {
  def findByPhone(phone: Phone): F[Option[User]]
  def create(userAndHash: User): F[Unit]
  def findById(id: UserId): F[Option[User]]
  def update(id: UserId)(update: User => User): F[Unit]
  def delete(id: UserId): F[Unit]
}

object UsersRepository {
  def make[F[_]: Async](
      implicit
      session: Resource[F, Session[F]]
    ): UsersRepository[F] = new UsersRepository[F] {
    override def findByPhone(phone: Phone): F[Option[User]] =
      UsersSql.findByPhone.queryOption(phone)

    override def create(user: User): F[Unit] =
      UsersSql.insert.execute(user)

    override def findById(id: UserId): F[Option[User]] =
      UsersSql.findById.queryOption(id)

    override def update(id: UserId)(update: User => User): F[Unit] =
      OptionT(findById(id))
        .semiflatMap(u => UsersSql.update.execute(update(u)))
        .getOrRaise(AError.Internal("User not found"))

    override def delete(id: UserId): F[Unit] =
      UsersSql.delete.execute(id)
  }
}
