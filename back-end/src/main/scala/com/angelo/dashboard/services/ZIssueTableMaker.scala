package com.angelo.dashboard.services

import com.angelo.dashboard.client.ZDbClient
import com.angelo.dashboard.client.ZDbClient.ZDbClient
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

  val live: ZLayer[ZDbClient with Blocking with ZConfig, Throwable, ZIssueTableMaker] =
    ZLayer.fromServicesM[ZDbClient.Service, Blocking.Service, ZConfig, Throwable, Service] { (client, blocking) =>
      getDbConfig.map { cfg =>
        import cfg._
        import blocking._

        new Service {
          override def makeTable: Task[Unit] =
            effectBlocking(
              client.createTable(createTableRequest(issueTable, initialReadCapacity, initialWriteCapacity))
            ).unit mapError errorHandler(issueTable)
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
