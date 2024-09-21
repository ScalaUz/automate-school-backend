package uz.scala.automateschool.auth

import uz.scala.automateschool.domain.JwtAccessTokenKey
import uz.scala.automateschool.domain.TokenExpiration

case class AuthConfig(
    tokenKey: JwtAccessTokenKey,
    accessTokenExpiration: TokenExpiration,
    refreshTokenExpiration: TokenExpiration,
    otpAttemptExpiration: TokenExpiration,
    otpExpiration: TokenExpiration,
    otpAttemptsLimit: Int,
  )
