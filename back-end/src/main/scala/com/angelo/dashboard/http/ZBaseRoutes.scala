package com.angelo.dashboard.http

import com.angelo.dashboard.environment.ExecutionEnvironment
import com.angelo.dashboard.environment.ExecutionEnvironment.ExecutionEnvironment
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import zio.{Has, Task, URLayer, ZLayer}

object ZBaseRoutes extends Http4sDsl[Task] {

  type ZBaseRoutes = Has[Service]

  trait Service {
    val routes: HttpRoutes[Task]
  }

  val live: URLayer[ExecutionEnvironment, ZBaseRoutes] =
    ZLayer.fromService[ExecutionEnvironment.Service, Service] { env =>
      import env._

      new Service {
        override val routes: HttpRoutes[Task] = Router("/" -> api)

        private def api: HttpRoutes[Task] =
          HttpRoutes.of[Task] { case GET -> Root => Ok("issue-board") }
      }
    }
}
