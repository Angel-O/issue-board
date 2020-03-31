package com.angelo.dashboard

import cats.effect._
import cats.implicits._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{HttpRoutes, Request}

class IssueHandler(issueRepo: IssueRepo) {

  val basePath = Root / "issues" //TODO fix this

  val issuesService = HttpRoutes
    .of[IO] {
      case GET -> basePath => getIssues

      case req @ POST -> basePath =>
        postIssue(req).flatMap(
          res =>
            if (res) Created("Item saved to db")
            else InternalServerError("Could not create Issue")
        )

      //TODO add get by Id

      case req @ PATCH -> basePath / "archive" =>
        archiveIssue(req).flatMap(
          res =>
            if (res) Ok("Item archived")
            else InternalServerError("Could not archive Issue")
        )

      case DELETE -> basePath / issueId =>
        deleteIssue(issueId).flatMap(
          res =>
            if (res) Ok("Item deleted")
            else InternalServerError("Could not delete Issue")
        )
    }

  private def getIssues =
    IO(issueRepo.retrieveIssues)
      .redeemWith(err => IO(println(err.getMessage)) >> InternalServerError(), Ok(_))

  private def postIssue(request: Request[IO]): IO[Boolean] =
    request.as[Issue].map(issueRepo.putIssue).map { res =>
      res.sdkHttpResponse().isSuccessful
    }

  private def archiveIssue(request: Request[IO]): IO[Boolean] =
    request.as[Issue].map(issueRepo.archiveIssue).map { res =>
      res.sdkHttpResponse().isSuccessful
    }

  private def deleteIssue(issueId: String): IO[Boolean] =
    IO(issueRepo.deleteIssue(issueId)).map { res =>
      res.sdkHttpResponse().isSuccessful
    }
}
