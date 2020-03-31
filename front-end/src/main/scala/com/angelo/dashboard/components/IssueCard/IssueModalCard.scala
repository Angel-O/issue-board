package com.angelo.dashboard.components.IssueCard

import com.angelo.dashboard.Issue
import com.angelo.dashboard.hoc.ModalCard
import com.angelo.dashboard.styling.GlobalStyles
import com.angelo.dashboard.utils.Helpers
import com.angelo.dashboard.{Issue, _}
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._

object IssueModalCard {

  case class Props(
    issue: Issue,
    modalIsOpen: Boolean,
    onSubmit: Callback,
    onArchive: Callback,
    onDelete: Callback,
    onClose: Callback
  )

  def render(p: Props) = {

    val actionButtons = Helpers
      .withKeys(
        List(
          //TODO coming next
//          <.button(
//            GlobalStyles.theme.button,
//            GlobalStyles.theme.isSuccess,
//            "save changes",
//            ^.disabled := true,
//            ^.onClick --> p.onSubmit
//          ),
          <.button(
            GlobalStyles.theme.button,
            GlobalStyles.theme.isWarning,
            "archive",
            ^.disabled := p.issue.isArchived,
            ^.onClick --> p.onArchive
          ),
          <.button(
            GlobalStyles.theme.button,
            GlobalStyles.theme.isDanger,
            "delete",
            ^.disabled := false,
            ^.onClick --> p.onDelete
          )
        )
      )
      .toVdomArray

    ModalCard(
      p.modalIsOpen,
      p.issue.title,
      ModalContent(p.issue),
      p.onClose,
      actionButtons
    )
  }

  val component = ScalaComponent
    .builder[Props]("IssueModalCard")
    .render_P(p => render(p))
    .build

  def apply(
    issue: Issue,
    modalIsOpen: Boolean,
    onSubmit: Callback,
    onArchive: Callback,
    onDelete: Callback,
    onClose: Callback
  ): Unmounted[Props, Unit, Unit] =
    component(Props(issue, modalIsOpen, onSubmit, onArchive, onDelete, onClose))
}
