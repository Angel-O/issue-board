package com.angelo.dashboard.circuit

import diode._
import diode.react.ReactConnector
import com.angelo.dashboard.AppConfig
import com.angelo.dashboard.api.ApiClient

object AppCircuit extends Circuit[AppModel] with ReactConnector[AppModel] {

  val apiClient = ApiClient(AppConfig().backEnd)

  val issueHandler = new IssueHandler(zoomTo(_.issuesLookup), apiClient)

  override protected def initialModel: AppModel = AppModel.init

  override protected def actionHandler: AppCircuit.HandlerFunction = composeHandlers(issueHandler)
}
