package com.angelo.dashboard.dao

import com.angelo.dashboard._
import com.angelo.dashboard.client.ZDbClient
import com.angelo.dashboard.client.ZDbClient.{IdNotFound, ZDbClient}
import com.angelo.dashboard.logging.ZLogger
import com.angelo.dashboard.logging.ZLogger.ZLogger
import zio._

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

  val live: RLayer[ZDbClient with ZLogger, ZIssueRepo] =
    ZLayer
      .fromServices[ZDbClient.Service, ZLogger.Service, Service] { (client, logging) =>
        import logging._

        new Service {

          override def putIssue(item: Issue): Task[String] =
            client.createIssue(item) as item.id

          override def getIssue(issueId: String): Task[Issue] =
            client.getIssue(issueId) >>= (ZIO.getOrFailWith(IssueNotFound)(_))

          override def countActiveIssues: Task[Int] =
            client.countIssues(IssueSchemaReaderWriter.IS_ARCHIVED, false)

          override def archiveIssue(item: Issue): Task[String] =
            client.updateIssue(item).bimap(errorMapper.applyOrElse(_, identity[Throwable]), _ => item.id)

          override def deleteIssue(issueId: String): Task[Unit] =
            client.deleteIssue(issueId) >>= (ZIO.cond(_, (), IssueNotFound))

          override def retrieveIssues: Task[Seq[Issue]] =
            client.getIssues >>= collectLoggingFailures

          private def collectLoggingFailures(attempts: Seq[Either[Throwable, Issue]]): UIO[Seq[Issue]] =
            ZIO.foreach(attempts)(UIO(_).tap(logOnlyFailures)).map(_.collect { case Right(issue) => issue })

          private def logOnlyFailures(attempt: Either[Throwable, Issue]): UIO[Unit]                    =
            ZIO.whenCase(attempt) { case Left(err) => warn(s"Error while retrieving issue: ${err.getMessage}") }
        }
      }

  private val errorMapper: Throwable =?> Throwable = {
    case IdNotFound => IssueNotFound
  }
}
