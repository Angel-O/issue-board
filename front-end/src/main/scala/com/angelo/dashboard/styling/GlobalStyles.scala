package com.angelo.dashboard.styling

import com.angelo.dashboard.CssSettings._
import scalacss.internal.DslBase.{ToStyle, MediaQueryEmpty => media} //this makes intellij happy

import scala.concurrent.duration._

object GlobalStyles extends StyleSheet.Inline {
  import dsl.{media => _, _} //this makes intellij happy

  style(unsafeRoot("body")(paddingTop(10.px)))
  style(unsafeRoot("img")(width(100.%%)))

  val theme   = new BulmaStyles
  val icons   = FontAwesomeIcons
  val scss    = Scss
  val noStyle = theme.empty

  def smallDevices(t: ToStyle) = media.maxWidth(560.px)(style(t: ToStyle))
  def blur(px: Int)            = applyFilter(s"blur(${px}px)")
  def transitionAll(ms: Int)   = applyTransition("all", ms)

  def applyFilter(filterDesc: String) = styleS(filter := filterDesc)

  def applyTransition(prop: String, ms: Int) = styleS(transitionProperty := s"$prop", transitionDuration(ms.millis))

  //TODO explore mixins
//  val blurTransition = mixin(applyTransition("blur", 1000), transitionTimingFunction.easeOut)
}
