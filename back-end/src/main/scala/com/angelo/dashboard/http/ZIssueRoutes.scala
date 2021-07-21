package com.angelo.dashboard.http

import com.angelo.dashboard.dao.ZIssueRepo
import com.angelo.dashboard.dao.ZIssueRepo.{IssueNotFound, ZIssueRepo}
import com.angelo.dashboard.environment.ExecutionEnvironment
import com.angelo.dashboard.environment.ExecutionEnvironment.ExecutionEnvironment
import com.angelo.dashboard.{=?>, Issue}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Request, Response}
import zio._

object ZIssueRoutes extends Http4sDsl[Task] {

  type ZIssueRoutes = Has[Service]

  trait Service {
    val routes: HttpRoutes[Task]
  }

  val live: URLayer[ExecutionEnvironment with ZIssueRepo, ZIssueRoutes] =
    ZLayer.fromServices[ExecutionEnvironment.Service, ZIssueRepo.Service, Service] { (env, issueRepo) =>
      import env._

      new Service {

        override val routes: HttpRoutes[Task] = Router("issues" -> api)

        private def api: HttpRoutes[Task] =
          HttpRoutes.of[Task] {
            case GET -> Root                            => getIssues
            case GET -> Root / NonEmptyPath(issueId)    => getIssue(issueId)
            case req @ POST -> Root                     => createIssue(req)
            case req @ PATCH -> Root                    => archiveIssue(req)
            case DELETE -> Root / NonEmptyPath(issueId) => deleteIssue(issueId)
          }

        private def getIssue(issueId: String): Task[Response[Task]] =
          (issueRepo.getIssue(issueId) >>= (Ok(_))) catchSome errorHandler

        private def getIssues: Task[Response[Task]] =
          issueRepo.retrieveIssues >>= (Ok(_))

        private def createIssue(request: Request[Task]): Task[Response[Task]] =
          request.as[Issue].flatMap(issueRepo.putIssue) >>= (Created(_))

        private def archiveIssue(request: Request[Task]): Task[Response[Task]] =
          (request.as[Issue].flatMap(issueRepo.archiveIssue) >>= (Ok(_))) catchSome errorHandler

        private def deleteIssue(issueId: String): Task[Response[Task]] =
          (issueRepo.deleteIssue(issueId) >>= (Ok(_))) catchSome errorHandler

        private val errorHandler: Throwable =?> Task[Response[Task]] = {
          case IssueNotFound => NotFound()
        }
      }
    }

  object NonEmptyPath {
    def unapply(str: String): Option[String] = Option.when(str.nonEmpty)(str)
  }
}
