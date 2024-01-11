package com.smartland.jobsboard.http.routes

import cats.*
import cats.implicits.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.server.*

class JobRoutes[F[_]: Monad] private extends Http4sDsl[F] {

  // POSt /jobs?offset=x&limit=y { filter } // TODO
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root =>
      Ok("TODO")
  }

  // GET /jobs/uuid
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      Ok(s"TODO find job for $id")
  }

  // POST /jobs/create { jobInfo }
  private val createJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root / "create" =>
      Ok("TODO")
  }

  // PUT /jobs/uuid { jobInfo }
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case PUT -> Root / UUIDVar(id) =>
      Ok(s"TODO update jod for $id")
  }

  // DELETE /jobs/uuid
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> Root / UUIDVar(id) =>
      Ok(s"TODO delete job at $id")
  }

  val routes: HttpRoutes[F] = Router(
    "/jobs" -> (allJobsRoute <+> findJobRoute <+> createJobRoute <+> updateJobRoute <+> deleteJobRoute)
  )

}

object JobRoutes {
  def apply[F[_]: Monad] = new JobRoutes[F]
}
