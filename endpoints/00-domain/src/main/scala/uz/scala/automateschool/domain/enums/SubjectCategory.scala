package uz.scala.automateschool.domain.enums

import enumeratum.values._

sealed abstract class SubjectCategory(val order: Int, val value: String) extends StringEnumEntry

object SubjectCategory extends StringEnum[SubjectCategory] with StringCirceEnum[SubjectCategory] {
  case object ExactSciences extends SubjectCategory(1, "Aniq fanlar")
  case object NaturalAndEconomicSciences extends SubjectCategory(2, "Tabiiy va iqtisodiy fanlar")
  case object Philology extends SubjectCategory(3, "Filologiya")
  case object SocialSciences extends SubjectCategory(4, "Ijtimoiy fanlar")
  case object PracticalSciences extends SubjectCategory(5, "Amaliy fanlar")
  override def values: IndexedSeq[SubjectCategory] = findValues

  implicit val ordering: Ordering[SubjectCategory] = Ordering.Int.on[SubjectCategory](_.order)
}
