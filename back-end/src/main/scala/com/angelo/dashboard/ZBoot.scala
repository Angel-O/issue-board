package com.angelo.dashboard

import zio.Exit.Failure
import zio._
import zio.interop.catz.CatsApp

object ZBoot extends CatsApp with ZEnvironments {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {

    val program: RIO[ProgramDependencies, Unit] =
      ZIO.collectAllPar_(ZHttpServer.serveRequests :: ZNotifier.scheduleNotifications :: Nil)

    program
      .provideLayer(programDeps)
      .onExit(finalizer)
      .exitCode
  }

  private def finalizer(exit: Exit[Throwable, Unit]): UIO[Unit] =
    ZIO.whenCase(exit) {
      case Failure(c) if c.died || c.failed => error("App exited unexpectedly")
      case _                                => info("App terminated")
    }
}
