package com.angelo.dashboard.environment

import cats.effect.ConcurrentEffect
import com.angelo.dashboard.layers.ZAppLayers.RuntimeEnv
import zio.interop.catz.taskEffectInstance
import zio.{Has, Runtime, Task, URLayer, ZEnv, ZLayer}

import scala.concurrent.ExecutionContext

object ExecutionEnvironment {

  type ExecutionEnvironment = Has[Service]

  trait Service {
    implicit val ce: ConcurrentEffect[Task]
    implicit val ec: ExecutionContext
  }

  val live: URLayer[RuntimeEnv, ExecutionEnvironment] =
    ZLayer.fromService[Runtime[ZEnv], Service] { runtime =>
      new Service {
        override implicit val ce: ConcurrentEffect[Task] = taskEffectInstance(runtime)
        override implicit val ec: ExecutionContext       = runtime.platform.executor.asEC
      }
    }
}
