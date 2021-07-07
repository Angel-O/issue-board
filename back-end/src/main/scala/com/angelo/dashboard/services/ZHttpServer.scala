package com.angelo.dashboard.services

import com.angelo.dashboard.=?>
import com.angelo.dashboard.config.ZConfig.{getServerConfig, ServerConfig, ZConfig}
import com.angelo.dashboard.environment.ExecutionEnvironment
import com.angelo.dashboard.environment.ExecutionEnvironment.ExecutionEnvironment
import com.angelo.dashboard.http.ZIssueRoutes
import com.angelo.dashboard.http.ZIssueRoutes.ZIssueRoutes
import com.angelo.dashboard.logging.Logs
import com.angelo.dashboard.logging.Logs.Logs
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{`Content-Length`, Connection}
import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, _}
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{AutoSlash, CORS, CORSConfig}
import org.http4s.server.{Router, Server}
import zio.interop.catz.implicits._
import zio.interop.catz.zioResourceSyntax
import zio.{Cause, Has, Task, TaskManaged, URIO, URLayer, ZIO, ZLayer}

object ZHttpServer extends Http4sDsl[Task] {

  type ZHttpServer = Has[Service]

  trait Service {
    def serverResource: TaskManaged[Server[Task]]
  }

  val live: URLayer[ExecutionEnvironment with ZIssueRoutes with Logs with ZConfig, ZHttpServer] =
    ZLayer.fromServicesM[ExecutionEnvironment.Service, ZIssueRoutes.Service, Logs.Service, ZConfig, Nothing, Service](
      (env, issuesRoutes, logging) =>
        getServerConfig.map { cfg =>
          import env._
          import logging._

          val routes =
            Router(
              "/"       -> HttpRoutes.of[Task] { case GET -> Root => Ok("issue-board") },
              "/api/v1" -> AutoSlash(issuesRoutes.routes)
            )

          new Service {
            override def serverResource: TaskManaged[Server[Task]] =
              BlazeServerBuilder
                .apply[Task](ec)
                .withServiceErrorHandler(errorHandler)
                .withIdleTimeout(cfg.connectionIdleTimeout)
                .withResponseHeaderTimeout(cfg.responseTimeout)
                .withHttpApp(CORS(routes.orNotFound, corsConfig(cfg)))
                .bindHttp(cfg.port, cfg.host)
                .resource
                .toManagedZIO

            private def errorHandler(req: Request[Task]): Throwable =?> Task[Response[Task]] =
              err => error(s"Error occurred: ${err.getMessage}", Cause.fail(err)).as(errorResponse(req))

            private def errorResponse(req: Request[Task]): Response[Task] =
              Response(InternalServerError, req.httpVersion, errorResponseHeaders(req))
          }
        }
    )

  private def errorResponseHeaders(req: Request[Task]) = {
    val allowOriginValue = req.headers.get("origin".ci).map(_.value).getOrElse("*")
    Headers.of(Connection("close".ci), `Content-Length`.zero, Header("Access-Control-Allow-Origin", allowOriginValue))
  }

  private def corsConfig(cfg: ServerConfig): CORSConfig =
    CORS.DefaultCORSConfig.copy(anyOrigin = false, allowedOrigins = _ == cfg.allowedOrigin)

  // accessor
  val service: URIO[ZHttpServer, Service] = ZIO.service[Service]
}
