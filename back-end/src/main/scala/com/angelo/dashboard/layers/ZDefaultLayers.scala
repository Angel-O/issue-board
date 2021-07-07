package com.angelo.dashboard.layers

import zio.ULayer
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.random.Random

trait ZDefaultLayers {

  val consoleLayer: ULayer[Console]   = Console.live
  val clockLayer: ULayer[Clock]       = Clock.live
  val randomLayer: ULayer[Random]     = Random.live
  val blockingLayer: ULayer[Blocking] = Blocking.live
}
