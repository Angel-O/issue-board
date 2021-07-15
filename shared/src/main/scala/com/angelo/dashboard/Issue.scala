package com.angelo.dashboard

import com.angelo.dashboard.Issue.{Content, Title}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.util.UUID

case class Issue private (id: String, title: Title, content: Content, isArchived: Boolean, lifeCycle: LifeCycle)

object Issue {

  // keeping it simple for now
  type Title   = String
  type Content = String

  implicit val decoder: Decoder[Issue] = deriveDecoder
  implicit val encoder: Encoder[Issue] = deriveEncoder

  def apply(title: Title, content: Content) =
    new Issue(UUID.randomUUID().toString, title, content, isArchived = false, LifeCycle())

  def make(id: String, title: Title, content: Content, isArchived: Boolean, lifeCycle: LifeCycle) =
    new Issue(id, title, content, isArchived, lifeCycle)
}
