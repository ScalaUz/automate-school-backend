package uz.scala.automateschool.exception

import io.circe.Json

sealed trait AError extends Throwable {
  def cause: String
  def errorCode: String
  override def getMessage: String = cause
  val json: Json =
    Json
      .obj(
        "message" -> Json.fromString(cause),
        "error_code" -> Json.fromString(errorCode),
      )
}

object AError {
  final case class Internal(cause: String) extends AError {
    override def errorCode: String = "INTERNAL"
  }
  final case class NotAllowed(cause: String) extends AError {
    override def errorCode: String = "NOT_ALLOWED"
  }
  final case class BadRequest(cause: String) extends AError {
    override def errorCode: String = "BAD_REQUEST"
  }

  final case class UnprocessableEntity(cause: String) extends AError {
    override def errorCode: String = "UNPROCESSABLE_ENTITY"
  }

  sealed trait AuthError extends AError
  object AuthError {
    final case class NoSuchUser(cause: String) extends AuthError {
      override def errorCode: String = "NOT_FOUND"
    }
    final case class InvalidToken(cause: String) extends AuthError {
      override def errorCode: String = "INVALID_TOKEN"
    }
    final case class PasswordDoesNotMatch(cause: String) extends AuthError {
      override def errorCode: String = "AUTHENTICATION"
    }

    final case class LimitExceeded(cause: String) extends AuthError {
      override def errorCode: String = "LIMIT_EXCEEDED"
    }
  }
}
