package com.angelo.dashboard.middleware

import com.angelo.dashboard.circuit.AppModel
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

case class ModelUpdate(stateBefore: AppModel, stateAfter: AppModel)

object ModelUpdate {
  implicit val encoder: Encoder[ModelUpdate] = deriveEncoder
}
