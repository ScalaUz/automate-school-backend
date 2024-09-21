package uz.scala.automateschool.api.routes

import cats.effect.Sync
import cats.implicits.catsSyntaxOptionId
import org.http4s._
import org.http4s.circe.JsonDecoder

import uz.scala.http4s.utils.Routes
import uz.scala.automateschool.domain.AuthedUser

final class RootRoutes[F[_]: JsonDecoder: Sync] extends Routes[F, AuthedUser] {
  override val path: String = "/"
  override val public: HttpRoutes[F] = HttpRoutes.of {
    case req @ GET -> Root / "endpoints" =>
      StaticFile.fromResource("/swagger/swagger.html", req.some).getOrElseF(NotFound())
    case req @ GET -> Root / "endpoints.yml" =>
      StaticFile.fromResource("/swagger/endpoints.yml", req.some).getOrElseF(NotFound())
  }
}
