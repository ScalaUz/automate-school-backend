package uz.scala.automateschool

import enumeratum.EnumEntry.Lowercase
import enumeratum._

sealed trait Language extends Lowercase
object Language extends Enum[Language] with CirceEnum[Language] {
  case object En extends Language
  case object Ru extends Language
  case object Uz extends Language

  val values: IndexedSeq[Language] = findValues
}
