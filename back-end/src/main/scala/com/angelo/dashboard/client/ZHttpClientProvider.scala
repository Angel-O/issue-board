package com.angelo.dashboard.client

import com.angelo.dashboard.environment.ExecutionEnvironment
import com.angelo.dashboard.environment.ExecutionEnvironment.ExecutionEnvironment
import com.angelo.dashboard.logging.Logs
import com.angelo.dashboard.logging.Logs.Logs
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import zio.interop.catz.zioResourceSyntax
import zio.{Has, Task, TaskManaged, URLayer, ZLayer}

object ZHttpClientProvider {

  type ZHttpClientProvider = Has[Service]

  trait Service {
    def asResource: TaskManaged[Client[Task]]
  }

  val live: URLayer[ExecutionEnvironment with Logs, ZHttpClientProvider] =
    ZLayer.fromServices[ExecutionEnvironment.Service, Logs.Service, Service] { (env, logging) =>
      import env._
      import logging._

      new Service {
        override def asResource: TaskManaged[Client[Task]] =
          BlazeClientBuilder
            .apply(ec)
            .resource
            .toManagedZIO
            .ensuring(info("http client shutting down"))
      }
    }
}
