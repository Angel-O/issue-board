package com.angelo.dashboard

import com.angelo.dashboard.circuit.AppCircuit
import com.angelo.dashboard.middleware.LoggingProcessor
import com.angelo.dashboard.routing.AppRouter
import com.angelo.dashboard.styling.{GlobalStyles, StyleInitializer}
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document

object Main extends App {

  AppCircuit.addProcessor(LoggingProcessor(AppConfig.loggingConfig.enableLogging))

  StyleInitializer.initStyleSheets()

  val app = ScalaComponent
    .builder[Unit]("App")
    .renderStatic(
      <.div(
        GlobalStyles.theme.container,
        AppRouter()
      )
    )
    .build

  app().renderIntoDOM(document.getElementById("app"))
}
