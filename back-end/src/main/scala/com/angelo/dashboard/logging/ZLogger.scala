package com.angelo.dashboard.logging

import zio.logging.slf4j.Slf4jLogger
import zio.logging.{LogAnnotation, Logger}
import zio.{Cause, Has, ULayer}

object ZLogger {

  type ZLogger = Has[Service]

  type Service = Logger[String]

  val live: ULayer[ZLogger] = {
    val logFormat = "[CAUSE: %s] %s"

    Slf4jLogger.make { (context, line) =>
      context
        .get(LogAnnotation.Cause)
        .map(_.untraced)
        .collect {
          case Cause.Fail(err: Throwable) => err.getClass.getSimpleName
          case Cause.Die(err: Throwable)  => err.getClass.getSimpleName
        }
        .map(msg => logFormat.format(msg, line))
        .getOrElse(line)
    }
  }
}
