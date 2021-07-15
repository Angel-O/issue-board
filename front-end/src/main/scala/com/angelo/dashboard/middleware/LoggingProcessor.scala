package com.angelo.dashboard.middleware

import java.time.LocalTime

import diode.{ActionProcessor, ActionResult, Dispatcher, NoAction}
import com.angelo.dashboard.circuit.AppModel
import io.circe.syntax._
import org.scalajs.dom

import scala.scalajs.js.JSON

class LoggingProcessor private (enableLogging: Boolean) extends ActionProcessor[AppModel] {

  override def process(
    dispatch: Dispatcher,
    action: Any,
    next: Any => ActionResult[AppModel],
    currentModel: AppModel
  ): ActionResult[AppModel] = {

    val result = next(action)

    action match {
      case NoAction => result
      case _        => if (enableLogging) logAction(action, currentModel, result); result
    }
  }

  private def logAction(action: Any, currentModel: AppModel, result: ActionResult[AppModel]): Unit =
    dom.console.log(
      LoggingProcessor.padded(s"%cAction ${action.getClass.getSimpleName} @ ${LocalTime.now()}") + "%o",
      "color:cyan",
      JSON.parse(ModelUpdate(currentModel, result.newModelOpt.getOrElse(currentModel)).asJson.noSpaces)
    )
}

object LoggingProcessor {
  def apply(enableLogging: Boolean) = new LoggingProcessor(enableLogging)

  private def padded(str: String): String = str.padTo(40, " ").mkString
}
