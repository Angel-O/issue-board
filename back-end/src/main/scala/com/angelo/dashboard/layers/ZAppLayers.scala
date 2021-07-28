package com.angelo.dashboard.layers

import com.angelo.dashboard.client.ZDbClient.ZDbClient
import com.angelo.dashboard.client.ZHttpClient.ZHttpClient
import com.angelo.dashboard.client.{ZDbClient, ZHttpClient}
import com.angelo.dashboard.config.ZConfig
import com.angelo.dashboard.config.ZConfig.ZConfig
import com.angelo.dashboard.dao.ZIssueRepo
import com.angelo.dashboard.dao.ZIssueRepo.ZIssueRepo
import com.angelo.dashboard.environment.ExecutionEnvironment
import com.angelo.dashboard.environment.ExecutionEnvironment.ExecutionEnvironment
import com.angelo.dashboard.http.ZBaseRoutes.ZBaseRoutes
import com.angelo.dashboard.http.ZIssueRoutes.ZIssueRoutes
import com.angelo.dashboard.http.ZRoutes.ZRoutes
import com.angelo.dashboard.http.{ZBaseRoutes, ZIssueRoutes, ZRoutes}
import com.angelo.dashboard.logging.ZLogger
import com.angelo.dashboard.logging.ZLogger.ZLogger
import com.angelo.dashboard.services.ZHttpServer.ZHttpServer
import com.angelo.dashboard.services.ZIssueTableMaker.ZIssueTableMaker
import com.angelo.dashboard.services.ZNotifier.ZNotifier
import com.angelo.dashboard.services.{ZHttpServer, ZIssueTableMaker, ZNotifier}
import zio.clock.Clock
import zio.random.Random
import zio.{Has, Runtime, TaskLayer, ULayer, ZEnv, ZLayer}

trait ZAppLayers extends ZDefaultLayers { rtm: Runtime[ZEnv] =>

  import ZAppLayers._

  val runtimeLayer: ULayer[RuntimeEnv]                = ZLayer.succeed(rtm)
  val executionEnvLayer: ULayer[ExecutionEnvironment] = runtimeLayer >>> ExecutionEnvironment.live

  val configLayer: ULayer[ZConfig]  = ZConfig.live
  val loggingLayer: ULayer[ZLogger] = ZLogger.live

  val configAndLogsLayer: ULayer[ConfigAndLogger] = configLayer ++ loggingLayer

  // client
  val dbClientLayer: TaskLayer[ZDbClient]     = (blockingLayer ++ configAndLogsLayer) >>> ZDbClient.live
  val httpClientLayer: TaskLayer[ZHttpClient] = (executionEnvLayer ++ loggingLayer) >>> ZHttpClient.live

  // dao
  val repoLayer: TaskLayer[ZIssueRepo] = (dbClientLayer ++ configAndLogsLayer) >>> ZIssueRepo.live

  // http
  val baseRoutesLayer: ULayer[ZBaseRoutes]      = executionEnvLayer >>> ZBaseRoutes.live
  val issueRoutesLayer: TaskLayer[ZIssueRoutes] = (executionEnvLayer ++ repoLayer) >>> ZIssueRoutes.live
  val httpAppLayer: TaskLayer[ZRoutes]          = (baseRoutesLayer ++ issueRoutesLayer) >>> ZRoutes.live

  // services
  val servicesSharedLayer = executionEnvLayer ++ configLayer
  val dbTableMakerLayer   = dbClientLayer >>> ZIssueTableMaker.live
  val httpServerLayer     = (servicesSharedLayer ++ httpAppLayer ++ loggingLayer) >>> ZHttpServer.live
  val notifierLayer       = (servicesSharedLayer ++ consoleLayer ++ repoLayer ++ httpClientLayer) >>> ZNotifier.live

  // programs
  val tableMakerLayer: TaskLayer[TableMakerEnvironment] = dbTableMakerLayer ++ loggingLayer ++ clockLayer ++ configLayer
  val serverLayer: TaskLayer[ServerEnvironment]         = httpServerLayer ++ loggingLayer
  val schedulerLayer: TaskLayer[SchedulerEnvironment]   = notifierLayer ++ configAndLogsLayer ++ clockLayer ++ randomLayer

  // app
  val appDependencies: TaskLayer[AppDependencies] = dbTableMakerLayer ++ serverLayer ++ schedulerLayer
}

object ZAppLayers {

  type RuntimeEnv            = Has[Runtime[ZEnv]]
  type ConfigAndLogger       = ZConfig with ZLogger
  type SchedulerEnvironment  = ZNotifier with ConfigAndLogger with Clock with Random
  type ServerEnvironment     = ZHttpServer with ZLogger
  type TableMakerEnvironment = ZIssueTableMaker with ZLogger with Clock with ZConfig
  type AppDependencies       = TableMakerEnvironment with SchedulerEnvironment with ServerEnvironment
}
