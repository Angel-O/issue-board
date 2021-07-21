package com.angelo.dashboard.client

import com.angelo.dashboard.environment.ExecutionEnvironment
import com.angelo.dashboard.environment.ExecutionEnvironment.ExecutionEnvironment
import com.angelo.dashboard.logging.ZLogger
import com.angelo.dashboard.logging.ZLogger.ZLogger
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import zio.interop.catz.zioResourceSyntax
import zio.{Has, RLayer, Task, ZLayer}

object ZHttpClient {

  type ZHttpClient = Has[Service]

  type Service = Client[Task]

  val live: RLayer[ExecutionEnvironment with ZLogger, ZHttpClient] =
    ZLayer.fromServicesManaged[ExecutionEnvironment.Service, ZLogger.Service, Any, Throwable, Service] {
      (env, logging) =>
        import env._
        import logging._

        BlazeClientBuilder
          .apply(ec)
          .resource
          .toManagedZIO
          .ensuring(info("http client shutting down"))
    }
}
