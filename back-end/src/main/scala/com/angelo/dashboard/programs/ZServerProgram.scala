package com.angelo.dashboard.programs

import com.angelo.dashboard.layers.ZAppLayers.ServerEnvironment
import com.angelo.dashboard.services.ZHttpServer
import zio.RIO
import zio.logging.Logging.info

object ZServerProgram {

  /** A very minimal program. Use of the managed resource, handled by the ZLayer */
  val serveHttpRequests: RIO[ServerEnvironment, Unit] =
    ZHttpServer.service
      .tap(server => info(s"server created. secure: ${server.isSecure}"))
      .unit
}
