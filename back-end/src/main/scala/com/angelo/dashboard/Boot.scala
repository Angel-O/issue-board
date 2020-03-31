package com.angelo.dashboard

import java.net.URI

import cats.effect.ExitCase.{Canceled, Completed, Error}
import cats.effect._
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.dsl.io._
import org.http4s.server.middleware.{CORS, CORSConfig}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

import scala.concurrent.duration._

object Boot extends IOApp {

  val dbClient = DynamoDbClient.builder
    .region(Region.EU_WEST_1)
    .endpointOverride(URI.create(AppConfig().dynamoDb.endpoint))
    .build()

  val databaseRepo  = new IssueRepo(dbClient, AppConfig().dynamoDb.issueTable)
  val issueService  = new IssueHandler(databaseRepo)
  val issueNotifier = new IssueNotifier(AppConfig().slackConfig.endpoint, databaseRepo)

  def loop: IO[Unit] =
    IO.sleep(2.minutes) >> (if (AppConfig().slackConfig.devMode) IO(println("Sending mock message to slack..."))
                            else issueNotifier.sendMessageToSlack) >> loop

  val corsConfig = CORSConfig(
    anyOrigin = true,
    anyMethod = true,
    allowCredentials = true,
    maxAge = 1.day.toSeconds
  )

  def run(args: List[String]): IO[ExitCode] = {
    val app = BlazeServerBuilder[IO]
      .bindHttp(AppConfig().serverConfig.port, AppConfig().serverConfig.host)
      .withHttpApp(
        CORS(
          Router(
            "/"    -> HttpRoutes.of[IO](_ => Ok("issue-board")),
            "/api" -> issueService.issuesService
          ).orNotFound,
          corsConfig
        )
      )
      .serve
      .compile
      .drain
    List(app, loop).parSequence_
      .as(ExitCode.Success)
      .guaranteeCase {
        case Error(e)  => IO(println(s"Error occurred: ${e.getLocalizedMessage}"))
        case Completed => IO(println("App completed"))
        case Canceled  => IO(println("App cancelled"))
      }
  }

}
