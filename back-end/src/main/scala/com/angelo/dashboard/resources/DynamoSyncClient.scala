package com.angelo.dashboard.resources

import com.angelo.dashboard.config.ZConfig.{getDbConfig, DynamoDbConfig, ZConfig}
import com.angelo.dashboard.logging.ZLogger
import com.angelo.dashboard.logging.ZLogger.ZLogger
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import zio._
import zio.duration.Duration.fromScala

import java.net.URI

object DynamoSyncClient {

  type DynamoSyncClient = Has[Service]

  /** the output of this layer is not suspended to leverage the caching abilities of ZLayers */
  type Service = DynamoDbClient

  /**
   * this managed layer will close the connection opened by the client at the end of the application,
   * only once, no matter how many times it is injected into other layers as vertical dependency
   */
  val live: RLayer[ZLogger with ZConfig, DynamoSyncClient] =
    ZLayer.fromServiceManaged[ZLogger.Service, ZConfig, Throwable, Service] { logging =>
      import logging._

      Managed
        .fromAutoCloseable(getDbConfig >>> makeClient)
        .ensuring(info("db client shutting down"))
    }

  private val makeClient: RIO[DynamoDbConfig, DynamoDbClient] =
    ZIO.access { cfg =>
      DynamoDbClient.builder
        .region(Region.EU_WEST_1)
        .endpointOverride(URI.create(cfg.endpoint))
        .overrideConfiguration(clientConfiguration(cfg))
        .build
    }

  private def clientConfiguration(cfg: DynamoDbConfig) =
    ClientOverrideConfiguration.builder.apiCallTimeout(fromScala(cfg.clientTimeout)).build
}
