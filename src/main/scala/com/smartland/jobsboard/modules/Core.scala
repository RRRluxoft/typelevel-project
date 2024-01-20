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

  def apply[F[_] : Async](xa: Transactor[F]): Resource[F, Core[F]] =
    Resource
      .eval(LiveJobs[F](xa))
      .map(jobs => new Core(jobs))

}
