package uz.scala.automateschool.repos.sql

import skunk._
import skunk.codec.all.int4
import skunk.codec.all.varchar
import skunk.implicits._

import uz.scala.automateschool.repos.dto.Lesson

private[repos] object TimetableSql {
  private val codec: Codec[Lesson] =
    (GroupsSql.id *: TeachersSql.id *: SubjectsSql.id *: varchar *: int4).to[Lesson]

  def insertBatch(timetable: List[Lesson]): Command[timetable.type] =
    sql"""INSERT INTO timetable VALUES (${codec.values.list(timetable)})""".command

  val selectAll: Query[Void, Lesson] =
    sql"""SELECT * FROM timetable""".query(codec)

  val deleteAll: Command[Void] =
    sql"""DELETE FROM timetable""".command
}
