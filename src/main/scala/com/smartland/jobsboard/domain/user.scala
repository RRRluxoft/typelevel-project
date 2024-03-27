package com.smartland.jobsboard.domain

object user {

  final case class User(
    email: String,
    hashedPassword: String,
    firstName: Option[String],
    lastName: Option[String],
    company: Option[String],
    role: Role
  )

  enum Role {
    case ADMIN, RECRUITER
  }
}
