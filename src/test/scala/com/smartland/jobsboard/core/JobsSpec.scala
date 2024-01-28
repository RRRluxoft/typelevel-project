package com.smartland.jobsboard.core

import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import com.smartland.jobsboard.fixtures.*
import doobie.postgres.implicits.*
import doobie.implicits.*
import doobie.util.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

/**
  * Docker SHOULD be run
  */
class JobsSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with DoobieSpec with JobFixture {

  val initScript: String = "sql/jobs.sql"

  "Jobs 'algebra'" - {
    "should return no jobs if the given UUID does not exist" in {
      transactor.use { xa =>
        val program = for {
          jobs      <- LiveJobs[IO](xa)
          retrieved <- jobs.find(NotFoundJobUuid)
        } yield retrieved

        program.asserting(_ shouldBe None)
      }
    }
  }

  "should return a job by ID" in {
    transactor.use { xa =>
      val program = for {
        jobs      <- LiveJobs[IO](xa)
        retrieved <- jobs.find(AwesomeJobUuid)
      } yield retrieved

      program.asserting(_ shouldBe Some(AwesomeJob))
    }
  }

  "should retrieve all jobs" in {
    transactor.use { xa =>
      val program = for {
        jobs      <- LiveJobs[IO](xa)
        retrieved <- jobs.all()
      } yield retrieved

      program.asserting(_ shouldBe List(AwesomeJob))
    }
  }

  "should create a new job" in {
    transactor.use { xa =>
      val program = for {
        jobs     <- LiveJobs[IO](xa)
        jobId    <- jobs.create("tom@smartland.com", RockTheJvmNewJob)
        maybeJob <- jobs.find(jobId)
      } yield maybeJob

      program.asserting(_.map(_.jobInfo) shouldBe Some(RockTheJvmNewJob))
    }
  }

  "should return an updated job if exists" in {
    transactor.use { xa =>
      val program = for {
        jobs            <- LiveJobs[IO](xa)
        maybeUpdatedJob <- jobs.update(AwesomeJobUuid, UpdatedAwesomeJob.jobInfo)
      } yield maybeUpdatedJob

      program.asserting(_ shouldBe Some(UpdatedAwesomeJob))
    }
  }

  "should return None trying to update a job that does not exist" in {
    transactor.use { xa =>
      val program = for {
        jobs            <- LiveJobs[IO](xa)
        maybeUpdatedJob <- jobs.update(NotFoundJobUuid, UpdatedAwesomeJob.jobInfo)
      } yield maybeUpdatedJob

      program.asserting(_ shouldBe None)
    }
  }

  "should delete an existing job" in {
    transactor.use { xa =>
      val program = for {
        jobs                <- LiveJobs[IO](xa)
        numberOfDeletedJobs <- jobs.delete(AwesomeJobUuid)
        countOfJobs         <- sql"""
                     select count(*)
                     from jobs
                     where id = $AwesomeJobUuid
                     """
                                 .query[Int]
                                 .unique
                                 .transact(xa)
      } yield (numberOfDeletedJobs, countOfJobs)

      program.asserting {
        case (numberOfDeletedJobs, countOfJobs) =>
          numberOfDeletedJobs shouldBe 1
          countOfJobs shouldBe 0
      }
    }
  }

  "should return zero updated rows if the job ID to delete is not found" in {
    transactor.use { xa =>
      val program = for {
        jobs                <- LiveJobs[IO](xa)
        numberOfDeletedJobs <- jobs.delete(NotFoundJobUuid)
      } yield numberOfDeletedJobs

      program.asserting(_ shouldBe 0)
    }
  }

}
