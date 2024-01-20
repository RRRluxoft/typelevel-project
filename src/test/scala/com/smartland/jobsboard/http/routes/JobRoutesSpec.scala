package com.smartland.jobsboard.http.routes

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.*
import cats.implicits.*
import com.smartland.jobsboard.core.*
import com.smartland.jobsboard.domain.job.*
import com.smartland.jobsboard.fixtures.JobFixture
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.*
import org.http4s.implicits.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.util.UUID

class JobRoutesSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with Http4sDsl[IO]
    with JobFixture {
  /////////////////////////////////////////////////////////////////////////////
  // prep
  /////////////////////////////////////////////////////////////////////////////
  val jobs: Jobs[IO] = new Jobs[IO]:
    override def create(ownerEmail: String, jobInfo: JobInfo): IO[UUID] =
      IO.pure(NewJobUuid)

    override def all(): IO[List[Job]] =
      IO.pure(List(AwesomeJob))

    override def find(id: UUID): IO[Option[Job]] =
      if id == AwesomeJobUuid
      then IO.pure(Some(AwesomeJob))
      else IO.pure(None)

    override def update(id: UUID, jobInfo: JobInfo): IO[Option[Job]] =
      if id == AwesomeJobUuid
      then IO.pure(Some(UpdatedAwesomeJob))
      else IO.pure(None)

    override def delete(id: UUID): IO[Int] =
      if id == AwesomeJobUuid then IO.pure(1) else IO.pure(0)

  given logger: Logger[IO]      = Slf4jLogger.getLogger[IO]
  val jobRoutes: HttpRoutes[IO] = JobRoutes[IO](jobs).routes

  /////////////////////////////////////////////////////////////////////////////
  // tests
  /////////////////////////////////////////////////////////////////////////////

  "JobRoutes" - {
    "should return a job with a given id" in {
      for {
        // simulate an HTTP request
        response <- jobRoutes.orNotFound.run(
          Request(method = Method.GET, uri = uri"/jobs/843df718-ec6e-4d49-9289-f799c0f40064")
        )
        // get the HTTP response
        retrieved <- response.as[Job]
      } yield {
        // make some assertions
        response.status shouldBe Status.Ok
        retrieved shouldBe AwesomeJob
      }
    }

    "should return all jobs" in {
      for {
        // simulate an HTTP request
        response <- jobRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/jobs")
        )
        // get the HTTP response
        retrieved <- response.as[List[Job]]
      } yield {
        // make some assertions
        response.status shouldBe Status.Ok
        retrieved shouldBe List(AwesomeJob)
      }
    }

    "should create a new job" in {
      for {
        // simulate an HTTP request
        response <- jobRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/jobs/create")
            .withEntity(AwesomeJob.jobInfo)
        )
        // get the HTTP response
        retrieved <- response.as[UUID]
      } yield {
        // make some assertions
        response.status shouldBe Status.Created
        retrieved shouldBe NewJobUuid
      }
    }

    "should only update the job that exists" in {
      for {
        //  PUT /jobs/uuid { jobInfo }
        responseOk <- jobRoutes.orNotFound.run(
          Request(method = Method.PUT, uri = uri"/jobs/843df718-ec6e-4d49-9289-f799c0f40064")
            .withEntity(UpdatedAwesomeJob.jobInfo)
        )
        responseInvalid <- jobRoutes.orNotFound.run(
          Request(method = Method.PUT, uri = uri"/jobs/843df718-ec6e-4d49-9289-000000000000")
            .withEntity(UpdatedAwesomeJob.jobInfo)
        )
      } yield {
        // make some assertions
        responseOk.status shouldBe Status.Ok
        responseInvalid.status shouldBe Status.NotFound
      }
    }

    "should delete the job that exists only" in {
      for {
        //  DELETE /jobs/uuid
        responseOk <- jobRoutes.orNotFound.run(
          Request(method = Method.DELETE, uri = uri"/jobs/843df718-ec6e-4d49-9289-f799c0f40064")
        )
        responseInvalid <- jobRoutes.orNotFound.run(
          Request(method = Method.DELETE, uri = uri"/jobs/843df718-ec6e-4d49-9289-000000000000")
            .withEntity(UpdatedAwesomeJob.jobInfo)
        )
      } yield {
        // make some assertions
        responseOk.status shouldBe Status.Ok
        responseInvalid.status shouldBe Status.NotFound
      }
    }

  }

}
