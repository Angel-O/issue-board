package com.angelo.dashboard

import cats.effect.{ConcurrentEffect, IO}
import cats.implicits._
import org.http4s.Method._
import org.http4s.{Request, Uri}
import org.http4s.client.blaze._
import org.http4s.client.dsl.io._
import io.circe.literal._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class IssueNotifier(endpoint: String, issueRepo: IssueRepo) {

  def sendMessageToSlack(implicit ce: ConcurrentEffect[IO]) = BlazeClientBuilder[IO](global).resource.use { client =>
    val postRequest =
      POST[String](
        json"""{"text":"Please schedule a team catchup! You have 3 issues raised within the team"}""".noSpaces,
        Uri.fromString(endpoint).right.get
      )

    if (issueRepo.countNotArchived >= AppConfig().slackConfig.minimumActiveIssues) { //TODO avoid scanning table every few minutes
      client.expect[String](postRequest) >> IO.unit
    } else {
      IO.unit
    }

  }

}
