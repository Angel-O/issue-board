package com.angelo.dashboard.client

import com.angelo.dashboard.environment.ExecutionEnvironment
import com.angelo.dashboard.environment.ExecutionEnvironment.ExecutionEnvironment
import com.angelo.dashboard.logging.ZLogger.ZLogger
import com.angelo.dashboard.resources.Http4sClient
import io.circe.Encoder
import org.http4s.Method.POST
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.{Request, Uri}
import zio.{Has, Task, ZLayer}

object ZHttpClient {

  type ZHttpClient = Has[Service]

  trait Service {
    def post[A: Encoder](endpoint: Uri, a: A): Task[Unit]
  }

  /** partial layer composition */
  val live: ZLayer[ExecutionEnvironment with ZLogger, Throwable, ZHttpClient] =
    (ZLayer.identity[ExecutionEnvironment] ++ Http4sClient.live) >>>
      ZLayer
        .fromServices[Http4sClient.Service, ExecutionEnvironment.Service, Service] { (client, env) =>
          import env._

          new Service {
            override def post[A: Encoder](endpoint: Uri, a: A): Task[Unit] =
              client.expect[Unit](Request[Task](POST, endpoint).withEntity(a))
          }
        }
}
