package com.angelo.dashboard.hoc

import com.angelo.dashboard._
import com.angelo.dashboard.styling.GlobalStyles
import com.angelo.dashboard.utils.Helpers
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._

object InputFields {

  def inputField(
    input: VdomElement,
    maybeLabel: Option[String] = None,
    maybeError: Option[VdomElement] = None
  ) =
    <.div(
      GlobalStyles.theme.field,
      maybeLabel.map(<.label(GlobalStyles.theme.label, _)),
      <.div(
        GlobalStyles.theme.control,
        input
      ),
      maybeError
    )

  def groupedInputField(inputs: VdomElement*) =
    <.div(
      GlobalStyles.theme.groupedField,
      Helpers.withKeys(inputs.toList.map(<.div(GlobalStyles.theme.control, _))).toVdomArray
    )
}
