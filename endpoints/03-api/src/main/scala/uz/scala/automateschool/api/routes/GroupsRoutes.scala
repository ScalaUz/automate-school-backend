package uz.scala.automateschool.api.routes

import cats.MonadThrow
import cats.implicits.toFlatMapOps
import org.http4s._
import org.http4s.circe.JsonDecoder

import uz.scala.automateschool.algebras.GroupsAlgebra
import uz.scala.automateschool.domain.AuthedUser
import uz.scala.automateschool.domain.groups.GroupsInput
import uz.scala.http4s.syntax.all.deriveEntityEncoder
import uz.scala.http4s.syntax.all.http4SyntaxReqOps
import uz.scala.http4s.utils.Routes

final class GroupsRoutes[F[_]: JsonDecoder: MonadThrow](groupsAlgebra: GroupsAlgebra[F])
    extends Routes[F, AuthedUser] {
  override val path: String = "/groups"
  override val `private`: AuthedRoutes[AuthedUser, F] = AuthedRoutes.of {
    case ar @ POST -> Root as _ =>
      ar.req.decodeR[GroupsInput] { input =>
        groupsAlgebra.create(input).flatMap(Created(_))
      }

    case GET -> Root as _ =>
      groupsAlgebra.getGroups.flatMap(Ok(_))
  }
}
