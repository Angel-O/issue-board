package com.angelo.dashboard

import java.net.URI

import com.angelo.dashboard.ZConfig.ZConfig
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import zio._
import zio.blocking.Blocking

object ZDbClient {

  type ZDbClient = Has[ZDbClient.Service]

  type DbBlockingCall[A] = DynamoDbClient => A

  trait Service {
    def useClient[A](dbCall: DbBlockingCall[A]): Task[A]
  }

  // closes client after each call
  val liveTransient: URLayer[ZConfig with Blocking, ZDbClient] =
    ZLayer.fromServices[ZConfig.Service, Blocking.Service, ZDbClient.Service] {
      case (config, blocking) =>
        new Service {

          def useClient[A](dbCall: DbBlockingCall[A]): Task[A] =
            asManaged(makeClient)
              .use(client => blocking.effectBlocking(dbCall(client)))

          private val makeClient: Task[DynamoDbClient] = ZIO.effect(
            DynamoDbClient.builder
              .region(Region.EU_WEST_1)
              .endpointOverride(URI.create(config.dynamoDb.endpoint))
              .build()
          )

          private def asManaged(clientBuilder: Task[DynamoDbClient]): TaskManaged[DynamoDbClient] =
            Managed.make(clientBuilder) { client =>
              ZIO.effect(client.close()).ignore *> info("closing db client connection")
            }
        }
    }

  // keeps client open until effect is terminated
  val live: RLayer[ZConfig with Blocking, ZDbClient] =
    ZLayer.fromServicesManaged[ZConfig.Service, Blocking.Service, Any, Throwable, ZDbClient.Service] {
      (config, blocking) =>
        ZManaged.make {
          ZIO
            .effect(
              DynamoDbClient.builder
                .region(Region.EU_WEST_1)
                .endpointOverride(URI.create(config.dynamoDb.endpoint))
                .build()
            )
        }(client => ZIO.effect(client.close()).ignore *> info("closing db client connection")).map { client =>
          new Service {
            def useClient[A](dbCall: DbBlockingCall[A]): Task[A] =
              blocking.effectBlocking(dbCall(client))
          }
        }
    }

  /** alternative live implementation (managed resource isolated) */
  private val managedClient: URLayer[ZConfig, Has[DynamoDbClient]] =
    ZLayer.fromServiceManaged[ZConfig.Service, Any, Nothing, DynamoDbClient] { config =>
      ZManaged.make(
        ZIO
          .effect(
            DynamoDbClient.builder
              .region(Region.EU_WEST_1)
              .endpointOverride(URI.create(config.dynamoDb.endpoint))
              .build()
          )
          .orDie
      )(client => info("closing db client connection") *> ZIO.effect(client.close()).ignore)
    }

  val live2: URLayer[Blocking with ZConfig, ZDbClient] =
    ZLayer.requires[Blocking] ++ managedClient >>> ZLayer
      .fromServices[DynamoDbClient, Blocking.Service, ZDbClient.Service] { (db, blocking) =>
        new Service {
          def useClient[A](dbCall: DbBlockingCall[A]): Task[A] =
            blocking.effectBlocking(dbCall(db))
        }
      }
}
