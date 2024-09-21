package uz.scala.timetable.utils

import scala.reflect.ClassTag

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits.catsSyntaxApplicativeErrorId
import pureconfig.ConfigReader
import pureconfig.ConfigSource
import uz.scala.automate_school.Mode._
import uz.scala.automate_school.Mode

object ConfigLoader {
  def load[F[_]: Sync, Conf: ConfigReader: ClassTag]: F[Conf] = {
    val fileName = Mode.current match {
      case Development => "local"
      case Test => "testing"
      case Staging => "staging"
      case Production => "production"
    }
    EitherT
      .fromEither[F](
        ConfigSource
          .file(s"conf/$fileName.conf")
          .recoverWith(_ => ConfigSource.resources(s"$fileName.conf"))
          .recoverWith(_ => ConfigSource.default)
          .load[Conf]
      )
      .valueOrF { failures =>
        new RuntimeException(s"Boom 💥💥💥 \n${failures.prettyPrint(2)}").raiseError[F, Conf]
      }
  }
}
