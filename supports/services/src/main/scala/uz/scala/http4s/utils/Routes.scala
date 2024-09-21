package uz.scala.http4s.utils

import cats.Applicative
import org.http4s.AuthedRoutes
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

abstract class Routes[F[_]: Applicative, U] extends Http4sDsl[F] {
  val path: String
  val public: HttpRoutes[F] = HttpRoutes.empty[F]
  val `private`: AuthedRoutes[U, F] = AuthedRoutes.empty[U, F]
}
