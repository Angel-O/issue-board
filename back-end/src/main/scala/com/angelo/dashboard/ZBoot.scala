package com.angelo.dashboard

import com.angelo.dashboard.layers.ZAppLayers
import com.angelo.dashboard.layers.ZAppLayers._
import com.angelo.dashboard.logging.Logs.Logs
import com.angelo.dashboard.programs.ZPrograms
import zio.Exit.Failure
import zio._
import zio.interop.catz.CatsApp
import zio.logging.Logging.error

object ZBoot extends CatsApp with ZAppLayers {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {

    val app: RIO[AppDependencies, Unit] =
      ZIO.collectAllPar_(ZPrograms.programs)

    app
      .onExit(finalizer)
      .provideLayer(appDependencies)
      .untraced
      .exitCode
  }

  private def finalizer(exit: Exit[Throwable, Unit]): URIO[Logs, Unit] =
    ZIO.whenCase(exit) {
      case Failure(c) if c.failed => error(s"App terminated: ${c.squash.getMessage}")
      case Failure(c) if c.died   => error(s"App exited unexpectedly: ${c.squash.getMessage}")
    }
}
