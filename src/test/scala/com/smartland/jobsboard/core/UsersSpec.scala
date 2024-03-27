package com.smartland.jobsboard.core

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import com.smartland.jobsboard.fixtures.*
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class UsersSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with DoobieSpec with UsersFixture {
  val initScript: String = "sql/users.sql"
  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  "Users 'algebra' " - {
    "first test" in {
      transactor.use { xa =>
        val program = for {
          users <- LiveUsers[IO](xa)
        } yield ()
        
        program asserting(_ shouldBe ())
        
      }
    }
  }
}
