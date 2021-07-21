package com.angelo.dashboard.services

import com.angelo.dashboard.client.ZHttpClient
import com.angelo.dashboard.client.ZHttpClient.ZHttpClient
import com.angelo.dashboard.config.ZConfig.{getSlackConfig, ZConfig}
import com.angelo.dashboard.dao.ZIssueRepo
import com.angelo.dashboard.dao.ZIssueRepo.ZIssueRepo
import com.angelo.dashboard.model.SlackPayload
import io.circe.syntax.EncoderOps
import org.http4s.Method.POST
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.{Request, Uri}
import zio._
import zio.console.Console
import zio.interop.catz.taskConcurrentInstance

import scala.util.control.NoStackTrace

object ZNotifier {

  type ZNotifier = Has[Service]

  trait Service {
    def sendMessageToSlack: IO[NotifierError, Unit]
  }

  sealed trait NotifierError                             extends NoStackTrace
  case class InvalidUri(override val getMessage: String) extends NotifierError
  case class SlackUnreacheable(cause: Throwable)         extends NotifierError
  case class RepositoryFail(cause: Throwable)            extends NotifierError

  val live: RLayer[ZConfig with ZHttpClient with Console with ZIssueRepo, ZNotifier] =
    ZLayer
      .fromServicesM[ZHttpClient.Service, Console.Service, ZIssueRepo.Service, ZConfig, Throwable, Service] {
        (client, console, repo) =>
          getSlackConfig.map { cfg =>
            import cfg._

            new Service {
              override def sendMessageToSlack: IO[NotifierError, Unit] =
                sendNotification.whenM(enoughActiveIssues)

              private val sendNotification: IO[NotifierError, Unit] =
                ZIO.ifM(ZIO.succeed(devMode))(logToConsole, notifyOnSlack)

              private def logToConsole: UIO[Unit] =
                console.putStrLn(SlackPayload.mockMessage.asJson.noSpaces).ignore

              private def notifyOnSlack: IO[NotifierError, Unit] =
                parseEndpoint(s"$endpoint/$token")
                  .map(Request[Task](POST, _).withEntity(SlackPayload.liveMessage(minimumActiveIssues)))
                  .flatMap(sendRequest)

              private def sendRequest(request: Request[Task]): IO[SlackUnreacheable, Unit] =
                client.expect[Unit](request).mapError(SlackUnreacheable)

              private def enoughActiveIssues: IO[RepositoryFail, Boolean] =
                repo.countActiveIssues.bimap(RepositoryFail, _ > minimumActiveIssues)
            }
          }
      }

  private val parseEndpoint: String => IO[InvalidUri, Uri] = endpoint =>
    ZIO
      .fromEither(Uri.fromString(endpoint))
      .orElseFail(InvalidUri(s"invalid endpoint: $endpoint"))

  // accessor
  val service: URIO[ZNotifier, Service] = ZIO.service[Service]
}
