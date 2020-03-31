package com.angelo.dashboard

import pureconfig._
import pureconfig.generic.auto._
import zio.{Has, TaskLayer, ULayer, URIO, ZIO}

object ZConfig {

  type ZConfig = Has[Service]
  type Service = AppConfig

  final case class AppConfig(
    serverConfig: ServerConfig,
    dynamoDb: DynamoDbConfig,
    slackConfig: SlackConfig
  )

  final case class ServerConfig(host: String, port: Int)
  final case class DynamoDbConfig(issueTable: String, endpoint: String)
  final case class SlackConfig(endpoint: String, minimumActiveIssues: Int, devMode: Boolean)

  val live: TaskLayer[ZConfig] = ZIO.effect(ConfigSource.default.loadOrThrow[AppConfig]).toLayer

  //accessors
  val getConfig: URIO[ZConfig, ZConfig.Service]    = ZIO.service[Service]
  val devModeEnabled: URIO[ZConfig, Boolean]       = getConfig.map(_.slackConfig.devMode)
  val getServerConfig: URIO[ZConfig, ServerConfig] = getConfig.map(_.serverConfig)
}
