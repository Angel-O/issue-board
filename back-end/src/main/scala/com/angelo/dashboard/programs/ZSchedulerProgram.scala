package com.angelo.dashboard.programs

import com.angelo.dashboard.=?>
import com.angelo.dashboard.config.ZConfig.{getSchedulerConfig, SchedulerConfig}
import com.angelo.dashboard.layers.ZAppLayers.SchedulerEnvironment
import com.angelo.dashboard.logging.Logs.Logs
import com.angelo.dashboard.services.ZNotifier
import com.angelo.dashboard.services.ZNotifier._
import zio.clock.Clock
import zio.duration.Duration
import zio.duration.Duration.fromScala
import zio.logging.Logging.{error, info, warn}
import zio.random.Random
import zio.{Cause, IO, Schedule, URIO, ZIO}

object ZSchedulerProgram {

  val scheduleSlackNotifications: ZIO[SchedulerEnvironment, NotifierError, Unit] =
    ZNotifier.service
      .map(_.sendMessageToSlack)
      .zipWith(getSchedulerConfig)(runWithSchedule)
      .flatten

  private def runWithSchedule(
    task: IO[NotifierError, Unit],
    cfg: SchedulerConfig
  ): ZIO[Logs with Random with Clock, NotifierError, Unit] =
    task
      .retry(retryPolicy(cfg))
      .repeat(repeatStrategy(cfg))
      .delay(fromScala(cfg.initialDelay))
      .unit

  private def retryPolicy(cfg: SchedulerConfig): Schedule[Logs with Random, NotifierError, Duration] =
    ((Schedule recurWhile policySatisfied) tapInput logRecoverableError) *> backoff(cfg) <* counterLogger(logAttempt)

  private def repeatStrategy(cfg: SchedulerConfig): Schedule[Logs, Unit, Long] =
    (Schedule spaced fromScala(cfg.loopInterval)) <* counterLogger(logSuccess)

  private def backoff(cfg: SchedulerConfig): Schedule[Random, NotifierError, Duration] =
    (Schedule exponential fromScala(cfg.backoff.basePeriod)).jittered resetAfter fromScala(cfg.backoff.resetPeriod)

  private def logRecoverableError(notifierError: NotifierError): URIO[Logs, Unit] =
    (ZIO whenCase notifierError)(logSomeErrors)

  private def counterLogger(logger: Long => URIO[Logs, Unit]): Schedule[Logs, Any, Long] =
    Schedule.count.map(_ + 1) tapOutput logger

  private def logAttempt(nthAttempt: Long): URIO[Logs, Unit] =
    warn(s"notification attempt failure. Attempted #$nthAttempt time(s)")

  private def logSuccess(nthNotification: Long): URIO[Logs, Unit] =
    info(s"notification #$nthNotification successfully sent")

  private val policySatisfied: NotifierError => Boolean = {
    case InvalidUri(_)                            => false
    case SlackUnreacheable(_) | RepositoryFail(_) => true
  }

  private val logSomeErrors: NotifierError =?> URIO[Logs, Unit] = {
    case SlackUnreacheable(err) => error(s"could not send notification: ${err.getMessage}")
    case RepositoryFail(err)    => error(s"could not verify active issues: ${err.getMessage}", Cause.fail(err))
  }
}
