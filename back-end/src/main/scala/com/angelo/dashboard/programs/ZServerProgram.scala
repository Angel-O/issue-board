package com.angelo.dashboard.programs

import com.angelo.dashboard.layers.ZAppLayers.ServerEnvironment
import com.angelo.dashboard.services.ZHttpServer
import zio.URIO
import zio.logging.Logging.info

object ZServerProgram {

  /** A very minimal program. Use of the managed resource, handled by the ZLayer */
  val serveHttpRequests: URIO[ServerEnvironment, Unit] =
    ZHttpServer.service
      .tap(server => info(s"server created (secure: ${server.isSecure})"))
      .unit
}
