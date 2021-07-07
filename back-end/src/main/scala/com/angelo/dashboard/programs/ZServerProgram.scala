package com.angelo.dashboard.programs

import com.angelo.dashboard.layers.ZAppLayers.ServerEnvironment
import com.angelo.dashboard.services.ZHttpServer
import zio.RIO
import zio.logging.Logging.info

object ZServerProgram {

  val serveHttpRequests: RIO[ServerEnvironment, Nothing] =
    ZHttpServer.service
      .flatMap(_.serverResource.useForever)
      .onTermination(_ => info("server shut down"))
}
