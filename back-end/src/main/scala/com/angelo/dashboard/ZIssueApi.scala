package com.angelo.dashboard

import cats.effect.ConcurrentEffect
import com.angelo.dashboard.ZIssueRepo.{IssueNotFound, ZIssueRepo}
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Request, Response}
import zio._

object ZIssueApi extends Http4sDsl[Task] {

  type ZIssueApi = Has[ZIssueApi.Service]

  trait Service {
    val api: HttpRoutes[Task]
  }

  def live(implicit ce: ConcurrentEffect[Task]): URLayer[ZIssueRepo, ZIssueApi] =
    ZLayer.fromService[ZIssueRepo.Service, ZIssueApi.Service] { issueRepo =>
      new Service {
        val api: HttpRoutes[Task] = HttpRoutes.of[Task] {
          case GET -> Root / "issues"                            => getIssues
          case GET -> Root / "issues" / NonEmptyPath(issueId)    => getIssue(issueId)
          case req @ POST -> Root / "issues"                     => postIssue(req)
          case req @ PATCH -> Root / "issues" / "archive"        => archiveIssue(req)
          case DELETE -> Root / "issues" / NonEmptyPath(issueId) => deleteIssue(issueId)
        }

        private def getIssue(issueId: String): Task[Response[Task]] =
          (issueRepo.getIssue(issueId) >>=
            (Ok(_))).catchSome { case IssueNotFound => NotFound() }

        private def getIssues: Task[Response[Task]] =
          issueRepo.retrieveIssues >>= (Ok(_))

        private def postIssue(request: Request[Task]): Task[Response[Task]] =
          request.as[Issue].flatMap(issueRepo.putIssue) >>= (Created(_))

        private def archiveIssue(request: Request[Task]): Task[Response[Task]] =
          (request.as[Issue].flatMap(issueRepo.archiveIssue) >>=
            (Ok(_))).catchSome { case IssueNotFound => NotFound() }

        private def deleteIssue(issueId: String): Task[Response[Task]] =
          (issueRepo.deleteIssue(issueId) >>=
            (Ok(_))).catchSome { case IssueNotFound => NotFound() }

      }
    }

  //accessors
  val endpoints: URIO[ZIssueApi, HttpRoutes[Task]] = ZIO.service[Service].map(_.api)

  object NonEmptyPath {
    def unapply(str: String): Option[String] = Option.when(str.nonEmpty)(str)
  }
}
