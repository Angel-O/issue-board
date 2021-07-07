package com.angelo.dashboard.circuit

import com.angelo.dashboard.circuit.AppModel.IssuesLookup
import com.angelo.dashboard.Issue
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

import scala.collection.immutable._

case class AppModel(issuesLookup: IssuesLookup)

case object AppModel {

  private[circuit] type IssueId = String
  type IssuesLookup             = Map[IssueId, Issue]
  type IssuesIds                = List[IssueId]

  private[circuit] val init = AppModel(ListMap.empty)

  private[circuit] def issuesAsMap(issuesList: List[Issue]): IssuesLookup =
    ListMap(sortedByTitle(issuesList).map(issue => (issue.id, issue)): _*)

  private[this] def sortedByTitle(issuesList: List[Issue]): Seq[Issue] = issuesList.sortBy(_.title.toLowerCase)

  implicit class IssuesOps(lookup: IssuesLookup) {

    def +(issue: Issue): IssuesLookup =
      ListMap((lookup + (issue.id -> issue)).sortedByTitle: _*)

    def -(issue: Issue): IssuesLookup =
      ListMap((lookup - issue.id).sortedByTitle: _*)

    def filterIssues(p: Issue => Boolean): IssuesLookup = lookup.filter { case (_, issue) => p(issue) }

    def filterIssuesNot(p: Issue => Boolean): IssuesLookup = filterIssues(i => !p(i))

    def ids: List[IssueId] = lookup.keys.toList

    private[IssuesOps] def sortedByTitle =
      lookup.toSeq.sortBy {
        case (_, issue) => issue.title.toLowerCase
      }
  }

  // for debug purposes
  implicit val encoder: Encoder[AppModel] = deriveEncoder
}
