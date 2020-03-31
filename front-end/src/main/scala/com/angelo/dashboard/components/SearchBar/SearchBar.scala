package com.angelo.dashboard.components.SearchBar

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._
import com.angelo.dashboard._
import com.angelo.dashboard.hoc.InputFields
import com.angelo.dashboard.styling.GlobalStyles

object SearchBar {

  case class Props(handleChange: String => CallbackTo[Unit])

  def render(p: Props): VdomElement =
    InputFields.inputField(
      <.input(
        GlobalStyles.theme.input,
        ^.`type` := "input",
        ^.placeholder := "Search...",
        ^.onChange ==> ((e: ReactEventFromInput) => p.handleChange(e.target.value))
      )
    )

  private val component =
    ScalaComponent
      .builder[Props]("SearchBar")
      .render_P(p => render(p))
      .build

  def apply(onTextChange: String => CallbackTo[Unit]): Unmounted[Props, Unit, Unit] =
    component(Props(onTextChange))
}
