package uz.scala.automateschool.states

import cats.Monad
import cats.effect.kernel.Ref
import cats.effect.kernel.Ref.Make
import cats.implicits.catsSyntaxApplicativeByName
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps

import uz.scala.automateschool.domain.TeacherId
import uz.scala.automateschool.states

trait TeachersWorkload[F[_]] {
  def set(id: TeacherId, hour: Int): F[Unit]
  def decrease(id: TeacherId): F[Unit]
  def hasHours(id: TeacherId): F[Boolean]
  def remove(id: TeacherId): F[Unit]
}

object TeachersWorkload {
  def make[F[_]: Monad: Make]: F[TeachersWorkload[F]] =
    Ref.of[F, Map[TeacherId, Int]](Map.empty[TeacherId, Int]).map { ref =>
      new states.TeachersWorkload[F] {
        override def set(id: TeacherId, hour: Int): F[Unit] =
          ref.update(_.updated(id, hour))
        override def decrease(id: TeacherId): F[Unit] =
          for {
            hasHour <- hasHours(id)
            _ <- ref.update(r => r.updated(id, r(id) - 1)).whenA(hasHour)
          } yield {}
        override def hasHours(id: TeacherId): F[Boolean] =
          ref.get.map(_.get(id).exists(_ != 0))

        override def remove(id: TeacherId): F[Unit] =
          ref.update(_ - id)
      }
    }
}
