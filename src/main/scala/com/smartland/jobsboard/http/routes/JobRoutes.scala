package com.smartland.jobsboard.http.routes

import cats.*
import cats.effect.*
import cats.implicits.*
import com.smartland.jobsboard.core.Jobs
import com.smartland.jobsboard.domain.job.*
import com.smartland.jobsboard.domain.pagination.*
import com.smartland.jobsboard.http.responses.*
import com.smartland.jobsboard.http.validation.syntax.*
import com.smartland.jobsboard.http.validation.validators.*
import com.smartland.jobsboard.logging.syntax.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*
import org.typelevel.log4cats.Logger

import java.util.UUID
import scala.collection.mutable

class JobRoutes[F[_] : Concurrent : Logger] private(jobs: Jobs[F]) extends HttpValidationDsl[F] {

  //  // database
  //  private val db = mutable.Map[UUID, Job]()

  object OffsetQueryParam extends OptionalQueryParamDecoderMatcher[Int]("offset")

  object LimitQueryParam extends OptionalQueryParamDecoderMatcher[Int]("limit")

  // POST /jobs?offset=x&limit=y { filter } // TODO
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@POST -> Root :? LimitQueryParam(limit) +& OffsetQueryParam(offset) =>
      for {
        filter <- req.as[JobFilter]
        jobList <- jobs.all(filter, Pagination(limit, offset))
        resp <- Ok(jobList)
      } yield resp
  }

  // GET /jobs/uuid
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      jobs
        .find(id)
        .flatMap(_.fold(NotFound(FailureResponse(s"Job $id doesnt exist")))(job => Ok(job)))
  }

  // POST /jobs/create { jobInfo }
  private val createJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@POST -> Root / "create" =>
      req.validate[JobInfo] { jobInfo =>
        for {
          //          jobInfo <- req.as[JobInfo].logError(e => s"Parsing payload failed: $e")
          jobId <- jobs.create("TODO@smart.land", jobInfo)
          resp <- Created(jobId)
          //        _ <- Logger[F].info(s"Created job: $job")
        } yield resp
      }
  }

  // PUT /jobs/uuid { jobInfo }
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@PUT -> Root / UUIDVar(id) =>
      req.validate[JobInfo] { jobInfo =>
        for {
          //          jobInfo     <- req.as[JobInfo]
          maybeNewJob <- jobs.update(id, jobInfo)
          resp <- maybeNewJob
            .fold(
              NotFound(FailureResponse(s"Cannot update job $id not found"))
            )(_ => Ok())
        } yield resp
      }
  }

  // DELETE /jobs/uuid
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@DELETE -> Root / UUIDVar(id) =>
      jobs.find(id).flatMap {
        case Some(job) =>
          for {
            _ <- jobs.delete(id)
            _ <- Logger[F].info(s"Job deleted id=$id")
            res <- Ok()
          } yield res
        case None => NotFound(FailureResponse(s"Cannot delete job $id not found"))
      }
  }

  val routes: HttpRoutes[F] = Router(
    "/jobs" -> (allJobsRoute <+> findJobRoute <+> createJobRoute <+> updateJobRoute <+> deleteJobRoute)
  )

}

object JobRoutes {
  def apply[F[_] : Concurrent : Logger](jobs: Jobs[F]) = new JobRoutes[F](jobs)
}
