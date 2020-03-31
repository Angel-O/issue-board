package com.angelo.dashboard.views.Home

import com.angelo.dashboard.styling.GlobalStyles
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.component.Scala.Unmounted
import com.angelo.dashboard.CssSettings._
import com.angelo.dashboard._
import com.angelo.dashboard.styling.GlobalStyles
import com.angelo.dashboard.utils.Helpers
import com.angelo.dashboard.views.Home.Column._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.html.Element
import scalacss.internal.mutable.StyleSheet

object Home {

  //TODO use colors from scss file
  object Style extends StyleSheet.Inline {
    import dsl._

    private val desktopHeight   = 100.vh
    private val viewHeight      = styleS(height(desktopHeight))
    private val smallViewHeight = styleS(height(desktopHeight / 2))
    private val light           = Color(GlobalStyles.scss.beigeLighter)
    private val dark            = c"#333"
    private val lighter         = c"#fff"

    val container = style(
      backgroundColor.rgba(0, 0, 0, 0.9),
      margin(0.px),
      color(lighter),
      textAlign.center
    )

    val contentArea = style(
      position.absolute,
      display.flex,
      justifyContent.center,
      alignItems.center,
      width(100.%%),
      zIndex(1),
      viewHeight,
      GlobalStyles.smallDevices(smallViewHeight)
    )

    val showcase = style(
      &.after(
        content := "''",
        viewHeight,
        display.block,
        width(100.%%),
        backgroundImage := "url(img/home-bg.jpg)",
        backgroundSize := "cover",
        backgroundPosition := "center",
        backgroundRepeat.noRepeat,
        GlobalStyles.blur(10),
        GlobalStyles.transitionAll(1000),
        GlobalStyles.smallDevices(smallViewHeight),
        &.hover(GlobalStyles.blur(0))
      )
    )

    val banner = style(
      display.flex,
      flexDirection.column,
      justifyContent.center,
      alignItems.center,
      width(100.%%),
      height(100.%%),
      overflow.hidden,
      &.hover(
        GlobalStyles.blur(2),
        GlobalStyles.transitionAll(1500)
      ),
      &.not(_.hover)(
        GlobalStyles.transitionAll(2000)
      )
    )

    val logo = style(
      height(10.%%)
    )

    val logoTitle = style(
      lineHeight(2.5),
      marginTop(1.rem),
      GlobalStyles.smallDevices(display.none)
    )

    val section = style(
      margin.auto,
      padding(4.em, 1.em),
      overflow.hidden
    )

    val lightSection = style(
      section,
      backgroundColor(light),
      color(dark)
    )

    val columnWrap = style(
      maxWidth(960.px),
      margin.auto
    )

    val titleStyle = styleF.bool(
      darkBackground =>
        styleS(
          GlobalStyles.theme.isX(3),
          if (darkBackground) styleS(color(light))
          else styleS(color(dark))
        )
    )

    val alignLastToLeftWhen =
      styleF.bool(
        toLeft => styleS(if (toLeft) &.lastChild(textAlign.left) else GlobalStyles.noStyle)
      )

    val iconStyle = style(
      &.not(_.lastChild)(marginBottom(0.5.em))
    )

    val footer = style(
      padding(2.2.rem),
      &.nthChild(1)(margin(0.px))
    )
  }

  private def sectionBuilder(
    alternate: Boolean,
    style: StyleA,
    columns: Column*
  ): VdomTagOf[Element] =
    <.section(
      style,
      <.div(
        GlobalStyles.theme.columns,
        GlobalStyles.theme.isVCentered,
        Style.columnWrap,
        Helpers
          .withKeys(
            columns.map { col =>
              import col._
              <.div(
                GlobalStyles.theme.column,
                GlobalStyles.theme.isX(12 / columns.size),
                Style.alignLastToLeftWhen(alternate),
                icon.map(<.i(Style.iconStyle, _)),
                title.map(<.h3(GlobalStyles.theme.title, Style.titleStyle(!alternate), _)),
                content.map(<.p(_))
              )
            }
          )
          .toVdomArray
      )
    )

  private val layout =
    <.div(
      Style.container,
      <.header(
        Style.showcase,
        <.div(
          Style.contentArea,
          <.div(
            Style.banner,
            <.img(Style.logo, ^.alt := "logo", ^.width := "40%", ^.src := "img/ib-logo.svg"),
            <.div(Style.logoTitle, Content.logoTitle)
          )
        )
      ),
      sectionBuilder(
        false,
        Style.section,
        ColumnITC(GlobalStyles.icons.faIconB("youtube", 3), "YouTube", Content.youtube),
        ColumnITC(GlobalStyles.icons.faIconS("chalkboard-teacher", 3), "Courses", Content.courses),
        ColumnITC(GlobalStyles.icons.faIconS("briefcase", 3), "Projects", Content.projects)
      ),
      sectionBuilder(
        true,
        Style.lightSection,
        ColumnI__(GlobalStyles.icons.faIconS("laptop-code", 10)),
        Column_TC("About us", Content.aboutUs)
      ),
      <.footer(
        Style.footer,
        <.p(Content.copyRight)
      )
    )

  private val component =
    ScalaComponent
      .builder[Unit]("Home")
      .renderStatic(layout)
      .build

  def apply(): Unmounted[Unit, Unit, Unit] = component()
}
