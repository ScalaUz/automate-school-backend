package uz.scala.automateschool.api.routes

import cats.MonadThrow
import cats.implicits.toFlatMapOps
import org.http4s._
import org.http4s.circe.JsonDecoder

import uz.scala.automateschool.ObjectId
import uz.scala.automateschool.algebras.SubjectsAlgebra
import uz.scala.automateschool.domain.AuthedUser
import uz.scala.automateschool.domain.subjects.SubjectInput
import uz.scala.http4s.syntax.all.deriveEntityEncoder
import uz.scala.http4s.syntax.all.http4SyntaxReqOps
import uz.scala.http4s.utils.Routes

final class SubjectsRoutes[F[_]: JsonDecoder: MonadThrow](subjectsAlgebra: SubjectsAlgebra[F])
    extends Routes[F, AuthedUser] {
  override val path: String = "/subjects"
  override val `private`: AuthedRoutes[AuthedUser, F] = AuthedRoutes.of {
    case ar @ POST -> Root as _ =>
      ar.req.decodeR[SubjectInput] { input =>
        subjectsAlgebra.create(input).flatMap(id => Created(ObjectId(id.value)))
      }

    case GET -> Root as _ =>
      subjectsAlgebra.getSubjects.flatMap(Ok(_))
  }
}
