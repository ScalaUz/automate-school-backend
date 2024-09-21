package uz.scala.automateschool.api.routes

import cats.MonadThrow
import cats.implicits.toFlatMapOps
import org.http4s._
import org.http4s.circe.JsonDecoder

import uz.scala.automateschool.ObjectId
import uz.scala.automateschool.algebras.TeachersAlgebra
import uz.scala.automateschool.domain.AuthedUser
import uz.scala.automateschool.domain.teachers.TeacherInput
import uz.scala.http4s.syntax.all.deriveEntityEncoder
import uz.scala.http4s.syntax.all.http4SyntaxReqOps
import uz.scala.http4s.utils.Routes

final class TeachersRoutes[F[_]: JsonDecoder: MonadThrow](teachersAlgebra: TeachersAlgebra[F])
    extends Routes[F, AuthedUser] {
  override val path: String = "/teachers"
  override val `private`: AuthedRoutes[AuthedUser, F] = AuthedRoutes.of {
    case ar @ POST -> Root as _ =>
      ar.req.decodeR[TeacherInput] { input =>
        teachersAlgebra.create(input).flatMap(id => Created(ObjectId(id.value)))
      }
    case GET -> Root as _ =>
      teachersAlgebra.getTeachers.flatMap(Ok(_))
  }
}
