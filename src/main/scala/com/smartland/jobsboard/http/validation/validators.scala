package com.smartland.jobsboard.http.validation

import cats.*
import cats.data.*
import cats.data.Validated.*
import cats.implicits.*
import com.smartland.jobsboard.domain.job.*

import java.net.URL
import scala.util.Failure
import scala.util.Success
import scala.util.Try

object validators {

  sealed trait ValidationFailure(val errorMessage: String)
  case class EmptyField(fieldName: String) extends ValidationFailure(s"'$fieldName' is empty")
  case class InvalidUrl(fieldName: String) extends ValidationFailure(s"'$fieldName' is invalid URL")
  case object JobInfoTitleEmpty extends ValidationFailure(s"title is empty")

  type ValidationResult[A] = ValidatedNel[ValidationFailure, A]
  trait Validator[A] {
    def validate(value: A): ValidationResult[A] // implementation by given
  }

  def validateRequired[A](field: A, fieldName: String)(required: A => Boolean): ValidationResult[A] =
    if required(field) then field.validNel else EmptyField(fieldName).invalidNel

  def validateUrl(field: String, fieldName: String): ValidationResult[String] =
//    if checking then field.validNel else EmptyField(fieldName).invalidNel
    Try(URL(field).toURI) match
      case Success(_)  => field.validNel
      case Failure(ex) => InvalidUrl(fieldName).invalidNel[String]

  given jobInfoValidator: Validator[JobInfo] = (jobInfo: JobInfo) => {
    val JobInfo(
      company,  // shouldnt be empty§
      title,  // shouldnt be empty
      description,  // shouldnt be empty
      externalUrl,  // should be a valid URL
      remote,
      location,  // shouldnt be empty
      salaryLo,
      salaryHi,
      currency,
      country,
      tags,
      image,
      seniority,
      other
    ) = jobInfo

    val validCompany: ValidationResult[String] = validateRequired(company, "company")(_.nonEmpty)
    val validTitle = validateRequired(title, "title")(_.nonEmpty)
    val validDescription = validateRequired(description, "description")(_.nonEmpty)
    val validExternalUrl: ValidationResult[String] = validateUrl(externalUrl, "externalUrl")
    val validLocation = validateRequired(location, "location")(_.nonEmpty)

    val result:ValidatedNel[ValidationFailure, JobInfo] =
      (
        validCompany, // company
        validTitle, // title
        validDescription, // description
        validExternalUrl, // externalUrl
        remote.validNel, // remote
        validLocation, // location
        salaryLo.validNel, // salaryLo
        salaryHi.validNel, // salaryHi
        currency.validNel, // currency
        country.validNel, // country
        tags.validNel, // tags
        image.validNel, // image
        seniority.validNel, // seniority
        other.validNel // other
      )
        .mapN(JobInfo.apply) // ValidatedNel[ValidationFailure, JobInfo]

    result
  }

}

