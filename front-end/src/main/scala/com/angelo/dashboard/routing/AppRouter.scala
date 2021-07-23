package com.angelo.dashboard.routing

import com.angelo.dashboard.AppConfig
import com.angelo.dashboard.routing.Pages.{DashBoardPage, FormPage, HomePage}
import com.angelo.dashboard.views.Home.Home
import com.angelo.dashboard.views._
import japgolly.scalajs.react.extra.router.StaticDsl.Route
import japgolly.scalajs.react.extra.router.{BaseUrl, Router, _}
import org.scalajs.dom

object AppRouter {

  def apply() = {

    val routerConfig: RouterConfig[Pages] = RouterConfigDsl[Pages].buildConfig { dsl =>
      import dsl._

      val home: Route[Unit]      = root
      val form: Route[Unit]      = "#form"
      val dashboard: Route[Unit] = "#dashboard"

      (emptyRule
        | staticRoute(home, HomePage) ~> render(Home())
        | staticRoute(form, FormPage) ~> renderR(router => Form(router))
        | staticRoute(dashboard, DashBoardPage) ~> render(Dashboard()))
        .notFound(redirectToPage(HomePage)(SetRouteVia.HistoryReplace))
        .renderWith(Navigation.apply)
    }

    val baseUrl: BaseUrl =
      dom.window.location.hostname match {
        case "localhost" | "127.0.0.1" | "0.0.0.0" =>
          BaseUrl.fromWindowUrl(_.takeWhile(_ != '#'))
        case _                                     =>
          BaseUrl.fromWindowOrigin / AppConfig().ui.rootPath //TODO doublecheck this is safe
      }

    Router(baseUrl, routerConfig).apply()
  }
}
