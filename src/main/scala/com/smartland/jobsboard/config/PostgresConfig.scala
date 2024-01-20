package com.smartland.jobsboard.config

import pureconfig.ConfigReader
import pureconfig.error.CannotConvert
import pureconfig.generic.derivation.default.*

case class PostgresConfig(nThreads: Int, url: String, user: String, pass: String)
    derives ConfigReader

object PostgresConfig {}
