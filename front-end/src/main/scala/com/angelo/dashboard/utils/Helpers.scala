package com.angelo.dashboard.utils

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.vdom.{TagOf, TopNode}

object Helpers {

  def withKeys[A <: TopNode](elems: Iterable[TagOf[A]]): Iterable[VdomTagOf[A]] =
    elems.zipWithIndex.map { case (e, idx) => e(^.key := idx) }
}
