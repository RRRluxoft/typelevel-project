package com.smartland.jobsboard.domain

object pagination {
  final case class Pagination(limit: Int, offset: Int)

  object Pagination {
    private val defaultPageSize = 20
    private val defaultOffset = 0

    def apply(maybeLimit: Option[Int], maybeOffset: Option[Int]) =
      new Pagination(maybeLimit.getOrElse(defaultPageSize), maybeOffset.getOrElse(defaultOffset))
  }

}
