package uz.scala.automateschool.repos.sql

import skunk._
import skunk.implicits._

import uz.scala.automateschool.domain.TeacherId
import uz.scala.automateschool.repos.dto.Teacher

private[repos] object TeachersSql extends Sql[TeacherId] {
  private val codec = (id *: nes *: nni *: zdt *: zdt.opt *: zdt.opt).to[Teacher]

  val insert: Command[Teacher] =
    sql"""INSERT INTO teachers VALUES ($codec)""".command

  val delete: Command[TeacherId] =
    sql"""UPDATE teachers SET deleted_at = now() WHERE id = $id""".command

  val update: Command[Teacher] =
    sql"""UPDATE teachers
          SET name = $nes,
            workload = $nni,
            updated_at = now()
          WHERE id = $id"""
      .command
      .contramap { (u: Teacher) =>
        u.name *: u.workload *: u.id *: EmptyTuple
      }

  val select: Query[Void, Teacher] =
    sql"""SELECT * FROM teachers""".query(codec)

  val selectById: Query[TeacherId, Teacher] =
    sql"""SELECT * FROM teachers WHERE id = $id""".query(codec)
}
