package uz.scala.automateschool

import uz.scala.automateschool.Language._

object ResponseMessages {
  val USER_NOT_FOUND: Map[Language, String] = Map(
    En -> "User not found",
    Ru -> "Пользователь не найден",
    Uz -> "Foydalanuvchi topilmadi",
  )

  val USER_UPDATED: Map[Language, String] = Map(
    En -> "User updated",
    Ru -> "Пользователь обновлен",
    Uz -> "Foydalanuvchi yangilandi",
  )

  val USER_DELETED: Map[Language, String] = Map(
    En -> "User deleted",
    Ru -> "Пользователь удален",
    Uz -> "Foydalanuvchi o'chirildi",
  )

  val PASSWORD_DOES_NOT_MATCH: Map[Language, String] = Map(
    En -> "Sms code does not match",
    Ru -> "Код подтверждения не совпадает",
    Uz -> "SMS kodi mos kelmadi",
  )

  val LIMIT_EXCEEDED: Map[Language, String] = Map(
    En -> "Login Attempt Limit Exceeded:\nYou've made too many unsuccessful login attempts.\nFor security reasons, please try again after 24 hours.",
    Ru -> "Превышен лимит попыток входа:\nВы предприняли слишком много неудачных попыток входа.\nВ целях безопасности повторите попытку через 24 часа.",
    Uz -> "Kirish urinishlari chegarasidan oshib ketdi:\nKirish uchun juda koʻp muvaffaqiyatsiz urinishlar qildingiz.\nXavfsizlik nuqtai nazaridan, 24 soatdan keyin qayta urinib ko'ring.",
  )

  val INVALID_TOKEN: Map[Language, String] = Map(
    En -> "Invalid token or expired",
    Ru -> "Неверный токен или токен устарел",
    Uz -> "Yaroqsiz yoki eskirgan token",
  )

  val BEARER_TOKEN_NOT_FOUND: Map[Language, String] = Map(
    En -> "Bearer token not found",
    Ru -> "Токен не найден",
    Uz -> "Bearer token topilmadi",
  )
}

