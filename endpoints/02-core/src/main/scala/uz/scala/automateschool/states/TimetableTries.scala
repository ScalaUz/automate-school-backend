package uz.scala.automateschool.states

import cats.Monad
import cats.effect.kernel.Ref
import cats.effect.kernel.Ref.Make
import cats.implicits.toFunctorOps

import uz.scala.automateschool.domain.TeacherId
import uz.scala.automateschool.states

trait TimetableTries[F[_]] {
  def set(id: TeacherId): F[Unit]
  def needRebuild(id: TeacherId): F[Boolean]
}

object TimetableTries {
  def make[F[_]: Monad: Make]: F[TimetableTries[F]] =
    Ref.of[F, Map[TeacherId, Int]](Map.empty[TeacherId, Int]).map { ref =>
      new states.TimetableTries[F] {
        override def set(id: TeacherId): F[Unit] =
          ref.update(r => r.updated(id, r.getOrElse(id, 0) + 1))
        override def needRebuild(id: TeacherId): F[Boolean] =
          ref.get.map(_.getOrElse(id, 0) > 6)
      }
    }
}
