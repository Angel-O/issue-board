package com.angelo.dashboard

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.parser.parse
import io.circe.{Decoder, Encoder}

import java.time.LocalDateTime

case class LifeCycle(createdAt: LocalDateTime)

object LifeCycle {

  implicit val decoder: Decoder[LifeCycle] = deriveDecoder
  implicit val encoder: Encoder[LifeCycle] = deriveEncoder

  def apply(): LifeCycle = new LifeCycle(createdAt = LocalDateTime.now())

  def decodeJsonString: String => Either[Throwable, LifeCycle] =
    parse _ andThen (_ flatMap (_.as[LifeCycle]))
}
