package com.angelo.dashboard.data

import cats.kernel.Semigroup

import scala.util.control.NoStackTrace

object Implicits {
  final case class AccumulatedException(errors: Throwable*) extends NoStackTrace {
    override def getMessage: String = errors.map(_.getMessage).mkString(" => ")
  }

  /** allows to have instances of [[cats.NonEmptyParallel]] to accumulate errors on Either with Throwable Left **/
  implicit val accumulatedExceptions = new Semigroup[Throwable] {
    override def combine(x: Throwable, y: Throwable): Throwable = AccumulatedException(x, y)
  }
}
