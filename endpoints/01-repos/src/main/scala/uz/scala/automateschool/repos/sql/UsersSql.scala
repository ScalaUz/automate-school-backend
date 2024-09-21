package uz.scala.automateschool.repos.sql

import skunk._
import skunk.implicits._

import uz.scala.automateschool.Phone
import uz.scala.automateschool.domain.UserId
import uz.scala.automateschool.repos.dto.User

private[repos] object UsersSql extends Sql[UserId] {
  private val codec = (id *: nes *: phone *: hash *: zdt *: zdt.opt *: zdt.opt).to[User]

  val findByPhone: Query[Phone, User] =
    sql"""SELECT * FROM users WHERE phone = $phone LIMIT 1""".query(codec)

  val findById: Query[UserId, User] =
    sql"""SELECT * FROM users WHERE id = $id LIMIT 1""".query(codec)

  val insert: Command[User] =
    sql"""INSERT INTO users VALUES ($codec)""".command

  val delete: Command[UserId] =
    sql"""UPDATE users SET deleted_at = now() WHERE id = $id""".command

  val update: Command[User] =
    sql"""UPDATE users SET name = $nes, updated_at = now() WHERE id = $id"""
      .command
      .contramap { (u: User) =>
        u.name *: u.id *: EmptyTuple
      }
}
