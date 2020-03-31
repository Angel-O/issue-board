package com.angelo.dashboard

import cats.effect.ConcurrentEffect
import fs2.text
import com.angelo.dashboard.ZConfig.{devModeEnabled, ZConfig}
import com.angelo.dashboard.ZIssueRepo.ZIssueRepo
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax._
import org.http4s.Method.POST
import org.http4s.circe.jsonDecoder
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{EntityBody, Request, Uri}
import zio._
import zio.clock.Clock
import zio.duration.durationInt

object ZNotifier {

  type ZNotifier = Has[ZNotifier.Service]

  trait Service {
    case class SlackPayload(text: String)

    def sendMessageToSlack: Task[Unit]

    protected def reqBody(payload: SlackPayload): EntityBody[Task] =
      fs2.Stream
        .emit(payload.asJson(deriveEncoder).noSpaces)
        .covary[Task]
        .through(text.utf8Encode)
  }

  private def live(implicit ce: ConcurrentEffect[Task]): URLayer[ZIssueRepo with ZConfig, ZNotifier] =
    ZLayer.fromServices[ZIssueRepo.Service, ZConfig.Service, ZNotifier.Service] { (repo, config) =>
      {
        val minCount = config.slackConfig.minimumActiveIssues
        val endpoint = config.slackConfig.endpoint

        new Service {
          def sendMessageToSlack: Task[Unit] =
            notifyOnSlack
              .whenM(
                repo.countNotArchived
                  .map(_ > minCount)
                  .tapError(err => error(s"Could not count active issues, ${err.getMessage}", err))
                  .orElse(ZIO.succeed(config.slackConfig.devMode))
              )

          private val payload = SlackPayload(
            s"Please schedule a team catchup! " +
              s"You have $minCount issues raised within the team"
          )

          private val request = Task.fromEither(Uri.fromString(endpoint)).map { uri =>
            Request[Task](method = POST, body = reqBody(payload), uri = uri)
          }

          private val notifyOnSlack =
            getExecutionCtx >>= { ec =>
              BlazeClientBuilder[Task](ec).resource.use(_.expect(request).unit)
            }
        }
      }
    }

  private def mock(implicit ce: ConcurrentEffect[Task]): ULayer[ZNotifier] =
    ZLayer.succeed {
      new Service {
        def sendMessageToSlack: Task[Unit] = logToConsole.compile.drain

        private val payload = SlackPayload(s"Sending mock message to Slack")

        private val logToConsole = reqBody(payload)
          .through(text.utf8Decode)
          .evalMap(info)
      }
    }

  def conditional(implicit ce: ConcurrentEffect[Task]): URLayer[ZIssueRepo with ZConfig, ZNotifier] =
    ZIO
      .ifM(devModeEnabled)(ZIO.succeed(mock), ZIO.succeed(live))
      .flatMap(getService.provideLayer(_))
      .toLayer

  //accessors
  val scheduleNotifications: URIO[ZNotifier with Clock, Unit] =
    ZIO.accessM[ZNotifier with Clock] { env =>
      val action   = env.get.sendMessageToSlack
      val schedule = Schedule.spaced(2.minutes)

      action
        .catchAll(err => error(s"Could not send notification: ${err.getMessage}", err))
        .repeat(schedule)
        .delay(10.seconds)
        .unit
    }

  val getService: URIO[ZNotifier, Service] = ZIO.service[ZNotifier.Service]
}
