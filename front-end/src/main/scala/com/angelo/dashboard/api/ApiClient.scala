package com.angelo.dashboard.api

import com.angelo.dashboard.AppConfig.BackEndConfig
import com.angelo.dashboard.Issue
import japgolly.scalajs.react.AsyncCallback

class ApiClient private (baseUrl: String) {

  def getIssues: AsyncCallback[List[Issue]]           = Api.GET[List[Issue]](s"$baseUrl/issues")
  def postIssue(issue: Issue): AsyncCallback[Unit]    = Api.POST(s"$baseUrl/issues", issue)
  def archiveIssue(issue: Issue): AsyncCallback[Unit] = Api.PATCH(s"$baseUrl/issues", issue)
  def deleteIssue(issue: Issue): AsyncCallback[Unit]  = Api.DELETE(s"$baseUrl/issues/${issue.id}")
}

object ApiClient {
  def apply(config: BackEndConfig) = new ApiClient(s"${config.basePath}/${config.path}")
}
