package com.smartland.foundations

import cats.effect.{IO, IOApp}
import cats.effect.kernel.MonadCancelThrow
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import doobie.util.transactor.Transactor
import doobie.util.ExecutionContexts

object Doobie extends IOApp.Simple {

  case class Student(id: Int, name: String)

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // JDBC connector
    "jdbc:postgresql:demo",  // db url
    "docker",                // user
    "docker"                 // pwd
  )

  def findAllStudentNames: IO[List[String]] = {
    val query = sql"select name from students".query[String]
    val action = query.to[List]
    action.transact(xa)
  }

  def saveStudent(id: Int, name: String): IO[Int] = {
    val query = sql"insert into students(id, name) values ($id, $name)"
    query.update
      .run
      .transact(xa)
  }
  def saveStudent_V2(name: String): IO[Int] = {
    val query = sql"insert into students(name) values ($name)"
    query.update
      .withUniqueGeneratedKeys[Int]("id")
//      .run
      .transact(xa)
  }

  def findStudentsByInitial(letter: String): IO[List[Student]] = {
    val selectPart = fr"select id, name"
    val fromPart = fr"from students"
    val wherePart = fr"where left(name, 1) = $letter"

    val query = selectPart ++ fromPart ++ wherePart
    query.query[Student].to[List]
      .transact(xa)
  }

  trait Students[F[_]] { // repository
    def findById(id: Int): F[Option[Student]]
    def findAll: F[List[Student]]
    def create(name: String): F[Int]
  }

  object Students {
    def make[F[_]: MonadCancelThrow](xa: Transactor[F]): Students[F] = new Students[F] {
      override def findById(id: Int): F[Option[Student]] =
        sql"select id, name from students where id=$id".query[Student]
          .option // to[Option] instead
          .transact(xa)

      override def findAll: F[List[Student]] =
        sql"select * from students".query[Student]
          .to[List]
          .transact(xa)

      override def create(name: String): F[Int] =
        sql"insert into students(name) values ($name)"
          .update
          .withUniqueGeneratedKeys[Int]("id")
//          .run
          .transact(xa)
    }
  }

  val postgresResource = for {
    ec <- ExecutionContexts.fixedThreadPool[IO](4)
    xa <- HikariTransactor.newHikariTransactor[IO](
            "org.postgresql.Driver", // JDBC connector
            "jdbc:postgresql:demo",  // db url
            "docker",                // user
            "docker",                // pwd
            ec
          )
  } yield xa

  val smallProgram = postgresResource.use { xa =>
    val studentsRepo = Students.make[IO](xa)
    for {
      id     <- studentsRepo.create("tom waits")
      std <- studentsRepo.findById(id)
      _      <- IO.println(s"The next student of Smart Land is $std")
    } yield ()
  }

  override def run: IO[Unit] =
//    findAllStudentNames.map(println)
//    saveStudent(7, "kitty cat").map(i => println(s"inserted $i"))
//    saveStudent_V2("kitty cat_2").map(i => println(s"inserted with id=$i"))
//    findStudentsByInitial("k").map(println)
    smallProgram
}
