package com.angelo.dashboard.components.IssueCard

import com.angelo.dashboard.Issue
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import java.time.format.DateTimeFormatter

object ModalContent {

  final case class Props(issue: Issue)

  private def formatDate(issue: Issue) =
    issue.lifeCycle.createdAt.toLocalDate.format(DateTimeFormatter.ofPattern("d MMM uuuu"))

  def render(p: Props): VdomElement =
    <.div(
      <.ul(
        <.li(
          s"date created: ${formatDate(p.issue)}"
        ),
        <.li(s"content: ${p.issue.content}")
      )
    )

  val component = ScalaComponent
    .builder[Props]("ModalContent")
    .render_P(p => render(p))
    .build

  def apply(issue: Issue) = component(Props(issue))
}
