package com.smartland.jobsboard.config

import pureconfig.ConfigReader
import pureconfig.error.CannotConvert
import pureconfig.generic.derivation.default.*

final case class AppConfig(
    postgresConfig: PostgresConfig,
    emberConfig: EmberConfig
) derives ConfigReader
