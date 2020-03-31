package com.angelo.dashboard.views.Home

import scalacss.internal.StyleA

// Home page column ADT
object Column {

  sealed abstract class Column(val icon: Option[StyleA], val title: Option[String], val content: Option[String])

  /*  I (icon)
   *  T (title)
   *  C (content)
   *  _ (either I, T, or C are absent)
   * */
  case class ColumnITC(i: StyleA, t: String, c: String) extends Column(Some(i), Some(t), Some(c))
  case class ColumnI__(i: StyleA)                       extends Column(Some(i), None, None)
  case class Column_TC(t: String, c: String)            extends Column(None, Some(t), Some(c))
  case class Column___()                                extends Column(None, None, None)
}
