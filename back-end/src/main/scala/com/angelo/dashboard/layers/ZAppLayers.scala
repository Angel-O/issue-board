package com.angelo.dashboard.layers

import com.angelo.dashboard.client.ZDbClientProvider.ZDbClientProvider
import com.angelo.dashboard.client.ZHttpClientProvider.ZHttpClientProvider
import com.angelo.dashboard.client.{ZDbClientProvider, ZHttpClientProvider}
import com.angelo.dashboard.config.ZConfig
import com.angelo.dashboard.config.ZConfig.ZConfig
import com.angelo.dashboard.dao.ZIssueRepo
import com.angelo.dashboard.dao.ZIssueRepo.ZIssueRepo
import com.angelo.dashboard.environment.ExecutionEnvironment
import com.angelo.dashboard.environment.ExecutionEnvironment.ExecutionEnvironment
import com.angelo.dashboard.http.ZIssueRoutes
import com.angelo.dashboard.http.ZIssueRoutes.ZIssueRoutes
import com.angelo.dashboard.logging.Logs
import com.angelo.dashboard.logging.Logs.Logs
import com.angelo.dashboard.services.ZHttpServer.ZHttpServer
import com.angelo.dashboard.services.ZNotifier.ZNotifier
import com.angelo.dashboard.services.{ZHttpServer, ZNotifier}
import zio.clock.Clock
import zio.random.Random
import zio.{Has, Runtime, TaskLayer, ULayer, ZEnv, ZLayer}

trait ZAppLayers extends ZDefaultLayers { rtm: Runtime[ZEnv] =>

  import ZAppLayers._

  val runtimeLayer: ULayer[RuntimeEnv]                = ZLayer.succeed(rtm)
  val executionEnvLayer: ULayer[ExecutionEnvironment] = runtimeLayer >>> ExecutionEnvironment.live

  val configLayer: TaskLayer[ZConfig] = ZConfig.live
  val loggingLayer: ULayer[Logs]      = Logs.live

  val configAndLogsLayer: TaskLayer[ConfigAndLogs] = configLayer ++ loggingLayer

  val dbClientLayer: TaskLayer[ZDbClientProvider] = configAndLogsLayer >>> ZDbClientProvider.live
  val repoLayer: TaskLayer[ZIssueRepo]            = (dbClientLayer ++ blockingLayer ++ configAndLogsLayer) >>> ZIssueRepo.live

  val httpClientLayer: ULayer[ZHttpClientProvider] = (executionEnvLayer ++ loggingLayer) >>> ZHttpClientProvider.live
  val routesLayer: TaskLayer[ZIssueRoutes]         = (executionEnvLayer ++ repoLayer) >>> ZIssueRoutes.live

  val servicesSharedLayer: TaskLayer[ServicesShared] = executionEnvLayer ++ configLayer

  val httpServerLayer = (servicesSharedLayer ++ routesLayer ++ loggingLayer) >>> ZHttpServer.live
  val notifierLayer   = (servicesSharedLayer ++ consoleLayer ++ repoLayer ++ httpClientLayer) >>> ZNotifier.live

  val serverLayer: TaskLayer[ServerEnvironment]       = httpServerLayer ++ loggingLayer
  val schedulerLayer: TaskLayer[SchedulerEnvironment] = notifierLayer ++ configAndLogsLayer ++ clockLayer ++ randomLayer

  val appDependencies: TaskLayer[AppDependencies] = serverLayer ++ schedulerLayer
}

object ZAppLayers {

  type RuntimeEnv           = Has[Runtime[ZEnv]]
  type ServicesShared       = ExecutionEnvironment with ZConfig
  type ConfigAndLogs        = ZConfig with Logs
  type SchedulerEnvironment = ZNotifier with ConfigAndLogs with Clock with Random
  type ServerEnvironment    = ZHttpServer with Logs
  type AppDependencies      = SchedulerEnvironment with ServerEnvironment
}
