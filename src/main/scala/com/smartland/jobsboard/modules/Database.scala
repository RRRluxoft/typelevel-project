package com.smartland.jobsboard.modules

import cats.effect.Async
import cats.effect.Resource
import com.smartland.jobsboard.config.*
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object Database {

  def makePostgresResource[F[_]: Async](
      config: PostgresConfig
  ): Resource[F, HikariTransactor[F]] = for {
    ec <- ExecutionContexts.fixedThreadPool[F](config.nThreads)
    xa <- HikariTransactor.newHikariTransactor[F](
      "org.postgresql.Driver",
      config.url,  // "jdbc:postgresql:board", // TODO: move to config
      config.user, // "docker",
      config.pass, // "docker",
      ec
    )
  } yield xa

}
