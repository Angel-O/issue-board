package com.angelo.dashboard.views

import com.angelo.dashboard.CssSettings._
import com.angelo.dashboard.circuit.AppCircuit
import com.angelo.dashboard.circuit.AppModel._
import com.angelo.dashboard.circuit.IssueHandler.{ArchiveIssue, DeleteIssue, FetchIssues}
import com.angelo.dashboard.components.IssueCard.IssueCard
import com.angelo.dashboard.components.SearchBar.SearchBar
import com.angelo.dashboard.styling.GlobalStyles
import com.angelo.dashboard.utils.Helpers
import com.angelo.dashboard.views.Dashboard.State.State
import com.angelo.dashboard.{Issue, _}
import diode.react.ModelProxy
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import scalacss.internal.mutable.StyleSheet

object Dashboard {

  object Style extends StyleSheet.Inline {
    import dsl._

    val labelContainer = style(
      display.flex,
      alignItems.center,
      width(30.%%)
    )

    val label = style(
      display.flex,
      justifyContent.flexStart,
      alignItems.center,
      width(50.%%)
    )

    val topBarWrap = style(
      paddingTop(2.em),
      paddingBottom(1.em)
    )

    val radioButtonsWrap = style(
      display.flex,
      justifyContent.spaceEvenly
    )

    val radioInputWrap = style(
      paddingRight(0.5.em)
    )
  }

  private type Props = ModelProxy[IssuesLookup]

  object State {

    case class State private (filteredIssuesIds: Option[IssuesIds]) {
      def underlyingIssuesIds: IssuesIds = filteredIssuesIds.getOrElse(Nil)
      def needsFetching: Boolean         = filteredIssuesIds.isEmpty
    }

    def applyUnsafe(issuesIds: IssuesIds): State =
      issuesIds match {
        case Nil => State(None)
        case _   => State(Some(issuesIds))
      }

    //use when you don't want the justMounted flag to change even when the IssuesIds list is empty
    def applySafe(issuesIds: IssuesIds): State = State(Some(issuesIds))
  }

  object Backend {

    private def shouldBeDisplayed(text: String)(issue: Issue): Boolean =
      issue.title.toLowerCase.contains(text) || issue.content.toLowerCase.contains(text)
  }

  case class Backend(bs: BackendScope[Props, State]) {

    import Backend._

    def render(s: State, p: Props): VdomNode = {

      def onFilter: String => Callback =
        text => bs.setState(State.applySafe(p.value.filterIssues(shouldBeDisplayed(text.trim.toLowerCase)).ids))

      val issuesCards = Helpers
        .withKeys(s.underlyingIssuesIds.flatMap { id =>
          p.value.get(id).map { issue =>
            <.div(
              GlobalStyles.theme.column,
              GlobalStyles.theme.isOneQuarter,
              <.div(
                IssueCard(issue, p.dispatchCB(ArchiveIssue(issue)), p.dispatchCB(DeleteIssue(issue)))
              )
            )
          }

        })
        .toVdomArray

      def showArchived: Callback = bs.setState(State.applySafe(p.value.filterIssues(_.isArchived).ids))
      def showActive: Callback   = bs.setState(State.applySafe(p.value.filterIssuesNot(_.isArchived).ids))
      def showAll: Callback      = bs.setState(State.applySafe(p.value.ids))

      def radioButtons(labelledActions: (String, Callback)*) =
        Helpers
          .withKeys(labelledActions.toList.map {
            case (label, action) =>
              <.div(
                Style.labelContainer,
                <.label(
                  Style.label,
                  ^.onClick --> action,
                  GlobalStyles.theme.radio,
                  <.div(
                    Style.radioInputWrap,
                    <.input(
                      ^.`type` := "radio",
                      ^.name := "active-status",
                    )
                  ),
                  <.div(label),
                )
              )
          })
          .toVdomArray

      <.div(
        <.div(
          GlobalStyles.theme.columns,
          Style.topBarWrap,
          <.div(
            GlobalStyles.theme.column,
            GlobalStyles.theme.isFourFifths,
            SearchBar(onFilter)
          ),
          <.div(
            GlobalStyles.theme.column,
            Style.radioButtonsWrap,
            radioButtons(("all", showAll), ("active", showActive), ("archived", showArchived))
          )
        ),
        <.div(
          GlobalStyles.theme.columns,
          GlobalStyles.theme.isMultiline,
          issuesCards
        )
      )
    }
  }

  private val dashboard =
    ScalaComponent
      .builder[Props]("Dashboard")
      .initialStateFromProps(p => State.applyUnsafe(p.value.ids))
      .renderBackend[Backend]
      .componentDidMount(comp => comp.props.dispatchCB(FetchIssues).when_(comp.state.needsFetching))
      .componentDidUpdate(comp =>
        comp.setState(State.applySafe(comp.currentProps.value.ids)).when_(comp.currentState.needsFetching)
      )
      .build

  def apply() =
    AppCircuit.connect(_.issuesLookup).apply(dashboard(_))

}
