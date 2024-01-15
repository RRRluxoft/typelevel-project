package com.smartland.jobsboard.playground

import cats.effect.{IO, IOApp, Resource}
import doobie.*
import doobie.implicits.*
import doobie.util.*
import doobie.hikari.HikariTransactor
import com.smartland.jobsboard.core.*
import com.smartland.jobsboard.domain.job.*

import scala.io.StdIn

object JobsPlayground extends IOApp.Simple {

  val postgresResource: Resource[IO, HikariTransactor[IO]] = for {
    ec <- ExecutionContexts.fixedThreadPool[IO](32)
    xa <- HikariTransactor.newHikariTransactor[IO](
            "org.postgresql.Driver",
            "jdbc:postgresql:board",
            "docker",
            "docker",
            ec
          )
  } yield xa

  val jobInfo = JobInfo.minimal(
    company = "Smart Land Co",
    title = "Software Engineere",
    description = "The best job ever",
    externalUrl = "smartland.com",
    remote = true,
    location = "Anywhere"
  )

  override def run: IO[Unit] = postgresResource.use { xa =>
    for {
      jobs      <- LiveJobs[IO](xa)
      _         <- IO(println(s"Ready. Next...")) *> IO(StdIn.readLine)
      id        <- jobs.create("tom_cat@smartland.com", jobInfo)
      _         <- IO(println(s"Ready. Next...")) *> IO(StdIn.readLine)
      list      <- jobs.all()
      _         <- IO(println(s"All jobs: $list")) *> IO(StdIn.readLine)
      _         <- jobs.update(id, jobInfo.copy(title = "Software Master"))
      newJob    <- jobs.find(id)
      _         <- IO(println(s"New job: $newJob. Next...")) *> IO(StdIn.readLine)
      _         <- jobs.delete(id)
      listAfter <- jobs.all()
      _         <- IO(println(s"Deleted job. List now: $listAfter")) *> IO(StdIn.readLine)
    } yield ()

  }
}
