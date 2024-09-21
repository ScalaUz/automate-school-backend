package uz.scala.automateschool.algebras

import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

import cats.Applicative
import cats.MonadThrow
import cats.data.NonEmptyList
import cats.effect.Temporal
import cats.effect.kernel.Ref.Make
import cats.effect.std.Random
import cats.implicits._
import eu.timepit.refined.types.numeric.PosInt
import org.typelevel.log4cats.Logger

import uz.scala.effects.Calendar
import uz.scala.automateschool.domain.GroupId
import uz.scala.automateschool.domain.SubjectId
import uz.scala.automateschool.domain.TeacherId
import uz.scala.automateschool.domain.timetable.Timetable
import uz.scala.automateschool.effects.GenUUID
import uz.scala.automateschool.repos.GroupsRepository
import uz.scala.automateschool.repos.SubjectsRepository
import uz.scala.automateschool.repos.TeachersRepository
import uz.scala.automateschool.repos.TimetableRepository
import uz.scala.automateschool.repos.dto
import uz.scala.automateschool.repos.dto.Group
import uz.scala.automateschool.repos.dto.Lesson
import uz.scala.automateschool.states.TeachersWorkload
import uz.scala.automateschool.states.TimetableState
import uz.scala.automateschool.states.TimetableTries

trait TimetableAlgebra[F[_]] {
  def getTimetable: F[Map[String, Map[String, List[Timetable]]]]
  def generateTimetable: F[Map[String, Map[String, List[Timetable]]]]
}

object TimetableAlgebra {
  def make[F[_]: MonadThrow: Calendar: GenUUID: Logger: Random: Make: Temporal](
      timetableRepository: TimetableRepository[F],
      subjectsRepository: SubjectsRepository[F],
      groupsRepository: GroupsRepository[F],
      teachersRepository: TeachersRepository[F],
    ): TimetableAlgebra[F] =
    new Impl[F](timetableRepository, subjectsRepository, groupsRepository, teachersRepository)

  sealed trait LessonResult
  private object LessonResult {
    case object SchoolDayFilled extends LessonResult
    case object TeacherHasLesson extends LessonResult
    case object LessonHasTheDay extends LessonResult
    case object TeacherHourOver extends LessonResult
    case object Success extends LessonResult

    val schoolDayFilled: LessonResult = SchoolDayFilled: LessonResult
    val success: LessonResult = Success: LessonResult
    val teacherHourOver: LessonResult = TeacherHourOver: LessonResult
    val teacherHasLesson: LessonResult = TeacherHasLesson: LessonResult
    val lessonHasTheDay: LessonResult = LessonHasTheDay: LessonResult
  }

  private val SchoolDays: List[String] = DayOfWeek
    .values()
    .filterNot(_.getValue == 7)
    .map(_.getDisplayName(TextStyle.SHORT, Locale.US))
    .toList

