package com.angelo.dashboard.client

import com.angelo.dashboard.config.ZConfig.{getDbConfig, ZConfig}
import com.angelo.dashboard.dao.AvBuilder
import com.angelo.dashboard.dao.DynamoDbRequests._
import com.angelo.dashboard.dao.IssueSchemaReaderWriter.{asAttemptedIssue, asOptionalIssue}
import com.angelo.dashboard.logging.ZLogger.ZLogger
import com.angelo.dashboard.resources.DynamoSyncClient
import com.angelo.dashboard.{=?>, Issue}
import software.amazon.awssdk.services.dynamodb.model.{ConditionalCheckFailedException, ResourceInUseException}
import zio._
import zio.blocking.Blocking

import scala.jdk.CollectionConverters.{IterableHasAsScala, MapHasAsScala}
import scala.util.control.NoStackTrace

object ZDbClient {

  type ZDbClient = Has[Service]

  trait Service {
    def createTable: Task[Unit]
    def createIssue(issue: Issue): Task[Unit]
    def getIssue(id: String): Task[Option[Issue]]
    def countIssues[V: AvBuilder](fieldName: String, value: V): Task[Int]
    def updateIssue(issue: Issue): Task[Unit]
    def deleteIssue(id: String): Task[Boolean]
    def getIssues: Task[Seq[Either[Throwable, Issue]]]
  }

  case object IdNotFound                           extends NoStackTrace
  case class TableAlreadyExists(tableName: String) extends NoStackTrace

  /** partial layer composition */
  val live: RLayer[ZConfig with Blocking with ZLogger, ZDbClient] =
    (ZLayer.identity[ZConfig] ++ ZLayer.identity[Blocking] ++ DynamoSyncClient.live) >>>
      ZLayer
        .fromServicesM[DynamoSyncClient.Service, Blocking.Service, ZConfig, Throwable, Service] { (client, blocking) =>
          import blocking._

          getDbConfig map { cfg =>
            import cfg._

            new Service {
              override def createTable: Task[Unit] =
                effectBlocking(client.createTable(createTableRequest(issueTable, readCapacity, writeCapacity))).unit
                  .catchSome(createTableErrorHandler(issueTable))

              override def createIssue(issue: Issue): Task[Unit] =
                effectBlocking(client.putItem(putItemRequest(issueTable)(issue)))

              override def getIssue(id: String): Task[Option[Issue]] =
                effectBlocking(client.getItem(getItemRequest(issueTable)(id)).item.asScala.toMap)
                  .map(asOptionalIssue(_))

              override def countIssues[V: AvBuilder](fieldName: String, value: V): Task[Int] =
                effectBlocking(client.scan(filterRequest(issueTable)(fieldName, value)).count.toInt)

              override def updateIssue(issue: Issue): Task[Unit] =
                effectBlocking(client.updateItem(archiveItemRequest(issueTable)(issue))).unit
                  .catchSome(updateErrorHandler)

              override def deleteIssue(id: String): Task[Boolean] =
                effectBlocking(client.deleteItem(deleteItemRequest(issueTable)(id)).attributes.asScala.nonEmpty)

              override def getIssues: Task[Seq[Either[Throwable, Issue]]] =
                effectBlocking(client.scanPaginator(scanRequest(issueTable)).items.asScala.toSeq.map(_.asScala.toMap))
                  .map(_.map(asAttemptedIssue(_)))
            }
          }
        }

  private val updateErrorHandler: Throwable =?> Task[Nothing] = {
    case _: ConditionalCheckFailedException => ZIO.fail(IdNotFound)
  }

  private val createTableErrorHandler: String => Throwable =?> Task[Nothing] = { tableName =>
    { case _: ResourceInUseException => ZIO.fail(TableAlreadyExists(tableName)) }
  }
}
