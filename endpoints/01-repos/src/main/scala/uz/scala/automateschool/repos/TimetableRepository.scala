package uz.scala.automateschool.repos

import cats.data.NonEmptyList
import cats.effect.Async
import cats.effect.Resource
import skunk._

import uz.scala.automateschool.repos.sql.TimetableSql
import uz.scala.skunk.syntax.all._

trait TimetableRepository[F[_]] {
  def getAll: F[List[dto.Lesson]]
  def create(timetable: NonEmptyList[dto.Lesson]): F[Unit]
  def clean: F[Unit]
}

object TimetableRepository {
  def make[F[_]: Async](
      implicit
      session: Resource[F, Session[F]]
    ): TimetableRepository[F] = new TimetableRepository[F] {
    override def getAll: F[List[dto.Lesson]] =
      TimetableSql.selectAll.all

    override def create(timetable: NonEmptyList[dto.Lesson]): F[Unit] = {
      val list = timetable.toList
      TimetableSql.insertBatch(list).execute(list)
    }

    override def clean: F[Unit] =
      TimetableSql.deleteAll.execute(Void)
  }
}
