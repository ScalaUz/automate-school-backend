package uz.scala.automateschool.domain.users

import eu.timepit.refined.types.string.NonEmptyString

import uz.scala.automateschool.EmailAddress

case class UserUpdateInput(
    name: NonEmptyString
  )
