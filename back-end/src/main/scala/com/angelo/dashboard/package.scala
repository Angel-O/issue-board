package com.angelo

import zio.{CanFail, ZIO}

import scala.reflect.ClassTag

package object dashboard {
  type =?>[-A, +B] = PartialFunction[A, B]

  /** Same as [[zio.ZIO.ZioRefineToOrDieOps]], but has the environment set to [[Any]] (only needed to avoid intelliJ
   * error highlighting) */
  implicit final class ZioRefineToOrDieOpsAny[E <: Throwable, A](private val self: ZIO[Any, E, A]) extends AnyVal {

    def refineToOrDie[E1 <: E: ClassTag](implicit ev: CanFail[E]): ZIO[Any, E1, A] =
      self.refineOrDie { case e: E1 => e }
  }
}
