package com.angelo.dashboard.routing

sealed trait Pages

object Pages {
  case object HomePage      extends Pages
  case object FormPage      extends Pages
  case object DashBoardPage extends Pages
}
