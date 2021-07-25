package com.angelo.dashboard.programs

import com.angelo.dashboard.layers.ZAppLayers.ServerEnvironment
import com.angelo.dashboard.logging.ZLogger.ZLogger
import com.angelo.dashboard.services.ZHttpServer
import zio.logging.Logging.{error, info}
import zio.{Cause, URIO, ZIO}

import java.net.BindException

object ZServerProgram {

  final case class CannotCreateServer(cause: Throwable) extends Exception(cause)

  /**
   * A very minimal program. Sandboxing the effect because errors when binding the address aren't properly
   * handled by the underlying libraries.
   */
  val serveHttpRequests: ZIO[ServerEnvironment, CannotCreateServer, Nothing] =
    ZHttpServer.service.flatMap {
      _.managedServer
        .use(server => info(s"server running (secure: ${server.isSecure})") *> ZIO.never)
        .sandbox
        .mapError(_.untraced)
        .catchAll(cause => printError(cause) *> ZIO.fail(CannotCreateServer(cause.squash)))
    }

  private def printError: Cause[Throwable] => URIO[ZLogger, Unit] = {
    case c @ Cause.Die(err: BindException) => error(s"unable to create server, ${err.getMessage}", c)
    case c                                 => error(s"unknown error while creating server, ${c.squash.getMessage}", c)
  }
}
