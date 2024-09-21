package uz.scala.automateschool

import cats.effect.Async
import cats.effect.std.Random
import org.typelevel.log4cats.Logger
import tsec.passwordhashers.PasswordHasher
import tsec.passwordhashers.jca.SCrypt

import uz.scala.automateschool.algebras._
import uz.scala.automateschool.auth.AuthConfig
import uz.scala.automateschool.auth.impl.Auth
import uz.scala.automateschool.domain.AuthedUser
import uz.scala.redis.RedisClient

case class Algebras[F[_]](
    auth: Auth[F, AuthedUser],
    users: UsersAlgebra[F],
    subjects: SubjectsAlgebra[F],
    Teachers: TeachersAlgebra[F],
  )

object Algebras {
  def make[F[_]: Async: Logger: Random](
      config: AuthConfig,
      repositories: Repositories[F],
      redis: RedisClient[F],
    )(implicit
      P: PasswordHasher[F, SCrypt]
    ): Algebras[F] = {
    val users = UsersAlgebra.make[F](repositories.users)
    Algebras[F](
      auth = Auth.make[F](config, users, redis),
      users = users,
      subjects = SubjectsAlgebra.make[F](repositories.subjects),
      Teachers = TeachersAlgebra.make[F](repositories.teachers),
    )
  }
}
