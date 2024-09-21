package uz.scala.automateschool.algebras

import cats.Monad
import cats.effect.std.Random
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps
import tsec.passwordhashers.PasswordHasher
import tsec.passwordhashers.jca.SCrypt

import uz.scala.automateschool.Phone
import uz.scala.automateschool.domain.UserId
import uz.scala.automateschool.domain.users.UserInput
import uz.scala.automateschool.domain.users.UserUpdateInput
import uz.scala.automateschool.effects.Calendar
import uz.scala.automateschool.effects.GenUUID
import uz.scala.automateschool.randomStr
import uz.scala.automateschool.repos.UsersRepository
import uz.scala.automateschool.repos.dto
import uz.scala.automateschool.utils.ID

trait UsersAlgebra[F[_]] {
  def create(userInfo: UserInput): F[UserId]
  def findById(id: UserId): F[Option[dto.User]]
  def findByPhone(phone: Phone): F[Option[dto.User]]
  def update(id: UserId, userUpdate: UserUpdateInput): F[Unit]
  def delete(id: UserId): F[Unit]
}

object UsersAlgebra {
  def make[F[_]: Monad: GenUUID: Calendar: Random](
      usersRepository: UsersRepository[F]
    )(implicit
      P: PasswordHasher[F, SCrypt]
    ): UsersAlgebra[F] =
    new UsersAlgebra[F] {
      override def create(userInfo: UserInput): F[UserId] =
        for {
          id <- ID.make[F, UserId]
          now <- Calendar[F].currentZonedDateTime
          passwordStr <- randomStr[F](6)
          hash <- SCrypt.hashpw[F](passwordStr)
          user = dto.User(
            id = id,
            createdAt = now,
            name = userInfo.name,
            phone = userInfo.phone,
            password = hash,
          )
          _ <- usersRepository.create(user)
        } yield id

      override def findById(id: UserId): F[Option[dto.User]] =
        usersRepository.findById(id)

      override def findByPhone(phone: Phone): F[Option[dto.User]] =
        usersRepository.findByPhone(phone)

      override def update(id: UserId, userUpdate: UserUpdateInput): F[Unit] =
        usersRepository.update(id)(_.copy(name = userUpdate.name))

      override def delete(id: UserId): F[Unit] =
        usersRepository.delete(id)
    }
}
