package uz.scala.automateschool.algebras

import cats.MonadThrow
import cats.data.NonEmptyList
import cats.implicits._

import uz.scala.automateschool.domain.GroupId
import uz.scala.automateschool.domain.groups.Group
import uz.scala.automateschool.domain.groups.GroupsInput
import uz.scala.automateschool.effects.GenUUID
import uz.scala.automateschool.exception.AError
import uz.scala.automateschool.repos.GroupsRepository
import uz.scala.automateschool.repos.dto
import uz.scala.automateschool.utils.ID
import uz.scala.effects.Calendar

trait GroupsAlgebra[F[_]] {
  def create(subject: GroupsInput): F[Unit]
  def getGroups: F[List[Group]]
}

object GroupsAlgebra {
  def make[F[_]: MonadThrow: Calendar: GenUUID](
      groupsRepository: GroupsRepository[F]
    ): GroupsAlgebra[F] =
    new Impl[F](groupsRepository)

  private class Impl[F[_]: MonadThrow: GenUUID: Calendar](
      groupsRepository: GroupsRepository[F]
    ) extends GroupsAlgebra[F] {
    override def create(groupsInput: GroupsInput): F[Unit] = for {
      groups <- groupsInput.groups.traverse { groupInput =>
        for {
          id <- ID.make[F, GroupId]
          now <- Calendar[F].currentZonedDateTime
          group = dto.Group(
            id = id,
            name = groupInput.name,
            students = groupInput.students,
            createdAt = now,
            level = groupsInput.level,
          )
        } yield group
      }
      nonEmptyGroups <- NonEmptyList
        .fromList(groups)
        .fold(AError.BadRequest("Groups are empty").raiseError[F, NonEmptyList[dto.Group]])(
          _.pure[F]
        )
      _ <- groupsRepository.create(nonEmptyGroups)
    } yield {}

    override def getGroups: F[List[Group]] = groupsRepository.getAll.map(_.map(_.toDomain))
  }
}
