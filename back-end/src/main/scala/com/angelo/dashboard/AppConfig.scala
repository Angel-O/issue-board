package com.angelo.dashboard

import pureconfig._
import pureconfig.generic.auto._
import zio.{Has, ULayer, URIO, ZIO}

final case class AppConfig(
  serverConfig: ServerConfig,
  dynamoDb: DynamoDbConfig,
  slackConfig: SlackConfig
)

final case class SlackConfig(endpoint: String, minimumActiveIssues: Int, devMode: Boolean)

final case class DynamoDbConfig(issueTable: String, endpoint: String)

final case class ServerConfig(
  host: String,
  port: Int,
)

object AppConfig {

  private lazy val config: AppConfig =
    ConfigSource.default
      .load[AppConfig]
      .fold(err => throw new Exception(s"Error loading configuration: $err"), identity)

  def apply(): AppConfig = config
}
