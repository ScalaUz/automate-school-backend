package automateschool.endpoint.setup

import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Console
import cats.effect.std.Dispatcher
import cats.effect.std.Random
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.effect.Log.NoOp.instance
import eu.timepit.refined.pureconfig._
import izumi.reflect.TagK
import org.http4s.server
import org.typelevel.log4cats.Logger
import pureconfig.generic.auto.exportReader

import uz.scala.database.Migrations
import uz.scala.redis.RedisClient
import uz.scala.skunk.SkunkSession
import uz.scala.automateschool.Algebras
import uz.scala.automateschool.Repositories
import uz.scala.automateschool.auth.impl.LiveMiddleware
import uz.scala.automateschool.domain.AuthedUser
import uz.scala.automateschool.http.{ Environment => ServerEnvironment }
import uz.scala.automateschool.utils.ConfigLoader

case class Environment[F[_]: Async: Logger: Dispatcher: Random](
    config: Config,
    repositories: Repositories[F],
    middleware: server.AuthMiddleware[F, AuthedUser],
    redis: RedisClient[F],
  ) {
  private val algebras: Algebras[F] = Algebras.make[F](config.auth, repositories, redis)
  lazy val toServer: ServerEnvironment[F] =
    ServerEnvironment(
      config = config.http,
      middleware = middleware,
      algebras = algebras,
    )
}
object Environment {
  def make[F[_]: TagK: Async: Console: Logger: Dispatcher]: Resource[F, Environment[F]] =
    for {
      config <- Resource.eval(ConfigLoader.load[F, Config])
      _ <- Resource.eval(Migrations.run[F](config.migrations))
      repositories <- SkunkSession.make[F](config.database).map { implicit session =>
        Repositories.make[F]
      }
      redis <- Redis[F].utf8(config.redis.uri.toString).map(RedisClient[F](_, config.redis.prefix))
      implicit0(random: Random[F]) <- Resource.eval(Random.scalaUtilRandom[F])

      middleware = LiveMiddleware.make[F](config.auth, redis)
    } yield Environment[F](config, repositories, middleware, redis)
}
