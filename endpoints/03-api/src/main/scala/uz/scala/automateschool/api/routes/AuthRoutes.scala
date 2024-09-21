package uz.scala.automateschool.api.routes

import cats.MonadThrow
import cats.implicits.catsSyntaxApplyOps
import cats.implicits.toFlatMapOps
import org.http4s.AuthedRoutes
import org.http4s.HttpRoutes
import org.http4s.circe.JsonDecoder
import org.typelevel.log4cats.Logger

import uz.scala.automateschool.Language
import uz.scala.automateschool.auth.impl.Auth
import uz.scala.automateschool.domain.AuthedUser
import uz.scala.automateschool.domain.auth.UserCredentials
import uz.scala.http4s.syntax.all.deriveEntityEncoder
import uz.scala.http4s.syntax.all.http4SyntaxReqOps
import uz.scala.http4s.utils.Routes

final case class AuthRoutes[F[_]: Logger: JsonDecoder: MonadThrow](
    auth: Auth[F, AuthedUser]
  ) extends Routes[F, AuthedUser] {
  override val path = "/auth"

  override val public: HttpRoutes[F] =
    HttpRoutes.of[F] {

      case req @ POST -> Root / "user" / "login" =>
        implicit val language: Language = req.lang
        req.decodeR[UserCredentials] { credentials =>
          auth
            .loginByPassword(credentials)
            .flatMap(Ok(_))
        }

      case req @ GET -> Root / "refresh" =>
        implicit val language: Language = req.lang
        auth.refresh(req).flatMap(Ok(_))
    }

  override val `private`: AuthedRoutes[AuthedUser, F] = AuthedRoutes.of {
    case GET -> Root / "me" as user =>
      Ok(user)

    case ar @ GET -> Root / "logout" as user =>
      auth.destroySession(ar.req, user.login) *> NoContent()
  }
}
