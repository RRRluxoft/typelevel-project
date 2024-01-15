package com.smartland.jobsboard.modules

import cats.*
import cats.effect.*
import cats.implicits.*
import com.smartland.jobsboard.core.{Jobs, LiveJobs}
import doobie.*
import doobie.implicits.*
import doobie.hikari.HikariTransactor
import doobie.util.*
import doobie.util.transactor.Transactor

final class Core[F[_]] private (val jobs: Jobs[F])

object Core {
  def postgresResource[F[_]: Async]: Resource[F, HikariTransactor[F]] = for {
    ec <- ExecutionContexts.fixedThreadPool[F](32)
    xa <- HikariTransactor.newHikariTransactor[F](
            "org.postgresql.Driver",
            "jdbc:postgresql:board", // TODO: move to config
            "docker",
            "docker",
            ec
          )
  } yield xa

  def apply[F[_]: Async]: Resource[F, Core[F]] =
    postgresResource[F]
      .evalMap(postgres => LiveJobs[F](postgres))
      .map(jobs => new Core(jobs))

}
