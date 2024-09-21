package uz.scala.automateschool.api.routes

import cats.MonadThrow
import cats.implicits.toFlatMapOps
import org.http4s._
import org.http4s.circe.JsonDecoder

import uz.scala.automateschool.algebras.TimetableAlgebra
import uz.scala.automateschool.domain.AuthedUser
import uz.scala.http4s.syntax.all.deriveEntityEncoder
import uz.scala.http4s.utils.Routes

final class TimetableRoutes[F[_]: JsonDecoder: MonadThrow](timetable: TimetableAlgebra[F])
    extends Routes[F, AuthedUser] {
  override val path: String = "/timetable"
  override val `private`: AuthedRoutes[AuthedUser, F] = AuthedRoutes.of {
    case GET -> Root as _ =>
      timetable.getTimetable.flatMap(Ok(_))

    case POST -> Root as _ =>
      timetable.generateTimetable.flatMap(Ok(_))
  }
}
