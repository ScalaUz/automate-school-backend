package uz.scala.automateschool.auth.impl

import scala.concurrent.duration.DurationInt

import cats.data.EitherT
import cats.data.OptionT
import cats.effect.Sync
import cats.effect.std.Random
import cats.implicits._
import dev.profunktor.auth.AuthHeaders
import dev.profunktor.auth.jwt.JwtAuth
import dev.profunktor.auth.jwt.JwtSymmetricAuth
import dev.profunktor.auth.jwt.JwtToken
import org.http4s.Request
import org.typelevel.log4cats.Logger
import pdi.jwt.JwtAlgorithm
import tsec.passwordhashers.jca.SCrypt

import uz.scala.automateschool.Language
import uz.scala.automateschool.ResponseMessages.INVALID_TOKEN
import uz.scala.automateschool.ResponseMessages.PASSWORD_DOES_NOT_MATCH
import uz.scala.automateschool.ResponseMessages.USER_NOT_FOUND
import uz.scala.automateschool.algebras.UsersAlgebra
import uz.scala.automateschool.auth.AuthConfig
import uz.scala.automateschool.auth.utils.AuthMiddleware
import uz.scala.automateschool.auth.utils.JwtExpire
import uz.scala.automateschool.auth.utils.Tokens
import uz.scala.automateschool.domain.AuthedUser
import uz.scala.automateschool.domain.auth._
import uz.scala.automateschool.exception.AError.AuthError
import uz.scala.automateschool.exception.AError.AuthError.NoSuchUser
import uz.scala.automateschool.exception.AError.AuthError.PasswordDoesNotMatch
import uz.scala.redis.RedisClient
import uz.scala.syntax.all.circeSyntaxDecoderOps
import uz.scala.syntax.refined.commonSyntaxAutoUnwrapV

trait Auth[F[_], A] {
  def loginByPassword(
      credentials: UserCredentials
    )(implicit
      language: Language
    ): F[AuthTokens]
  def destroySession(request: Request[F], login: String): F[Unit]
  def refresh(request: Request[F])(implicit language: Language): F[AuthTokens]
}

object Auth {
  def make[F[_]: Sync: Random](
      config: AuthConfig,
      users: UsersAlgebra[F],
      redis: RedisClient[F],
    )(implicit
      logger: Logger[F]
    ): Auth[F, AuthedUser] =
    new Auth[F, AuthedUser] {
      val tokens: Tokens[F] =
        Tokens.make[F](JwtExpire[F], config)
      val jwtAuth: JwtSymmetricAuth = JwtAuth.hmac(config.tokenKey.secret, JwtAlgorithm.HS256)

      override def loginByPassword(
          credentials: UserCredentials
        )(implicit
          language: Language
        ): F[AuthTokens] =
        users.findByPhone(credentials.phone).flatMap {
          case None =>
            NoSuchUser(USER_NOT_FOUND(language)).raiseError[F, AuthTokens]
          case Some(user) if !SCrypt.checkpwUnsafe(credentials.password.value, user.password) =>
            PasswordDoesNotMatch(PASSWORD_DOES_NOT_MATCH(language)).raiseError[F, AuthTokens]
          case Some(user) => processCreateToken(user.toDomain)
        }

      override def refresh(request: Request[F])(implicit language: Language): F[AuthTokens] =
        for {
          refreshToken <- EitherT(
            AuthMiddleware
              .getAndValidateJwtToken[F](
                jwtAuth,
                token =>
                  for {
                    _ <- OptionT(redis.get(AuthMiddleware.REFRESH_TOKEN_PREFIX + token))
                      .semiflatMap(_.decodeAsF[F, AuthedUser])
                      .semiflatMap(user => redis.del(user.login))
                      .value
                    _ <- redis.del(AuthMiddleware.REFRESH_TOKEN_PREFIX + token.value)
                  } yield {},
              )
              .apply(request)
          ).leftMap(AuthError.InvalidToken.apply).rethrowT

          tokens <- OptionT(redis.get(refreshToken.value))
            .semiflatMap(_.decodeAsF[F, AuthTokens])
            .getOrElseF(refreshTokens(refreshToken))

        } yield tokens

      override def destroySession(request: Request[F], login: String): F[Unit] =
        AuthHeaders
          .getBearerToken(request)
          .traverse_(token => redis.del(AuthMiddleware.ACCESS_TOKEN_PREFIX + token.value, login))

      private def processCreateToken(user: AuthedUser)(implicit language: Language): F[AuthTokens] =
        OptionT(redis.get(user.login))
          .cataF(
            createNewToken(user),
            json =>
              for {
                tokens <- json.decodeAsF[F, AuthTokens]
                validTokens <- EitherT(
                  AuthMiddleware
                    .validateJwtToken[F](
                      JwtToken(tokens.accessToken),
                      jwtAuth,
                      _ => redis.del(tokens.accessToken, tokens.refreshToken, user.login),
                    )
                ).foldF(
                  error =>
                    logger.info(s"Tokens recreated reason of that: $error") *>
                      createNewToken(user),
                  _ => tokens.pure[F],
                )
              } yield validTokens,
          )

      private def refreshTokens(
          refreshToken: JwtToken
        )(implicit
          language: Language
        ): F[AuthTokens] =
        (for {
          user <- EitherT
            .fromOptionF(
              redis.get(AuthMiddleware.REFRESH_TOKEN_PREFIX + refreshToken.value),
              AuthError.InvalidToken(INVALID_TOKEN(language)),
            )
            .semiflatMap(_.decodeAsF[F, AuthedUser])
          _ <- EitherT.right[AuthError](clearOldTokens(user.login))
          tokens <- EitherT.right[AuthError](createNewToken(user))
          _ <- EitherT.right[AuthError](redis.put(refreshToken.value, tokens, 1.minute))
        } yield tokens).rethrowT

      private def createNewToken(user: AuthedUser): F[AuthTokens] =
        for {
          tokens <- tokens.createToken[AuthedUser](user)
          accessToken = AuthMiddleware.ACCESS_TOKEN_PREFIX + tokens.accessToken
          refreshToken = AuthMiddleware.REFRESH_TOKEN_PREFIX + tokens.refreshToken
          _ <- redis.put(accessToken, user, config.accessTokenExpiration.value)
          _ <- redis.put(refreshToken, user, config.refreshTokenExpiration.value)
          _ <- redis.put(user.login, tokens, config.refreshTokenExpiration.value)
        } yield tokens

      private def clearOldTokens(login: String): F[Unit] =
        OptionT(redis.get(login))
          .semiflatMap(_.decodeAsF[F, AuthTokens])
          .semiflatMap(tokens =>
            redis.del(
              s"${AuthMiddleware.REFRESH_TOKEN_PREFIX}${tokens.refreshToken}",
              s"${AuthMiddleware.ACCESS_TOKEN_PREFIX}${tokens.accessToken}",
            )
          )
          .value
          .void
    }
}
