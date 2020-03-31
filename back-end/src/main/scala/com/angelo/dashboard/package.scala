package com.angelo

import zio.blocking.Blocking
import zio.logging.slf4j.Slf4jLogger
import zio.logging.{Logger, Logging}
import zio.{Cause, UIO, ULayer, URIO, ZIO}

import scala.concurrent.ExecutionContext

package object dashboard {

  object Logs {
    type Service = Logger[String]
    val loggingLayer: ULayer[Logging] = Slf4jLogger.make((_, line) => line)

    // accessor (provided)
    val getLogger: UIO[Logs.Service] = ZIO.service[Logs.Service].provideLayer(loggingLayer)
  }

  // global accessors
  def info(msg: String): UIO[Unit]                  = Logs.getLogger.flatMap(_.info(msg))
  def error(msg: String): UIO[Unit]                 = Logs.getLogger.flatMap(_.error(msg))
  def error(msg: String, err: Throwable): UIO[Unit] = Logs.getLogger.flatMap(_.error(msg, Cause.fail(err)))

  val getBlocking: URIO[Blocking, Blocking.Service] = ZIO.service[Blocking.Service]

  val getExecutionCtx: UIO[ExecutionContext] = ZIO.runtime[Any].map(_.platform.executor.asEC)
}
