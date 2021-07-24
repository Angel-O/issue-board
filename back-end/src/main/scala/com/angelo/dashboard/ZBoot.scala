package com.angelo.dashboard

import com.angelo.dashboard.layers.ZAppLayers
import com.angelo.dashboard.layers.ZAppLayers._
import com.angelo.dashboard.logging.ZLogger.ZLogger
import com.angelo.dashboard.programs.ZPrograms
import zio.Exit.Failure
import zio._
import zio.interop.catz.CatsApp
import zio.logging.Logging.{error, info}

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

  private def finalizer(exit: Exit[Throwable, Unit]): URIO[ZLogger, Unit] =
    ZIO.whenCase(exit) {
      case Failure(c) if c.interruptedOnly => info("App interrupted")
      case Failure(c) if c.failed          => error(s"App terminated: ${c.squash.getMessage}", Cause.fail(c.squash))
      case Failure(c) if c.died            => error(s"App exited unexpectedly: ${c.squash.getMessage}")
    }
}
