package com.angelo.dashboard.model

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class SlackPayload(text: String) extends AnyVal

object SlackPayload {
  implicit val encoder: Encoder[SlackPayload] = deriveEncoder

  def liveMessage(n: Int): SlackPayload =
    SlackPayload(s"Please schedule a team catchup! You have $n issues raised within the team")

  val mockMessage: SlackPayload =
    SlackPayload("Sending mock message to Slack")
}
