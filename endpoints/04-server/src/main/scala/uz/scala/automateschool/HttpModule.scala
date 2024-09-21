package uz.scala.automateschool

import cats.data.NonEmptyList
import cats.effect.Async
import cats.effect.ExitCode
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.implicits.toFunctorOps
import cats.implicits.toSemigroupKOps
import org.http4s.HttpRoutes
import org.http4s.circe.JsonDecoder
import org.http4s.server.Router
import org.typelevel.log4cats.Logger

import uz.scala.http4s.HttpServer
import uz.scala.http4s.utils.Routes
import uz.scala.automateschool.api.routes.AuthRoutes
import uz.scala.automateschool.api.routes._
import uz.scala.automateschool.domain.AuthedUser
import uz.scala.automateschool.http.Environment

object HttpModule {
  private def allRoutes[F[_]: Async: JsonDecoder: Dispatcher: Logger](
      env: Environment[F]
    ): NonEmptyList[HttpRoutes[F]] =
    NonEmptyList
      .of[Routes[F, AuthedUser]](
        new AuthRoutes[F](env.algebras.auth),
        new RootRoutes[F],
        new SubjectsRoutes[F](env.algebras.subjects),
        new TeachersRoutes[F](env.algebras.Teachers),
      )
      .map { r =>
        Router(
          r.path -> (r.public <+> env.middleware(r.`private`))
        )
      }

  def make[F[_]: Async: Dispatcher](
      env: Environment[F]
    )(implicit
      logger: Logger[F]
    ): Resource[F, F[ExitCode]] =
    HttpServer.make[F](env.config, _ => allRoutes[F](env)).map { _ =>
      logger.info(s"HTTP server is started").as(ExitCode.Success)
    }
}
