package com.angelo.dashboard.views

import com.angelo.dashboard.circuit.AppCircuit
import com.angelo.dashboard.circuit.IssueHandler.CreateIssue
import com.angelo.dashboard.hoc.InputFields.{groupedInputField, inputField}
import com.angelo.dashboard.routing.{DashBoardPage, Pages}
import com.angelo.dashboard.{Issue, _}
import japgolly.scalajs.react.component.Scala.{BackendScope, Unmounted}
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ReactEventFromInput, ScalaComponent}
import cats.implicits._
import com.angelo.dashboard.Issue
import com.angelo.dashboard.routing.{DashBoardPage, Pages}
import com.angelo.dashboard.styling.GlobalStyles
import com.angelo.dashboard.styling.GlobalStyles

object Form {

  case class Props(router: RouterCtl[Pages], onSubmit: Issue => Callback)
  case class State(title: Option[String], content: Option[String])

  object State {
    val init = State(None, None)
  }

  object Backend {

    private def onTextChange(bs: BackendScope[Props, State], stateChange: State => State): Callback =
      bs.modState(stateChange)

    private def stateToIssue(s: State): Option[Issue] = s.title >>= (title => s.content.map(Issue(title, _)))

    //TODO create validation module
    private def withError(textField: Option[String], label: String) =
      textField.collect {
        case s if s.trim.isEmpty => <.p(GlobalStyles.theme.help, GlobalStyles.theme.isDanger, s"Invalid $label")
      }

  }

  case class Backend(bs: BackendScope[Props, State]) {
    import Backend._

    def handleTextBoxChange(newValue: String): Callback = onTextChange(bs, _.copy(title = Some(newValue)))

    def handleTextAreaChange(newValue: String): Callback = onTextChange(bs, _.copy(content = Some(newValue)))

    def canSubmit(s: State): Boolean = s.title.exists(_.nonEmpty) && s.content.exists(_.nonEmpty)

    def clearForm: Callback = bs.setState(State.init)

    def render(p: Props, s: State) = {

      val titleTextBox = inputField(
        <.input.text(
          GlobalStyles.theme.input,
          ^.`type` := "text",
          ^.placeholder := "Title",
          ^.value := s.title.getOrElse(""),
          ^.onChange ==> ((e: ReactEventFromInput) => handleTextBoxChange(e.target.value))
        ),
        maybeError = withError(s.title, "title")
      )

      val textArea = inputField(
        <.textarea(
          GlobalStyles.theme.textArea,
          ^.placeholder := "Message",
          ^.value := s.content.getOrElse(""),
          ^.rows := 25,
          ^.onChange ==> ((e: ReactEventFromInput) => handleTextAreaChange(e.target.value))
        ),
        maybeError = withError(s.content, "message")
      )

      val buttons = groupedInputField(
        <.button(
          GlobalStyles.theme.button,
          GlobalStyles.theme.isLink,
          ^.disabled := !canSubmit(s),
          ^.onClick -->? stateToIssue(s).map(issue => p.router.set(DashBoardPage) >> p.onSubmit(issue)),
          "Submit"
        ),
        <.button(
          GlobalStyles.theme.button,
          GlobalStyles.theme.isLink,
          GlobalStyles.theme.isLight,
          ^.onClick --> clearForm,
          "Cancel"
        )
      )

      <.div(
        GlobalStyles.theme.box,
        <.div(
          GlobalStyles.theme.container,
          <.div(
            GlobalStyles.theme.notification,
            titleTextBox,
            textArea,
            buttons
          )
        )
      )
    }
  }

  private val component =
    ScalaComponent
      .builder[Props]("Form")
      .initialState(State.init)
      .renderBackend[Backend]
      .build

  def apply(router: RouterCtl[Pages]): Unmounted[Props, State, Backend] =
    AppCircuit.wrap(_.issuesLookup)(
      p => component(Props(router, issue => p.dispatchCB(CreateIssue(issue))))
    )
}
