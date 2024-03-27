package com.smartland.jobsboard.core

import cats.*
import cats.implicits.*
import cats.effect.*
import com.smartland.jobsboard.domain.user.User
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.*
import doobie.util.fragment.*
import org.typelevel.log4cats.Logger

trait Users[F[_]] {
  // CRUD
  def find(email: String): F[Option[User]]
  def create(user: User): F[String]
  def update(user: User): F[Option[User]]
  def delete(user: User): F[Boolean]
}

final class LiveUsers[F[_]] private (xa: Transactor[F]) extends Users[F] {
  override def find(email: String): F[Option[User]] = ???
  override def create(user: User): F[String] = ???
  override def update(user: User): F[Option[User]] = ???
  override def delete(user: User): F[Boolean] = ???
}

object LiveUsers {
  def apply[F[_]: MonadCancelThrow: Logger](xa: Transactor[F]): F[LiveUsers[F]] =
    new LiveUsers[F](xa).pure[F]
}
