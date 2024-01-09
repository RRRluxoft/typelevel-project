package com.smartland.foundations

import cats.effect.{IO, IOApp}
import cats.implicits.*
import cats.*
import com.comcast.ip4s.Port
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.circe.*
import org.http4s.dsl.impl.{OptionalValidatingQueryParamDecoderMatcher, QueryParamDecoderMatcher}
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.{Header, HttpRoutes, QueryParamDecoder, Request}
import org.http4s.server.Router
import org.typelevel.ci.CIString

import java.util.UUID

object Http4s extends IOApp.Simple {

  // a456df8e-9d20-4cfa-8b47-d73eb46f0ed9
  type Student = String
  case class Instructor(firstName: String, lastName: String)
  case class Course(id: String, title: String, year: Int, students: List[Student], instructorNme: String)

  object CourseRepository {
    val catsEffectCourse = Course(
      id = "a456df8e-9d20-4cfa-8b47-d73eb46f0ed9",
      title = "Rock the JVM typelevel",
      year = 2024,
      List("Tom Waits", "RRR", "Smart"),
      instructorNme = "Marcin Odersky"
    )

    private val courses: Map[String, Course] = Map(catsEffectCourse.id -> catsEffectCourse)

    // API
    def findCourseById(courseId: UUID): Option[Course] = courses.get(courseId.toString)
    def findCourseByInstructor(instructorNme: String): List[Course] =
      courses.values.filter(_.instructorNme == instructorNme).toList
  }

  // Endpoints
  // GET 'localhost:8080/courses?instructor=Marcin%20Odersky&year=2024'
  // GET 'localhost:8080/courses/a456df8e-9d20-4cfa-8b47-d73eb46f0ed9/students'
  object InstructorQueryParameterMatcher extends QueryParamDecoderMatcher[String]("instructor")

  object YearQueryParameterMatcher extends OptionalValidatingQueryParamDecoderMatcher[Int]("year")

  def courseRoutes[F[_] : Monad]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root / "courses" :? InstructorQueryParameterMatcher(instructor) +& YearQueryParameterMatcher(maybeYear) =>
        val courses = CourseRepository.findCourseByInstructor(instructor)
        maybeYear.fold(Ok(courses.asJson)) { y =>
          y.fold(_ => BadRequest("Invalid year"), year => Ok(courses.filter(_.year == year).asJson))
        }

      case GET -> Root / "courses" / UUIDVar(courseId) / "students" =>
        CourseRepository.findCourseById(courseId)
          .fold(NotFound(s"No course with $courseId was found")) { c =>
            Ok(c.students.asJson, Header.Raw(CIString("My-custom-header"), "smartland"))
          }
      // .map(_.students) match
      //   case None => NotFound(s"No course with $courseId")
      //   case Some(students) => Ok(students.asJson)

      case GET -> Root => Ok("test ok!")
    }
  }

  def healthEndpoint[F[_] : Monad]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root / "health" => Ok("All going well!")
    }
  }

  def allRoutes[F[_] : Monad]: HttpRoutes[F] = courseRoutes[F] <+> healthEndpoint[F]

  // GET 'localhost:8080/api/courses?instructor=Marcin%20Odersky&year=2024'
  // GET 'localhost:8080/api/courses/a456df8e-9d20-4cfa-8b47-d73eb46f0ed9/students'
  // GET 'localhost:8080/private/health'
  def routerWithPathPrefixes = Router(
    "/api" -> courseRoutes[IO],
    "/private" -> healthEndpoint[IO]
  ).orNotFound

  override def run: IO[Unit] =
    // IO.println(UUID.randomUUID().toString())
    EmberServerBuilder.default[IO]
      .withPort(Port.fromInt(18080).get)
      .withHttpApp(routerWithPathPrefixes)
      .build
      .use(_ => IO.println("Server ready!") *> IO.never)

}
