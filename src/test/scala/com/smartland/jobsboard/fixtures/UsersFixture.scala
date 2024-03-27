package com.smartland.jobsboard.fixtures

import cats.implicits.*
import com.smartland.jobsboard.domain.user.Role
import com.smartland.jobsboard.domain.user.User

trait UsersFixture {

  val Tom = User(
    email = "tom@smartland.com",
    hashedPassword = "rockthejvm",
    firstName = "Tom".some,
    lastName = "Cat".some,
    company = "Smart.Land".some,
    role = Role.ADMIN
  )

  val Rick = User(
    "rick@smartland.com",
    "rickthere",
    "Rick".some,
    "Sanchos".some,
    "Smart.Land".some,
    Role.RECRUITER
  )

}
