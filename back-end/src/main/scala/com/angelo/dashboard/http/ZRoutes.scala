package com.angelo.dashboard.http

import com.angelo.dashboard.http.ZBaseRoutes.ZBaseRoutes
import com.angelo.dashboard.http.ZIssueRoutes.ZIssueRoutes
import org.http4s.HttpApp
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware.AutoSlash
import zio._
import zio.interop.catz._

object ZRoutes extends Http4sDsl[Task] {

  type ZRoutes = Has[Service]

  trait Service {
    val httpApp: HttpApp[Task]
  }

  val live: URLayer[ZBaseRoutes with ZIssueRoutes, ZRoutes] =
    ZLayer.fromServices[ZBaseRoutes.Service, ZIssueRoutes.Service, Service] { (baseApi, issuesApi) =>
      new Service {

        override val httpApp: HttpApp[Task] =
          Router(
            "/"       -> baseApi.routes,
            "/api/v1" -> AutoSlash(issuesApi.routes)
          ).orNotFound
      }
    }
}
