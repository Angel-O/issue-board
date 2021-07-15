package com.angelo.dashboard.logging

import zio.logging.slf4j.Slf4jLogger
import zio.logging.{LogAnnotation, Logger, Logging}
import zio.{Cause, Has, ULayer}

object Logs {

  type Logs    = Has[Service]
  type Service = Logger[String]

  val live: ULayer[Logging] = Slf4jLogger.make((_, line) => line)

  /** not used, playing around with zio-logging looking for a valid use case */
  val liveAnnotated: ULayer[Logging] = {
    val logFormat = "[ERROR OCCURRED: %s] %s"

    Slf4jLogger.make { (context, line) =>
      context
        .get(LogAnnotation.Cause)
        .collect { case Cause.Fail(err: Throwable) => err.getMessage }
        .map(msg => logFormat.format(msg, line))
        .getOrElse(line)
    }
  }
}
