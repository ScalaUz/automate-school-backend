package uz.scala.automateschool.auth.utils

import cats.MonadThrow
import cats.data.EitherT
import cats.data.Kleisli
import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.all._
import dev.profunktor.auth.jwt._
import org.http4s.Credentials.Token
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s.server
import pdi.jwt._

import uz.scala.http4s.syntax.all.http4SyntaxReqOps
import uz.scala.automateschool.Language
import uz.scala.automateschool.ResponseMessages.BEARER_TOKEN_NOT_FOUND
import uz.scala.automateschool.ResponseMessages.INVALID_TOKEN

object AuthMiddleware {
  val OTP_PREFIX = "OTP:"
  val OTP_ATTEMPT_PREFIX = "OTP_ATTEMPT:"
  val ACCESS_TOKEN_PREFIX = "ACCESS_"
  val REFRESH_TOKEN_PREFIX = "REFRESH_"
  def getBearerToken[F[_]: MonadThrow]: Kleisli[F, Request[F], Option[JwtToken]] =
    Kleisli { request =>
      MonadThrow[F].pure(
        request
          .headers
          .get[Authorization]
          .collect {
            case Authorization(Token(AuthScheme.Bearer, token)) => JwtToken(token)
          }
          .orElse {
            request.params.get("X-Access-Token").map(JwtToken.apply)
          }
      )
    }

  def validateJwtToken[F[_]: MonadThrow](
      token: JwtToken,
      jwtAuth: JwtSymmetricAuth,
      removeToken: JwtToken => F[Unit],
    )(implicit
      language: Language
    ): F[Either[String, JwtToken]] =
    Jwt
      .decode(
        token.value,
        jwtAuth.secretKey.value,
        jwtAuth.jwtAlgorithms,
      )
      .liftTo
      .map(_ => token.asRight[String])
      .handleErrorWith { _ =>
        removeToken(token).as {
          INVALID_TOKEN(language).asLeft[JwtToken]
        }
      }
  def getAndValidateJwtToken[F[_]: MonadThrow](
      jwtAuth: JwtSymmetricAuth,
      removeToken: JwtToken => F[Unit],
    )(implicit
      language: Language
    ): Kleisli[F, Request[F], Either[String, JwtToken]] =
    Kleisli { request =>
      EitherT
        .fromOptionF(getBearerToken[F].apply(request), BEARER_TOKEN_NOT_FOUND(language))
        .flatMapF { token =>
          validateJwtToken(token, jwtAuth, removeToken)
        }
        .value
    }

  def apply[F[_]: Sync, A](
      jwtAuth: JwtSymmetricAuth,
      authenticate: String => F[Option[A]],
      removeToken: JwtToken => F[Unit],
    ): server.AuthMiddleware[F, A] = { routes: AuthedRoutes[A, F] =>
    val dsl = new Http4sDsl[F] {}; import dsl._

    val onFailure: AuthedRoutes[String, F] =
      Kleisli(req => OptionT.liftF(Forbidden(req.context)))

    def getUser(
        token: JwtToken
      )(implicit
        language: Language
      ): EitherT[F, String, A] =
      EitherT.fromOptionF(authenticate(ACCESS_TOKEN_PREFIX + token.value), INVALID_TOKEN(language))

    Kleisli { (req: Request[F]) =>
      implicit val language: Language = req.lang
      OptionT {
        EitherT(getAndValidateJwtToken[F](jwtAuth, removeToken).apply(req))
          .flatMap(getUser)
          .foldF(
            err => onFailure(AuthedRequest(err, req)).value,
            user => routes(AuthedRequest(user, req)).value,
          )
      }
    }
  }
}
