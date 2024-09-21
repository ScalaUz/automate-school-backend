package uz.scala.automateschool.domain.users

import eu.timepit.refined.types.string.NonEmptyString

import uz.scala.automateschool.Phone

case class UserInput(
    name: NonEmptyString,
    phone: Phone,
  )
