package com.smartland.jobsboard

import cats.*
import cats.effect.*
import cats.implicits.*
import com.smartland.jobsboard.config.EmberConfig
import com.smartland.jobsboard.config.syntax.*
import com.smartland.jobsboard.config.AppConfig
import com.smartland.jobsboard.http.routes.HealthRoutes
import com.smartland.jobsboard.modules.{Core, HttpApi}
import com.smartland.jobsboard.modules.Database
import doobie.hikari.HikariTransactor
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.*
import org.http4s.HttpRoutes
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigReader.Result
import pureconfig.ConfigSource

/*
  1 - add a plain health endpoint ti our app
  2 - add minimal configuration
  3 - basic http sever layout
 */

object Application extends IOApp.Simple {

  // GET 'localhost:8080/api/courses?instructor=Marcin%20Odersky&year=2024'
  // GET 'localhost:8080/api/courses/a456df8e-9d20-4cfa-8b47-d73eb46f0ed9/students'
  // GET 'localhost:8080/private/health'

  //  val configSource: Result[EmberConfig] = ConfigSource.default.load[EmberConfig]

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] = ConfigSource.default.loadF[IO, AppConfig].flatMap {
    case AppConfig(postgresConfig, emberConfig) =>
      val appResource = for {
        xa <- Database.makePostgresResource[IO](postgresConfig)
        core <- Core[IO](xa)
        httpApi <- HttpApi[IO](core)
        server <- EmberServerBuilder
          .default[IO]
          .withHost(emberConfig.host)
          .withPort(emberConfig.port)
          .withHttpApp(httpApi.endpoints.orNotFound)
          .build
      } yield server

      appResource.use(_ => IO.println("Server ready!") *> IO.never)
  }

}
