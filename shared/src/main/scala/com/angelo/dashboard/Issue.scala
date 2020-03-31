package com.angelo.dashboard

import java.time.LocalDateTime
import java.util.UUID

import com.angelo.dashboard.Issue.{Content, LifeCycle, Title}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.parser.parse

case class Issue private (id: String, title: Title, content: Content, isArchived: Boolean, lifeCycle: LifeCycle)

object Issue {

  // keeping it simple for now
  type Title   = String
  type Content = String

  implicit val decoder: Decoder[Issue] = deriveDecoder
  implicit val encoder: Encoder[Issue] = deriveEncoder

  case class LifeCycle(createdAt: LocalDateTime)

  object LifeCycle {

    implicit val decoder: Decoder[LifeCycle] = deriveDecoder
    implicit val encoder: Encoder[LifeCycle] = deriveEncoder

    def apply(): LifeCycle = new LifeCycle(createdAt = LocalDateTime.now())

    def decodeJsonString: String => Either[Throwable, LifeCycle] =
      (s: String) => parse(s) flatMap (_.as[LifeCycle])
  }

  //TODO create a facade for a js library build UUIDs
  def apply(title: Title, content: Content) =
    new Issue(UUID.randomUUID().toString, title, content, isArchived = false, LifeCycle())

  def apply(id: String, title: Title, content: Content, isArchived: Boolean, lifeCycle: LifeCycle) =
    new Issue(id, title, content, isArchived, lifeCycle)
}
