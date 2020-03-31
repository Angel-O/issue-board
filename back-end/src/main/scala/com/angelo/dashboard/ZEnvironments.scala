package com.angelo.dashboard

import cats.effect.ConcurrentEffect
import com.angelo.dashboard.ZConfig.ZConfig
import com.angelo.dashboard.ZDbClient.ZDbClient
import com.angelo.dashboard.ZIssueApi.ZIssueApi
import com.angelo.dashboard.ZIssueRepo.ZIssueRepo
import com.angelo.dashboard.ZNotifier.ZNotifier
import zio.blocking.Blocking
import zio.clock.Clock
import zio.interop.catz.taskEffectInstance
import zio.{Runtime, Task, TaskLayer, ZEnv}

trait ZEnvironments { r: Runtime[ZEnv] =>

  implicit val ce: ConcurrentEffect[Task] = taskEffectInstance(r)

  type ProgramDependencies = ZIssueApi with ZNotifier with ZConfig with Clock

  val configLayer: TaskLayer[ZConfig] = ZConfig.live

  val dbClientLayer: TaskLayer[ZDbClient]               = configLayer ++ Blocking.live >>> ZDbClient.live
  val repoLayer: TaskLayer[ZIssueRepo]                  = (dbClientLayer ++ configLayer) >>> ZIssueRepo.live
  val apiLayer: TaskLayer[ZIssueApi]                    = repoLayer >>> ZIssueApi.live
  val httpServerDeps: TaskLayer[ZIssueApi with ZConfig] = apiLayer ++ configLayer

  val notifierLayer: TaskLayer[ZNotifier]                = (repoLayer ++ configLayer) >>> ZNotifier.conditional
  val slackNotifierDeps: TaskLayer[ZNotifier with Clock] = notifierLayer ++ Clock.live

  val programDeps: TaskLayer[ProgramDependencies] = httpServerDeps ++ slackNotifierDeps
}
