package uz.scala.automateschool.http

import cats.effect.Async
import org.http4s.server

import uz.scala.http4s.HttpServerConfig
import uz.scala.automateschool.Algebras
import uz.scala.automateschool.domain.AuthedUser

case class Environment[F[_]: Async](
    config: HttpServerConfig,
    middleware: server.AuthMiddleware[F, AuthedUser],
    algebras: Algebras[F],
  )
