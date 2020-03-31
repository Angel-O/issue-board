package com.angelo.dashboard

import profig._
import EnvVars._

object AppConfig {

  case class AppConfig(backEnd: BackEndConfig, ui: UiConfig, logging: LoggingConfig)
  case class BackEndConfig(basePath: String, path: String)
  case class UiConfig(rootPath: String)
  case class LoggingConfig(enableLogging: Boolean)

  Profig.loadDefaults()

  val uiConfig: UiConfig = {
    val config = Profig("ui").as[UiConfig]
    config.copy(rootPath = getOrDefault(process.env.ROOT_PATH, config.rootPath))
  }

  val backEndConfig: BackEndConfig = {
    val config = Profig("backEnd").as[BackEndConfig]
    config.copy(basePath = getOrDefault(process.env.BACKEND_BASE_PATH, config.basePath))
  }

  val loggingConfig: LoggingConfig = {
    val config = Profig("logging").as[LoggingConfig]
    config.copy(enableLogging = getOrDefault(process.env.ENABLE_LOGGING, config.enableLogging))
  }

  lazy val config: AppConfig = AppConfig(backEndConfig, uiConfig, loggingConfig)

  def apply(): AppConfig = config
}
