package uz.scala.timetable

import java.util.UUID

import io.circe.generic.JsonCodec

@JsonCodec(encodeOnly = true)
case class ObjectId(
    id: UUID
  )
