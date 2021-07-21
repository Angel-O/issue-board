package com.angelo.dashboard.client

import com.angelo.dashboard.config.ZConfig.{getDbConfig, DynamoDbConfig, ZConfig}
import com.angelo.dashboard.logging.ZLogger
import com.angelo.dashboard.logging.ZLogger.ZLogger
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import zio._
import zio.duration.Duration.fromScala

import java.net.URI

object ZDbClient {

  type ZDbClient = Has[Service]

  type Service = DynamoDbClient

  /**
   * this managed layer will close the connection opened by the client at the end of the application,
   * only once, no matter how many times it is injected into other layers as vertical dependency
   */
  val live: RLayer[ZLogger with ZConfig, ZDbClient] =
    ZLayer.fromServiceManaged[ZLogger.Service, ZConfig, Throwable, Service] { logging =>
      import logging._

      def acquireClient(cfg: DynamoDbConfig): Task[DynamoDbClient] =
        ZIO.effect(
          DynamoDbClient.builder
            .region(Region.EU_WEST_1)
            .endpointOverride(URI.create(cfg.endpoint))
            .overrideConfiguration(clientConfiguration(cfg))
            .build
        )

      def releaseClient(client: DynamoDbClient): UIO[Unit] =
        ZIO.effect(client.close()).ignore <* info("closing db client connection")

      def clientConfiguration(cfg: DynamoDbConfig) =
        ClientOverrideConfiguration.builder.apiCallTimeout(fromScala(cfg.clientTimeout)).build

      getDbConfig.toManaged_ flatMap (cfg => Managed.make(acquireClient(cfg))(releaseClient))
    }
}
