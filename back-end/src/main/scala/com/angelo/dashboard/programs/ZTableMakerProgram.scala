package com.angelo.dashboard.programs

import com.angelo.dashboard.layers.ZAppLayers.TableMakerEnvironment
import com.angelo.dashboard.logging.ZLogger.ZLogger
import com.angelo.dashboard.services.ZIssueTableMaker
import com.angelo.dashboard.services.ZIssueTableMaker._
import zio.Schedule.Decision
import zio.Schedule.Decision.{Continue, Done}
import zio.duration.durationInt
import zio.logging.Logging.{error, info, warn}
import zio.{Cause, IO, Schedule, URIO, ZIO}

object ZTableMakerProgram {

  final case class TableCreationFail(cause: Throwable) extends Exception("could not create table", cause)

  val initTable: ZIO[TableMakerEnvironment, TableCreationFail, Unit] =
    ZIssueTableMaker.service
      .flatMap(_.makeTable)
      .retry(retryPolicy)
      .foldM(recoverIfTableExists, _ => ZIO.unit)

  private def retryPolicy: Schedule[ZLogger, Throwable, (Long, Throwable)] =
    (everySecondAtMost5times && untilTableIsCreated).onDecision(logDecision)

  private def untilTableIsCreated: Schedule[Any, Throwable, Throwable] =
    Schedule.recurUntil(tableExistsErrorOccurs)

  private def everySecondAtMost5times: Schedule[Any, Any, Long] =
    Schedule.recurs(5) <* Schedule.spaced(1 second)

  private def tableExistsErrorOccurs: Throwable => Boolean = {
    case TableAlreadyExists(_) => true
    case _                     => false
  }

  private def recoverIfTableExists: Throwable => IO[TableCreationFail, Unit] = {
    case TableAlreadyExists(_) => ZIO.unit
    case err                   => ZIO.fail(err).mapError(TableCreationFail)
  }

  private def logDecision[R, In]: Decision[R, In, (Long, Throwable)] => URIO[ZLogger, Unit] = {
    case Done((_, TableAlreadyExists(tableName))) => info(s"table $tableName already exists, nothing to do")
    case Continue((n, _), _, _)                   => warn(s"error while creating table, attempted ${n + 1} time(s), retrying...")
    case Done((n, err))                           => error(s"failed to create table after $n attempts: ${err.getMessage}", Cause.fail(err))
  }
}
