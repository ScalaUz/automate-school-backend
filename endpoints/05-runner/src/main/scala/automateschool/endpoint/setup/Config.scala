package automateschool.endpoint.setup

import uz.scala.database.MigrationsConfig
import uz.scala.http4s.HttpServerConfig
import uz.scala.redis.RedisConfig
import uz.scala.skunk.DataBaseConfig
import uz.scala.automateschool.auth.AuthConfig

case class Config(
    http: HttpServerConfig,
    database: DataBaseConfig,
    auth: AuthConfig,
    redis: RedisConfig,
  ) {
  lazy val migrations: MigrationsConfig = MigrationsConfig(
    hostname = database.host.value,
    port = database.port.value,
    database = database.database.value,
    username = database.user.value,
    password = database.password.value,
    schema = "public",
    location = "db/migration",
  )
}
