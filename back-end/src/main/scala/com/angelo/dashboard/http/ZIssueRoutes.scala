package com.angelo.dashboard.http

import com.angelo.dashboard.dao.ZIssueRepo
import com.angelo.dashboard.dao.ZIssueRepo.{IssueNotFound, ZIssueRepo}
import com.angelo.dashboard.environment.ExecutionEnvironment
import com.angelo.dashboard.environment.ExecutionEnvironment.ExecutionEnvironment
import com.angelo.dashboard.{=?>, Issue}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
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
        override val routes: HttpRoutes[Task] = HttpRoutes.of[Task] {
          case GET -> Root / "issues"                            => getIssues
          case GET -> Root / "issues" / NonEmptyPath(issueId)    => getIssue(issueId)
          case req @ POST -> Root / "issues"                     => createIssue(req)
          case req @ PATCH -> Root / "issues"                    => archiveIssue(req)
          case DELETE -> Root / "issues" / NonEmptyPath(issueId) => deleteIssue(issueId)
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
