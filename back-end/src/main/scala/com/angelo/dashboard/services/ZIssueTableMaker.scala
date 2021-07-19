package com.angelo.dashboard.services

import com.angelo.dashboard.client.ZDbClientProvider
import com.angelo.dashboard.client.ZDbClientProvider.ZDbClientProvider
import com.angelo.dashboard.config.ZConfig.{getDbConfig, ZConfig}
import com.angelo.dashboard.dao.DynamoDbHelpers.createTableRequest
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException
import zio.blocking.Blocking
import zio.{Has, Task, URIO, ZIO, ZLayer}

import scala.util.control.NoStackTrace

object ZIssueTableMaker {

  type ZIssueTableMaker = Has[Service]

  trait Service {
    def makeTable: Task[Unit]
  }

  case class TableAlreadyExists(tableName: String) extends NoStackTrace

  val live: ZLayer[ZDbClientProvider with Blocking with ZConfig, Throwable, ZIssueTableMaker] =
    ZLayer.fromServicesManaged[ZDbClientProvider.Service, Blocking.Service, ZConfig, Throwable, Service] {
      (clientProvider, blocking) =>
        clientProvider.asResource.zipWith(getDbConfig.toManaged_) { (client, cfg) =>
          import blocking._
          import cfg._

          new Service {
            override def makeTable: Task[Unit] =
              effectBlocking(client.createTable(createTableRequest(issueTable))).unit mapError errorHandler(issueTable)
          }
        }

    }

  private def errorHandler(tableName: String): Throwable => Throwable = {
    case _: ResourceInUseException => TableAlreadyExists(tableName)
    case err                       => err
  }

  // accessor
  val service: URIO[ZIssueTableMaker, Service] = ZIO.service[Service]
}
