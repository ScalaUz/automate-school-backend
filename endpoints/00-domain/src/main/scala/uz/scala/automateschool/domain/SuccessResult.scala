package uz.scala.automateschool.domain

import io.circe.generic.JsonCodec

@JsonCodec
case class SuccessResult(
    message: String
  )
