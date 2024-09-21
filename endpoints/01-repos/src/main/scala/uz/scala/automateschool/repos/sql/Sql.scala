package uz.scala.automateschool.repos.sql

import skunk.Codec
import uz.scala.automateschool.effects.IsUUID

abstract class Sql[T: IsUUID] {
  val id: Codec[T] = identification[T]
}
