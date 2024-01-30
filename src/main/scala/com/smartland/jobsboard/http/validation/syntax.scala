package com.smartland.jobsboard.http.validation

import cats.*
import cats.data.*
import cats.data.Validated.*
import cats.implicits.*
import com.smartland.jobsboard.http.responses.*
import com.smartland.jobsboard.http.validation.validators.*
import com.smartland.jobsboard.logging.syntax.*
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.typelevel.log4cats.Logger

object syntax {

  def validateEntity[A](entity: A)(using validator: Validator[A]): ValidationResult[A] =
    validator.validate(entity)

  trait HttpValidationDsl[F[_]: MonadThrow: Logger] extends Http4sDsl[F] {
    extension (req: Request[F]) {
      def validate[A: Validator](serverLogicIfValid: A => F[Response[F]])(using EntityDecoder[F, A]): F[Response[F]] =
        req
          .as[A]
          .logError(err => s"Parsing payload failed $err")
          .map(e => validateEntity(e))
          .flatMap {
            case Valid(entity) =>
              serverLogicIfValid(entity)
            case Invalid(err) =>
              BadRequest(FailureResponse(err.toList.map(_.errorMessage).mkString(", ")))
          }
    }
  }

}
