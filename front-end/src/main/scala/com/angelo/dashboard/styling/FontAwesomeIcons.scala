package com.angelo.dashboard.styling

import com.angelo.dashboard.CssSettings._

private[styling] object FontAwesomeIcons extends StyleSheet.Inline {

  import dsl._

  // fontawesome social
  def faIconB(icon: String, size: Int): StyleA = style(addClassName(s"fab fa-$icon fa-${size}x"))

  // fontawesome rest
  def faIconS(icon: String, size: Int): StyleA = style(addClassName(s"fas fa-$icon fa-${size}x"))
}
