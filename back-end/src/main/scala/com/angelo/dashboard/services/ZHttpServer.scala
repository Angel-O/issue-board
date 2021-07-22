package com.angelo.dashboard.services

import com.angelo.dashboard.=?>
import com.angelo.dashboard.config.ZConfig.{getServerConfig, ServerConfig, ZConfig}
import com.angelo.dashboard.environment.ExecutionEnvironment
import com.angelo.dashboard.environment.ExecutionEnvironment.ExecutionEnvironment
import com.angelo.dashboard.http.ZRoutes
import com.angelo.dashboard.http.ZRoutes.ZRoutes
import com.angelo.dashboard.logging.ZLogger
import com.angelo.dashboard.logging.ZLogger.ZLogger
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{`Content-Length`, Connection}
import org.http4s.implicits._
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{CORS, CORSConfig}
import zio.interop.catz.implicits._
import zio.interop.catz.zioResourceSyntax
import zio.{Cause, Has, RLayer, Task, TaskManaged, UIO, URIO, ZIO, ZLayer}

object ZHttpServer extends Http4sDsl[Task] {

  type ZHttpServer = Has[Service]

  trait Service {

    /**
     * unlike clients this resource is suspended, since there is no plan to cache it via ZLayers and reuse it by
     * injecting it vertically into other layers
     */
    def managedServer: TaskManaged[Server[Task]]
  }

  val live: RLayer[ExecutionEnvironment with ZRoutes with ZLogger with ZConfig, ZHttpServer] =
    ZLayer.fromServicesM[ExecutionEnvironment.Service, ZRoutes.Service, ZLogger.Service, ZConfig, Throwable, Service] {
      (env, api, logging) =>
        getServerConfig map { cfg =>
          import env._
          import logging._

          new Service {
            override def managedServer: TaskManaged[Server[Task]] =
              BlazeServerBuilder
                .apply[Task](ec)
                .withServiceErrorHandler(errorHandler(onError))
                .withIdleTimeout(cfg.connectionIdleTimeout)
                .withResponseHeaderTimeout(cfg.responseTimeout)
                .withHttpApp(CORS(api.httpApp, corsConfig(cfg)))
                .bindHttp(cfg.port, cfg.host)
                .resource
                .toManagedZIO
                .ensuring(info("server shut down"))

            private val onError: Throwable => UIO[Unit] =
              err => error(s"Error occurred: ${err.getMessage}", Cause.fail(err))
          }
        }
    }

  private def errorHandler(onError: Throwable => UIO[Unit])(req: Request[Task]): Throwable =?> Task[Response[Task]] =
    err => onError(err).as(errorResponse(req))

  private def corsConfig(cfg: ServerConfig): CORSConfig =
    CORS.DefaultCORSConfig.copy(anyOrigin = false, allowedOrigins = _ == cfg.allowedOrigin)

  private def errorResponse(req: Request[Task]): Response[Task] =
    Response(InternalServerError, req.httpVersion, errorResponseHeaders(req))

  private def errorResponseHeaders(req: Request[Task]) = {
    val allowOriginValue = req.headers.get("origin".ci).map(_.value).getOrElse("*")
    Headers.of(Connection("close".ci), `Content-Length`.zero, Header("Access-Control-Allow-Origin", allowOriginValue))
  }

  // accessor
  val service: URIO[ZHttpServer, Service] = ZIO.service[Service]
}
