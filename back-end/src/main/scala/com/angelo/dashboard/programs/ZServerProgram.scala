package com.angelo.dashboard.programs

import com.angelo.dashboard.layers.ZAppLayers.ServerEnvironment
import com.angelo.dashboard.logging.ZLogger.ZLogger
import com.angelo.dashboard.services.ZHttpServer
import zio.logging.Logging.{error, info}
import zio.{Cause, ZIO}

import java.net.BindException

object ZServerProgram {

  final case class CannotCreateServer(cause: Throwable) extends Exception("could not start server", cause)

  /**
   * A very minimal program. Sandboxing the effect because errors when binding the address aren't properly
   * handled by the underlying libraries.
   */
  val serveHttpRequests: ZIO[ServerEnvironment, CannotCreateServer, Nothing] =
    ZHttpServer.service.flatMap {
      _.managedServer
        .tapM(server => info(s"server running (secure: ${server.isSecure})"))
        .useForever
        .sandbox
        .catchAll(cause => errorHandler(cause, cause.squash).mapError(CannotCreateServer))
    }

  private def errorHandler: (Cause[Throwable], Throwable) => ZIO[ZLogger, BindException, Nothing] = {
    case (c, err: BindException) => error(s"unable to run server, ${err.getMessage}", c) *> ZIO.fail(err)
    case (c, err)                => error(s"unknown error while starting server, ${err.getMessage}", c) *> ZIO.die(err)
  }
}
