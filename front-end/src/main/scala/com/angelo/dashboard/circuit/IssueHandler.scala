package com.angelo.dashboard.circuit

import com.angelo.dashboard.circuit.IssueHandler.IssuesFetched
import diode._
import com.angelo.dashboard.Issue
import com.angelo.dashboard.api.ApiClient
import com.angelo.dashboard.circuit.AppModel._
import com.angelo.dashboard.circuit.IssueHandler._

import scala.concurrent.ExecutionContext.Implicits.global

class IssueHandler(modelRW: ModelRW[AppModel, IssuesLookup], apiClient: ApiClient) extends ActionHandler(modelRW) {

  override protected def handle = {
    case FetchIssues           => effectOnly(fetchIssuesEffect())
    case IssuesFetched(issues) => updated(issuesAsMap(issues))
    case CreateIssue(issue)    => updated(modelRW.value + issue, createIssueEffect(issue))
    case ArchiveIssue(issue)   => updated(modelRW.value + issue.copy(isArchived = true), archiveIssueEffect(issue))
    case DeleteIssue(issue)    => updated(modelRW.value - issue, deleteIssueEffect(issue))
  }

  private def fetchIssuesEffect() =
    Effect(apiClient.getIssues.unsafeToFuture().map(IssuesFetched)) //TODO change api to use future

  private def createIssueEffect(issue: Issue) =
    Effect(apiClient.postIssue(issue).unsafeToFuture().map(_ => NoAction))

  private def archiveIssueEffect(issue: Issue) =
    Effect(apiClient.archiveIssue(issue).unsafeToFuture().map(_ => NoAction))

  private def deleteIssueEffect(issue: Issue) =
    Effect(apiClient.deleteIssue(issue).unsafeToFuture().map(_ => NoAction))
}

object IssueHandler {
  case object FetchIssues                       extends Action
  case class IssuesFetched(issues: List[Issue]) extends Action
  case class CreateIssue(issue: Issue)          extends Action
  case class ArchiveIssue(issue: Issue)         extends Action
  case class DeleteIssue(issue: Issue)          extends Action
}
