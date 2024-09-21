package uz.scala.automateschool.repos.sql

import skunk._
import skunk.implicits._

import uz.scala.automateschool.domain.GroupId
import uz.scala.automateschool.repos.dto.Group

private[repos] object GroupsSql extends Sql[GroupId] {
  private val codec = (id *: nes *: posInt *: posInt *: zdt *: zdt.opt *: zdt.opt).to[Group]

  def insert(groups: List[Group]): Command[groups.type] =
    sql"""INSERT INTO groups VALUES (${codec.values.list(groups)})""".command

  val delete: Command[GroupId] =
    sql"""UPDATE groups SET deleted_at = now() WHERE id = $id""".command

  val update: Command[Group] =
    sql"""UPDATE groups
          SET name = $nes,
            level = $posInt,
            students = $posInt,
            updated_at = now()
          WHERE id = $id"""
      .command
      .contramap { (u: Group) =>
        u.name *: u.level *: u.students *: u.id *: EmptyTuple
      }

  val select: Query[Void, Group] =
    sql"""SELECT * FROM groups""".query(codec)

  val selectById: Query[GroupId, Group] =
    sql"""SELECT * FROM groups WHERE id = $id""".query(codec)
}
