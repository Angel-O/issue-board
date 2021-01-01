package com.angelo.dashboard

import cats.effect.ConcurrentEffect
import com.angelo.dashboard.ZConfig.ZConfig
import com.angelo.dashboard.ZIssueApi.ZIssueApi
import org.http4s.{Headers, HttpRoutes, Request, Response, Status}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{`Content-Length`, Connection}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.{AutoSlash, CORS, CORSConfig}
import zio._
import zio.duration.durationInt
import zio.interop.catz.implicits._

object ZHttpServer extends Http4sDsl[Task] {

  def serveRequests(implicit ce: ConcurrentEffect[Task]): RIO[ZIssueApi with ZConfig, Unit] =
    (ZIssueApi.endpoints zip ZConfig.getServerConfig) >>= {
      case (endpoints, config) =>
        val httpApp = CORS(
          Router(
            "/"    -> HttpRoutes.of[Task] { case GET -> Root => Ok("issue-board") },
            "/api" -> AutoSlash(endpoints)
          ).orNotFound,
          CORSConfig(anyOrigin = true, allowCredentials = true, maxAge = 1.day.getSeconds)
        )

        getExecutionCtx >>= { ec =>
          BlazeServerBuilder
            .apply[Task](ec)
            .withNio2(true)
            .withServiceErrorHandler(errorHandler)
            .withHttpApp(httpApp)
            .bindHttp(config.port, config.host)
            .serve
            .compile
            .drain
        }
    }

  private def errorHandler(req: Request[Task]): PartialFunction[Throwable, Task[Response[Task]]] = {
    val headers = Headers(Connection("close".ci) :: `Content-Length`.zero :: Nil)
    err =>
      error(s"Error occurred: ${err.getMessage}", err) *> Task.succeed(
        Response(Status.InternalServerError, req.httpVersion, headers)
      )
  }
}
