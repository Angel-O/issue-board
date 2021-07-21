package com.angelo.dashboard.config

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio.{Has, ULayer, URIO, ZIO}

import scala.concurrent.duration.Duration

object ZConfig {

  type ZConfig = Has[Service]

  type Service = AppConfig

  final case class AppConfig(
    serverConfig: ServerConfig,
    dynamoDb: DynamoDbConfig,
    slackConfig: SlackConfig,
    schedulerConfig: SchedulerConfig
  )

  final case class ServerConfig(
    host: String,
    port: Int,
    connectionIdleTimeout: Duration,
    responseTimeout: Duration,
    allowedOrigin: String
  )
  final case class DynamoDbConfig(issueTable: String, endpoint: String, clientTimeout: Duration)
  final case class SlackConfig(endpoint: String, token: String, minimumActiveIssues: Int, devMode: Boolean)
  final case class SchedulerConfig(initialDelay: Duration, loopInterval: Duration, backoff: BackoffConfig)
  final case class BackoffConfig(basePeriod: Duration, resetPeriod: Duration)

  val live: ULayer[ZConfig] =
    ZIO.effect(ConfigSource.url(getClass.getResource("/application.conf")).loadOrThrow[AppConfig]).toLayer.orDie

  //accessors
  private val service: URIO[ZConfig, Service]            = ZIO.service[Service]
  val getServerConfig: URIO[ZConfig, ServerConfig]       = service.map(_.serverConfig)
  val getDbConfig: URIO[ZConfig, DynamoDbConfig]         = service.map(_.dynamoDb)
  val getSlackConfig: URIO[ZConfig, SlackConfig]         = service.map(_.slackConfig)
  val getSchedulerConfig: URIO[ZConfig, SchedulerConfig] = service.map(_.schedulerConfig)
}
