package uz.scala.automateschool.states

import cats.Monad
import cats.effect.kernel.Ref
import cats.effect.kernel.Ref.Make
import cats.implicits.catsSyntaxApplicativeByName
import cats.implicits.toFlatMapOps
import cats.implicits.toFunctorOps

import uz.scala.automateschool.states

trait ExtraHour[F[_]] {
  def get: F[Double]
  def increase(hour: Double): F[Unit]
  def decrease: F[Unit]
  def hasHours: F[Boolean]
}

object ExtraHour {
  def make[F[_]: Monad: Make]: F[ExtraHour[F]] =
    Ref.of[F, Double](0).map { ref =>
      new states.ExtraHour[F] {
        override def get: F[Double] =
          ref.get
        override def increase(hour: Double): F[Unit] =
          ref.update(_ + hour)
        override def decrease: F[Unit] =
          for {
            hasHour <- hasHours
            _ <- ref.update(_ - 1).whenA(hasHour)
          } yield {}
        override def hasHours: F[Boolean] =
          ref.get.map(_ != 0)
      }
    }
}
