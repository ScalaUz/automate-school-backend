package uz.scala.automateschool.states

import cats.Monad
import cats.effect.kernel.Ref
import cats.effect.kernel.Ref.Make
import cats.implicits.toFunctorOps

import uz.scala.automateschool.domain.GroupId
import uz.scala.automateschool.domain.SubjectId
import uz.scala.automateschool.domain.TeacherId
import uz.scala.automateschool.repos.dto.Lesson

trait TimetableState[F[_]] {
  def get: F[Map[GroupId, Map[String, List[Lesson]]]]
  def groupLessons(id: GroupId): F[Map[String, List[Lesson]]]
  def lessonsOn(id: GroupId, day: String): F[List[Lesson]]
  def addLesson(
      id: GroupId,
      day: String,
      lesson: Lesson,
    ): F[Unit]
  def isFreeMoment(
      id: GroupId,
      day: String,
      moment: Int,
    ): F[Boolean]
  def isTeacherFree(
      teacherId: TeacherId,
      day: String,
      moment: Int,
    ): F[Boolean]
  def busyMoments(id: GroupId, day: String): F[List[Int]]
  def availableDays(id: GroupId, maxLesson: Int): F[List[String]]
  def todayHasLesson(
      id: GroupId,
      day: String,
      subjectId: SubjectId,
    ): F[Boolean]
  def existingLessonMin(
      id: GroupId
    ): F[List[String]]
  def lessonCountOnWeek(
      id: GroupId,
      subjectId: SubjectId,
    ): F[Int]
}

object TimetableState {
  def make[F[_]: Monad: Make](
      initial: Map[GroupId, Map[String, List[Lesson]]]
    ): F[TimetableState[F]] =
    Ref.of[F, Map[GroupId, Map[String, List[Lesson]]]](initial).map { ref =>
      new TimetableState[F] {
        override def get: F[Map[GroupId, Map[String, List[Lesson]]]] = ref.get
        override def lessonsOn(id: GroupId, day: String): F[List[Lesson]] =
          ref.get.map(_(id)(day))
        override def addLesson(
            id: GroupId,
            day: String,
            lesson: Lesson,
          ): F[Unit] =
          ref.update { timetable =>
            val lessons = timetable(id)(day)
            val groupLessons = timetable(id).updated(day, lesson +: lessons)
            timetable.updated(id, groupLessons)
          }
        override def groupLessons(id: GroupId): F[Map[String, List[Lesson]]] =
          ref.get.map(_(id))
        override def isFreeMoment(
            id: GroupId,
            day: String,
            moment: Int,
          ): F[Boolean] =
          ref.get.map {
            _(id)(day).forall(_.moment != moment)
          }
        override def isTeacherFree(
            teacherId: TeacherId,
            day: String,
            moment: Int,
          ): F[Boolean] =
          ref.get.map {
            !_.exists {
              case _ -> lessons =>
                lessons(day).exists(l => l.teacherId == teacherId && l.moment == moment)
            }
          }
        override def busyMoments(id: GroupId, day: String): F[List[Int]] =
          lessonsOn(id, day).map(_.map(_.moment.toInt))
        override def availableDays(id: GroupId, maxLesson: Int): F[List[String]] =
          groupLessons(id).map { lessons =>
            val days = lessons.filterNot {
              case _ -> lessons =>
                lessons.length >= maxLesson
            }
            val min = days.map(_._2.length).min
            lessons.collect {
              case day -> lessons if lessons.length == min => day
            }.toList
          }

        override def todayHasLesson(
            id: GroupId,
            day: String,
            subjectId: SubjectId,
          ): F[Boolean] =
          groupLessons(id).map { dayToLessons =>
            dayToLessons(day).exists(_.subjectId == subjectId)
          }

        override def existingLessonMin(
            id: GroupId
          ): F[List[String]] =
          groupLessons(id).map { dayToLessons =>
            val min = dayToLessons.map(_._2.length).min
            dayToLessons.collect {
              case day -> lessons if lessons.length == min => day
            }.toList
          }

        override def lessonCountOnWeek(
            id: GroupId,
            subjectId: SubjectId,
          ): F[Int] =
          groupLessons(id).map { dayToLessons =>
            dayToLessons.foldLeft(0) {
              case acc -> (_ -> lessons) =>
                if (lessons.exists(_.subjectId == subjectId))
                  acc + 1
                else
                  acc
            }
          }
      }
    }
}
