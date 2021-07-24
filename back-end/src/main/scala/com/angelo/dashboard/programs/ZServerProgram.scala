package com.angelo.dashboard.programs

import com.angelo.dashboard.layers.ZAppLayers.ServerEnvironment
import com.angelo.dashboard.services.ZHttpServer
import zio.ZIO
import zio.logging.Logging.info

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
        .catchAll(c => ZIO.fail(CannotCreateServer(c.squash)))
    }
}
