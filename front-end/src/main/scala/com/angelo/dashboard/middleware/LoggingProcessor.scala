package com.angelo.dashboard.middleware

import java.time.LocalTime

import diode.{ActionProcessor, ActionResult, Dispatcher, NoAction}
import com.angelo.dashboard.circuit.AppModel
import io.circe.Encoder
import io.circe.generic.semiauto._
import io.circe.syntax._
import org.scalajs.dom

import scala.scalajs.js.JSON

object LoggingProcessor {

  case class ModelUpdate(stateBefore: AppModel, stateAfter: AppModel)

  implicit val encoder: Encoder[ModelUpdate] = deriveEncoder

  def padded(str: String): String = str.padTo(40, " ").mkString
}

class LoggingProcessor(enableLogging: Boolean) extends ActionProcessor[AppModel] {

  import LoggingProcessor._

  override def process(
    dispatch: Dispatcher,
    action: Any,
    next: Any => ActionResult[AppModel],
    currentModel: AppModel
  ): ActionResult[AppModel] = {

    val result = next(action)

    action match {
      case NoAction => result
      case _ if enableLogging =>
        dom.console.log(
          padded(s"%cAction ${action.getClass.getSimpleName} @ ${LocalTime.now()}") + "%o",
          "color:cyan",
          JSON.parse(
            ModelUpdate(
              currentModel,
              result.newModelOpt.getOrElse(currentModel)
            ).asJson.noSpaces
          )
        )
        result
      case _ => result
    }
  }
}
