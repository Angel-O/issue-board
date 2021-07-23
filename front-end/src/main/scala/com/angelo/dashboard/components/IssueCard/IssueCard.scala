package com.angelo.dashboard.components.IssueCard

import com.angelo.dashboard.CssSettings._
import com.angelo.dashboard.styling.GlobalStyles
import com.angelo.dashboard.{Issue, _}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scalacss.internal.mutable.StyleSheet

object IssueCard {

  object Style extends StyleSheet.Inline {

    import dsl._

    val opaque = styleF.bool(isArchived =>
      styleS(
        GlobalStyles.applyTransition(opacity.attr.id, 1000),
        transitionTimingFunction.easeIn,
        if (isArchived) opacity(0.5)
        else opacity(1)
      )
    )

    val issueBox = style(
      cursor.pointer,
      height(20.em)
    )
  }

  case class Props(issue: Issue, onArchive: Callback, onDelete: Callback)

  case class State(modalIsOpen: Boolean)

  object State {
    val init = State(modalIsOpen = false)
  }

  class Backend(bs: BackendScope[Props, State]) {

    def closeModal: Callback = bs.setState(State(modalIsOpen = false))

    def openModal: Callback = bs.setState(State(modalIsOpen = true))

    def onSubmit: Callback = Callback.empty

    def handleArchive: Callback = (bs.props >>= (_.onArchive)) >> closeModal

    def handleDelete: Callback = (bs.props >>= (_.onDelete)) >> closeModal

    def render(p: Props, s: State): VdomElement = {

      val card =
        <.div(
          GlobalStyles.theme.box,
          Style.opaque(p.issue.isArchived),
          Style.issueBox,
          ^.onClick --> openModal, //TODO create toggle + create card component...
          <.article(
            <.div(
              GlobalStyles.theme.mediaContent,
              <.div(
                GlobalStyles.theme.content,
                <.p(
                  <.strong(p.issue.title),
                  <.br,
                  p.issue.content
                )
              )
            )
          )
        )

      <.div(
        card,
        IssueModalCard(p.issue, s.modalIsOpen, onSubmit, handleArchive, handleDelete, closeModal)
      )
    }
  }

  val component = ScalaComponent
    .builder[Props]("IssueCard")
    .initialState(State.init)
    .renderBackend[Backend]
    .build

  def apply(issue: Issue, onArchive: Callback, onDelete: Callback) =
    component(Props(issue, onArchive, onDelete))
}
