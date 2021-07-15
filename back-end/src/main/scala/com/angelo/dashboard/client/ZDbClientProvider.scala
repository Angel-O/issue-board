package com.angelo.dashboard.client

import com.angelo.dashboard.config.ZConfig.{getDbConfig, ZConfig}
import com.angelo.dashboard.logging.Logs
import com.angelo.dashboard.logging.Logs.Logs
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import zio._
import zio.duration.Duration.fromScala

import java.net.URI

object ZDbClientProvider {

  type ZDbClientProvider = Has[ZDbClientProvider.Service]

  trait Service {
    def asResource: TaskManaged[DynamoDbClient]
  }

  val live: RLayer[Logs with ZConfig, ZDbClientProvider] =
    ZLayer.fromServiceM[Logs.Service, ZConfig, Throwable, Service] { logging =>
      import logging._

      getDbConfig.map { cfg =>
        new Service {

          override def asResource: TaskManaged[DynamoDbClient] =
            Managed.make(acquireClient)(releaseClient)

          private val acquireClient: Task[DynamoDbClient] = ZIO.effect(
            DynamoDbClient.builder
              .region(Region.EU_WEST_1)
              .endpointOverride(URI.create(cfg.endpoint))
              .overrideConfiguration(clientConfiguration)
              .build
          )

          private def releaseClient(client: DynamoDbClient): UIO[Unit] =
            ZIO.effect(client.close()).ignore *> info("closing db client connection")

          private def clientConfiguration =
            ClientOverrideConfiguration.builder.apiCallTimeout(fromScala(cfg.clientTimeout)).build
        }
      }
    }
}
