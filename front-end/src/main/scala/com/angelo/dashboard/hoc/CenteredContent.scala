package com.angelo.dashboard.hoc

import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._

// Not used atm
object CenteredContent {

  case class Props(message: String)

  private def render(p: Props) =
    <.div(
      ^.display.flex,
      ^.alignItems.stretch,
      ^.justifyContent.center,
      ^.height := "100%",
      <.div(
        <.h1(p.message),
        <.a(
          ^.`class` := "twitter"
        )
      ),
    )

  private val component =
    ScalaComponent
      .builder[Props]("CenteredContent")
      .render_P(render)
      .build

  def apply(message: String): Unmounted[Props, Unit, Unit] = component(Props(message))
}
