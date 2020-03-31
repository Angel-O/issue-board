package com.angelo.dashboard.styling

import com.angelo.dashboard.components.IssueCard.IssueCard.Style
import com.angelo.dashboard.CssSettings._
import com.angelo.dashboard.components.IssueCard.IssueCard
import com.angelo.dashboard.views.Dashboard
import com.angelo.dashboard.views.Home.Home
import org.scalajs.dom

object StyleInitializer {

  def initStyleSheets(): Unit = {

    val stylesSheets = List(
      Home.Style,
      Dashboard.Style,
      Style,
      GlobalStyles
    )

    stylesSheets.map(_.render(cssStringRenderer, implicitly)).foreach(dom.console.debug(_))
    stylesSheets.foreach(_.addToDocument())
  }
}
