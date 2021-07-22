package com.angelo.dashboard.programs

import com.angelo.dashboard.layers.ZAppLayers.ServerEnvironment
import com.angelo.dashboard.services.ZHttpServer
import zio.logging.Logging.info
import zio.{RIO, ZIO}

object ZServerProgram {

  /** A very minimal program */
  val serveHttpRequests: RIO[ServerEnvironment, Nothing] =
    ZHttpServer.service.flatMap {
      _.managedServer.use(server => info(s"server running (secure: ${server.isSecure})") *> ZIO.never)
    }
}