  private class Impl[F[_]: MonadThrow: GenUUID: Calendar: Random: Make: Temporal](
      timetableRepository: TimetableRepository[F],
      subjectsRepository: SubjectsRepository[F],
      groupsRepository: GroupsRepository[F],
      teachersRepository: TeachersRepository[F],
    )(implicit
      logger: Logger[F]
    ) extends TimetableAlgebra[F] {
    override def getTimetable: F[Map[String, Map[String, List[Timetable]]]] =
      for {
        subjects <- subjectsRepository.getAll
        groups <- groupsRepository.getAll
        teachers <- teachersRepository.getAll
        lessons <- timetableRepository.getAll
        subjectNameById = subjects.map(s => s.id -> s).toMap
        teacherNameById = teachers.map(t => t.id -> t).toMap
        groupsById = groups.map(g => g.id -> g).toMap
        timetable = lessons.groupBy(_.groupId).map {
          case g -> lessons => g -> lessons.groupBy(_.weekday)
        }
      } yield makeTimetable(timetable, groupsById, subjectNameById, teacherNameById)

    override def generateTimetable: F[Map[String, Map[String, List[Timetable]]]] =
      for {
        subjects <- subjectsRepository.getAll
        groups <- groupsRepository.getAll
        teachers <- teachersRepository.getAll
        studyHours <- subjectsRepository.studyHours

        subjectById = subjects.map(s => s.id -> s).toMap
        teacherById = teachers.map(t => t.id -> t).toMap
        groupsById = groups.map(g => g.id -> g).toMap

        timetable <- processGeneration(groupsById, studyHours, subjectById, teacherById)
        lessons = timetable.values.flatMap(_.values).flatten.toList
        _ <- timetableRepository.clean
        _ <- NonEmptyList.fromList(lessons).traverse(timetableRepository.create)
      } yield makeTimetable(timetable, groupsById, subjectById, teacherById)

    private def processGeneration(
        groupById: Map[GroupId, Group],
        studyHours: List[dto.StudyHour],
        subjectById: Map[SubjectId, dto.Subject],
        teacherById: Map[TeacherId, dto.Teacher],
      ): F[Map[GroupId, Map[String, List[dto.Lesson]]]] =
      for {
        _ <- logger.info(s"Generating timetable for ${groupById.size} groups")
        teacherSubjects <- subjectsRepository.teachersSubjects
        teachersBySubjectId = teacherSubjects
          .toList
          .flatMap {
            case tId -> subjects =>
              subjects.map(s => s.id -> tId)
          }
          .groupMap(_._1)(_._2)
        shuffledTeachers <- teachersBySubjectId
          .toList
          .traverse {
            case subjectId -> teachers =>
              Random[F].shuffleList(teachers).map(subjectId -> _)
          }
          .map(_.toMap)
        initialTimetable = groupById.map {
          case groupId -> _ => groupId -> SchoolDays.map(_ -> List.empty[Lesson]).toMap
        }
        implicit0(timetable: TimetableState[F]) <- TimetableState.make[F](initialTimetable)
        implicit0(teacherWorkload: TeachersWorkload[F]) <- TeachersWorkload.make[F]
        implicit0(timetableTries: TimetableTries[F]) <- TimetableTries.make[F]
        _ <- teacherById.toList.traverse {
          case teacherId -> teacher =>
            teacherWorkload.set(teacherId, teacher.workload.value)
        }

        subjectsByLevel = studyHours.groupBy(_.level)

        sortedGroups = groupById
          .values
          .toList
          .filter(_.level.value > 4)
          .sortBy(_.level.value)

        timetable <- sortedGroups
          .traverse_(g =>
            generateForGroup(
              g.id,
              groupById,
              subjectsByLevel,
              shuffledTeachers,
              subjectById,
            )
          )
          .flatMap { _ =>
            timetable.get
          }
          .handleErrorWith { _ =>
            println("+++++++++++++++++++++++ ERROR +++++++++++++++++++++++++++")
            processGeneration(groupById, studyHours, subjectById, teacherById)
          }
      } yield timetable

    private def generateForGroup(
        groupId: GroupId,
        groupsById: Map[GroupId, Group],
        subjectsByLevel: Map[PosInt, List[dto.StudyHour]],
        teachersBySubjectId: Map[SubjectId, List[TeacherId]],
        subjectById: Map[SubjectId, dto.Subject],
      )(implicit
        timetable: TimetableState[F],
        teachersWorkload: TeachersWorkload[F],
        timetableTries: TimetableTries[F],
      ): F[List[Unit]] = {
      val group = groupsById(groupId)
      val studyHour = subjectsByLevel(group.level)
      val maxLesson = (studyHour.map(_.hour.value).sum / 6).ceil.toInt
      println("=================== NEW GROUP =====================")
      println(
        s"${group.level}-${group.name}, studyHour: ${studyHour.map(_.hour.value).sum}, maxLesson: $maxLesson"
      )
      studyHour
        .sortBy(sh => subjectById(sh.subjectId).category)
        .traverse { sh =>
          setGroupLessons(
            groupId,
            sh.subjectId,
            teachersBySubjectId.getOrElse(sh.subjectId, List.empty),
            sh.hour.value,
            maxLesson,
          )
        }
    }

    private def setGroupLessons(
        groupId: GroupId,
        subjectId: SubjectId,
        teachersIds: List[TeacherId],
        studyHour: Int,
        maxLesson: Int,
      )(implicit
        timetable: TimetableState[F],
        teachersWorkload: TeachersWorkload[F],
        timetableTries: TimetableTries[F],
      ): F[Unit] =
      (1 to studyHour).toList.traverse_ { _ =>
        for {
          freeDays <- timetable.availableDays(groupId, maxLesson)
          _ <- addLesson(freeDays, groupId, subjectId, teachersIds, maxLesson, studyHour > 6)
        } yield {}
      }

    private def addLesson(
        availableDays: List[String],
        groupId: GroupId,
        subjectId: SubjectId,
        teachers: List[TeacherId],
        maxLesson: Int,
        addMore: Boolean,
      )(implicit
        timetable: TimetableState[F],
        teachersWorkload: TeachersWorkload[F],
        timetableTries: TimetableTries[F],
      ): F[Unit] =
      teachers match {
        case ::(head, next) =>
          if (availableDays.isEmpty)
            for {
              freeDays <- timetable.availableDays(groupId, maxLesson)
              teachersHasHour <- teachers
                .traverse(id => teachersWorkload.hasHours(id).map(id -> _))
                .map(_.filter(_._2).map(_._1))
              _ = println(s"availableDays empty freeDays: $freeDays, teacher: $teachers")
              _ <- addLesson(freeDays, groupId, subjectId, teachersHasHour, maxLesson, addMore)
            } yield {}
          else
            for {
              needRebuild <- timetableTries.needRebuild(head)
              _ <- new Exception().raiseError[F, Unit].whenA(needRebuild)
              day <- Random[F].elementOf(availableDays)
              _ = println(s"availableDays: $availableDays")
              _ = println(s"day: $day, maxLesson: $maxLesson")
              busyMoments <- timetable.busyMoments(groupId, day)
              _ = println(s"busyMoments: $busyMoments")
              freeMoments = (1 to maxLesson).diff(busyMoments).toList
              _ = println(s"freeMoments: $freeMoments")
              result <- addLessonIfAvailable(day, groupId, subjectId, freeMoments, head)
              _ = println(s"result: $result")
              restDays = availableDays.filterNot(_ == day)
              existingLessonMin <- timetable.existingLessonMin(groupId)
              _ = println(s"restDays: $restDays")
              _ <- result match {
                case LessonResult.SchoolDayFilled | LessonResult.LessonHasTheDay =>
                  addLesson(restDays, groupId, subjectId, teachers, maxLesson, addMore)
                case LessonResult.TeacherHasLesson =>
                  for {
                    _ <- timetableTries.set(head)
                    _ <- addLesson(
                      existingLessonMin,
                      groupId,
                      subjectId,
                      teachers,
                      maxLesson,
                      addMore,
                    )
                  } yield {}
                case LessonResult.TeacherHourOver =>
                  addLesson(restDays, groupId, subjectId, next, maxLesson, addMore)
                case LessonResult.Success => Applicative[F].unit
              }
            } yield {}
        case Nil => Applicative[F].unit
      }

    private def addLessonIfAvailable(
        day: String,
        groupId: GroupId,
        subjectId: SubjectId,
        freeMoments: List[Int],
        teacherId: TeacherId,
      )(implicit
        timetable: TimetableState[F],
        teachersWorkload: TeachersWorkload[F],
      ): F[LessonResult] =
      freeMoments match {
        case ::(moment, restMoments) =>
          for {
            isTeacherFree <- timetable.isTeacherFree(teacherId, day, moment)
            _ = println(s"isTeacherFree: $isTeacherFree, $teacherId, $day, $moment")
            isFreeMoment <- timetable.isFreeMoment(groupId, day, moment)

            _ = println(s"isFreeMoment: $isFreeMoment")
            hasTeacherHours <- teachersWorkload.hasHours(teacherId)
            todayHasLesson <- timetable.todayHasLesson(groupId, day, subjectId)
            existingLessonsCount <- timetable.lessonCountOnWeek(groupId, subjectId)
            _ = println(
              s"todayHasLesson: $todayHasLesson, existingLessonMin: $existingLessonsCount"
            )

            lesson = Lesson(groupId, teacherId, subjectId, day, moment)
            needWrite = (existingLessonsCount == 6 && todayHasLesson) || !todayHasLesson
            res <-
              if (!hasTeacherHours)
                teachersWorkload.remove(teacherId).as(LessonResult.teacherHourOver)
              else if (isFreeMoment && isTeacherFree && needWrite)
                teachersWorkload.decrease(teacherId).flatMap { _ =>
                  timetable.addLesson(groupId, day, lesson).as(LessonResult.success)
                }
              else if (!needWrite)
                LessonResult.lessonHasTheDay.pure[F]
              else if (isFreeMoment && restMoments.isEmpty)
                LessonResult.teacherHasLesson.pure[F]
              else
                addLessonIfAvailable(
                  day,
                  groupId,
                  subjectId,
                  restMoments,
                  teacherId,
                ) // .delayBy(400.millis)
          } yield res
        case Nil => LessonResult.schoolDayFilled.pure[F]
      }

    private def makeTimetable(
        timetable: Map[GroupId, Map[String, List[dto.Lesson]]],
        groupsById: Map[GroupId, Group],
        subjectNameById: Map[SubjectId, dto.Subject],
        teacherNameById: Map[TeacherId, dto.Teacher],
      ): Map[String, Map[String, List[Timetable]]] =
      timetable.map {
        case groupId -> weekdays =>
          val group = groupsById(groupId)

          s"${group.level}-${group.name}" -> weekdays.map {
            case weekday -> lessons =>
              weekday -> lessons.sortBy(_.moment).map { lesson =>
                Timetable(
                  moment = lesson.moment,
                  subject = Timetable.Subject(
                    name = subjectNameById(lesson.subjectId).name,
                    id = lesson.subjectId,
                  ),
                  teacher = Timetable.Teacher(
                    name = teacherNameById(lesson.teacherId).name,
                    id = lesson.teacherId,
                  ),
                  groupId = group.id,
                )
              }
          }
      }
  }
}
