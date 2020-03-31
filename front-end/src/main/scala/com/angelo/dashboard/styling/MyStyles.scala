package com.angelo.dashboard.styling

import com.angelo.dashboard.CssSettings._
import scalacss.internal.mutable.StyleSheet

// Standalone sheet test (not used atm)
object MyStyles extends StyleSheet.Standalone {

  import dsl._

  "div.std" - (
    margin(12 px, auto),
    textAlign.left,
    cursor.pointer,
    &.hover -
      cursor.zoomIn,
    media.not.handheld.landscape.maxWidth(640 px) -
      width(400 px),
    &("span") -
      color.red
  )

  "h1".firstChild -
    fontWeight.bold

  for (i <- 0 to 3)
    s".indent-$i" -
      paddingLeft(i * 2.ex)
}
