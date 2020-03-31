package com.angelo.dashboard.hoc

import com.angelo.dashboard.styling.GlobalStyles
import com.angelo.dashboard._
import com.angelo.dashboard.styling.GlobalStyles
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object ModalCard {

  case class Props(
    isOpen: Boolean,
    title: String,
    content: VdomElement,
    onClose: Callback,
    actionButtons: VdomArray
  )

  def render(p: Props) =
    <.div(
      GlobalStyles.theme.modal,
      GlobalStyles.theme.isActive(p.isOpen), //make it dry
      <.div(
        GlobalStyles.theme.modalBackground,
        ^.onClick --> p.onClose
      ),
      <.div(
        GlobalStyles.theme.modalCard,
        <.header(
          GlobalStyles.theme.modalCardHead,
          <.p(GlobalStyles.theme.modalCardTitle, p.title),
          <.button(GlobalStyles.theme.delete, ^.onClick --> p.onClose)
        ),
        <.section(
          ^.height := "20em", //TODO pass via props
          GlobalStyles.theme.modalCardBody,
          p.content
        ),
        <.footer(
          GlobalStyles.theme.modalCardFoot,
          <.div(
            ^.display := "flex",
            ^.justifyContent := "flex-end",
            ^.width := "100%",
            p.actionButtons
          )
        )
      )
    )

  val component = ScalaComponent
    .builder[Props]("ModalCard")
    .render_P(render)
    .build

  def apply(
    isOpen: Boolean,
    title: String,
    content: VdomElement,
    onClose: Callback,
    actionButtons: VdomArray
  ) =
    component(
      Props(
        isOpen,
        title,
        content,
        onClose,
        actionButtons
      )
    )
}
