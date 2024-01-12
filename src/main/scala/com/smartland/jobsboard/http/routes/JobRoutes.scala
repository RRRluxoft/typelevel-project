package com.smartland.jobsboard.http.routes

import cats.*
import cats.effect.*
import cats.implicits.*
import com.smartland.jobsboard.http.job.{Job, JobInfo}
import com.smartland.jobsboard.http.responses.FailureResponse
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*

import java.util.UUID
import scala.collection.mutable

class JobRoutes[F[_] : Concurrent] private extends Http4sDsl[F] {

  // database
  private val db = mutable.Map[UUID, Job]()

  // POSt /jobs?offset=x&limit=y { filter } // TODO
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root =>
      Ok(db.values)
  }

  // GET /jobs/uuid
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      db.get(id)
        .fold(NotFound(FailureResponse(s"Job $id doesnt exist")))(job => Ok(job))
  }

  // POST /jobs/create { jobInfo }
  private def createJob(jobInfo: JobInfo): F[Job] = Job(
    id = UUID.randomUUID(),
    date = System.currentTimeMillis(),
    ownerEmail = "TODO@smart.land",
    jobInfo = jobInfo,
    active = true
  ).pure[F]
  private val createJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@POST -> Root / "create" =>
      for {
        jobInfo <- req.as[JobInfo]
        job <- createJob(jobInfo)
        resp <- Created(job.id)
      } yield resp
  }

  // PUT /jobs/uuid { jobInfo }
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@PUT -> Root / UUIDVar(id) =>
      db.get(id) match
        case Some(job) =>
          for {
            jobInfo <- req.as[JobInfo]
            _ <- db.put(id, job.copy(jobInfo = jobInfo)).pure[F]
            resp <- Ok()
          } yield resp

        case None => NotFound(FailureResponse(s"Cannot update job $id not found"))
  }

  // DELETE /jobs/uuid
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@DELETE -> Root / UUIDVar(id) =>
      db.remove(id) match
        case Some(job) => Ok()
        case None => NotFound(FailureResponse(s"Cannot delete job $id not found"))
  }

  val routes: HttpRoutes[F] = Router(
    "/jobs" -> (allJobsRoute <+> findJobRoute <+> createJobRoute <+> updateJobRoute <+> deleteJobRoute)
  )

}

object JobRoutes {
  def apply[F[_] : Concurrent] = new JobRoutes[F]
}
