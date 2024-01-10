package com.smartland.jobsboard.config

import cats.{MonadError, MonadThrow}
import cats.syntax.flatMap.*
import cats.syntax.functor.*
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.error.ConfigReaderException

import scala.reflect.ClassTag

object syntax {
  extension (source: ConfigSource)
    def loadF[F[_], A](using reader: ConfigReader[A], F: MonadThrow[F], tag: ClassTag[A]): F[A] =
      F.pure(source.load[A]).flatMap {
        case Left(err) => F.raiseError[A](ConfigReaderException(err)) //  No ClassTag available for A
        case Right(a) => F.pure(a)
      }
}
