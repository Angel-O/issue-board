package com.angelo.dashboard.dao

import com.angelo.dashboard._
import com.angelo.dashboard.client.ZDbClient
import com.angelo.dashboard.client.ZDbClient.ZDbClient
import com.angelo.dashboard.config.ZConfig.{getDbConfig, ZConfig}
import com.angelo.dashboard.dao.DynamoDbHelpers._
import com.angelo.dashboard.logging.ZLogger
import com.angelo.dashboard.logging.ZLogger.ZLogger
import software.amazon.awssdk.services.dynamodb.model._
import zio._
import zio.blocking.Blocking

import scala.jdk.CollectionConverters._
import scala.util.control.NoStackTrace

object ZIssueRepo {

  type ZIssueRepo = Has[Service]

  trait Service {
    def putIssue(item: Issue): Task[String]
    def getIssue(issueId: String): Task[Issue]
    def countActiveIssues: Task[Int]
    def retrieveIssues: Task[Seq[Issue]]
    def archiveIssue(issue: Issue): Task[String]
    def deleteIssue(issueId: String): Task[Unit]
  }

  case object IssueNotFound extends NoStackTrace

  //TODO ifA not working properly
  val live: RLayer[ZDbClient with Blocking with ZLogger with ZConfig, ZIssueRepo] =
    ZLayer
      .fromServicesM[
        ZDbClient.Service,
        Blocking.Service,
        ZLogger.Service,
        ZConfig,
        Throwable,
        Service
      ] { (client, blocking, logging) =>
        getDbConfig.map { cfg =>
          import blocking._
          import cfg._
          import logging._

          new Service {

            override def putIssue(item: Issue): Task[String] =
              effectBlocking(client.putItem(putItemRequest(issueTable)(item)))
                .as(item.id)

            override def getIssue(issueId: String): Task[Issue] =
              effectBlocking(client.getItem(getItemRequest(issueTable)(issueId)).item.asScala.toMap)
                .map(record => asOptionalIssue(record)) >>= (ZIO.getOrFailWith(IssueNotFound)(_))

            override def countActiveIssues: Task[Int] =
              effectBlocking(client.scan(findActiveRequest(issueTable)).count.toInt)

            override def archiveIssue(item: Issue): Task[String] =
              effectBlocking(client.updateItem(archiveItemRequest(issueTable)(item)))
                .bimap(archiveErrorHandler, _ => item.id)

            override def deleteIssue(issueId: String): Task[Unit] =
              effectBlocking(
                client.deleteItem(deleteItemRequest(issueTable)(issueId)).attributes.asScala.nonEmpty
              ) >>= (ZIO.cond(_, (), IssueNotFound))

            /**
             * Note: the entire operation needs to be enclosed inside [[effectBlocking]] in order to handle errors
             * gracefully. If the second part {{{items.asScala.toSeq.map(_.asScala.toMap))}}} is executed inside a
             * `map` or `flatMap` call it will result in an unhandled error. Seems to be a limitation of the
             * [[client.scanPaginator]] api, which performs unsafe operations even if the sdk response is not
             * successful.
             * In order to reproduce the crash, change the code as explained above, run the project, stop the database
             * and then call the endpoint to get all issues. There will be an unhandled error in the logs.
             * Alternatives could be: using [[client.scan]] instead, using the async client, using a 3rd party library,
             * keeping it as it is now avoiding unsafe operations inside `map`/`flatMap`, or `flatMapping` over the
             * first part of the computation suspending the second part (the unsafe one) allowing the IO to safely
             * handle any error
             */
            override def retrieveIssues: Task[Seq[Issue]] =
              effectBlocking(client.scanPaginator(scanAllRequest(issueTable)).items.asScala.toSeq.map(_.asScala.toMap))
                .flatMap(collectLoggingFailures)

            private def collectLoggingFailures(dbRecords: Seq[Map[String, AttributeValue]]): UIO[Seq[Issue]] =
              ZIO
                .foreach(dbRecords)(record => UIO(asAttemptedIssue(record)).tap(logOnlyFailures(record)))
                .map(_.collect { case Right(issue) => issue })

            private def logOnlyFailures[A](dbFields: Map[String, A]): Either[Throwable, Issue] => UIO[Unit]  =
              ZIO.whenCase(_) { case Left(err) => ZIO.whenCase(err)(logSomeErrors(dbFields)) }

            private def logSomeErrors[A](record: Map[String, A]): Throwable =?> UIO[Unit]                    = {
              case err @ FieldNotFound(_) => warn(err.getMessage)
              case err                    => warn(s"unable to deserialize issue. Fields: $record. Error: ${err.getMessage}")
            }
          }
        }
      }

  private val archiveErrorHandler: Throwable => Throwable = {
    case _: ConditionalCheckFailedException => IssueNotFound
    case err                                => err
  }
}
