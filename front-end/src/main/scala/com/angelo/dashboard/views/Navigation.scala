package com.angelo.dashboard.views

import com.angelo.dashboard.routing.{DashBoardPage, FormPage, HomePage, Pages}
import com.angelo.dashboard.styling.GlobalStyles
import com.angelo.dashboard._
import com.angelo.dashboard.routing.{DashBoardPage, FormPage, HomePage, Pages}
import com.angelo.dashboard.styling.GlobalStyles
import com.angelo.dashboard.utils.Helpers
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import japgolly.scalajs.react.vdom.html_<^._

object Navigation {

  case class Props(router: RouterCtl[Pages], resolution: Resolution[Pages])

  def render(p: Props) = {

    val logo =
      <.div(
        GlobalStyles.theme.navbarBrand,
        p.router.link(HomePage)(
          GlobalStyles.theme.navbarItem,
          <.img(
            ^.alt := "logo",
            ^.src := "img/ib-logo-2.svg",
            ^.maxHeight := "3.5rem"
          )
        ),
      )

    def menuItems(labelledPages: (Pages, String)*) = {

      val items = labelledPages.toList.map {
        case (page, label) =>
          p.router.link(page)(
            GlobalStyles.theme.navbarItem,
            GlobalStyles.theme.isTab,
            GlobalStyles.theme.isActive(page == p.resolution.page),
            label
          )
      }

      Helpers.withKeys(items).toVdomArray
    }

    <.div(
      <.nav(
        GlobalStyles.theme.navbar,
        GlobalStyles.theme.isLight,
        ^.role := "navigation",
        logo,
        <.div(
          GlobalStyles.theme.navbarMenu,
          <.div(
            GlobalStyles.theme.navbarStart,
            menuItems((FormPage, "Create Issue"), (DashBoardPage, "View dashboard"))
          )
        )
      ),
      <.div(
        p.resolution.render()
      )
    )
  }

  private val component =
    ScalaComponent
      .builder[Props]("Navigation")
      .render_P(render)
      .build

  def apply(router: RouterCtl[Pages], resolution: Resolution[Pages]): Unmounted[Props, Unit, Unit] =
    component(Props(router, resolution))
}
