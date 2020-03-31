package com.angelo.dashboard.styling

import com.angelo.dashboard.CssSettings._

private[styling] class BulmaStyles(implicit r: StyleSheet.Register) extends StyleSheet.Inline()(r) {

  import dsl._

  implicit class StyleAOps(s: StyleS) {

    def -(modifier: StyleA) =
      style(addClassName(s"${s.className.get}-${modifier.className}"))
  }

  private val gridDomain = Domain.ofRange(1 to 12)

  //Form styles
  val control      = style(addClassName("control"))
  val label        = style(addClassName("label"))
  val field        = style(addClassName("field"))
  val groupedField = field + style(addClassName("is-grouped"))
  val input        = style(addClassName("input"))
  val textArea     = style(addClassName("textarea"))
  val button       = style(addClassName("button"))
  val radio        = style(addClassName("radio"))

  //Modifiers
  val isLink       = style(addClassName("is-link"))
  val isLarge      = style(addClassName("is-large"))
  val isLight      = style(addClassName("is-light"))
  val isDanger     = style(addClassName("is-danger"))
  val isWarning    = style(addClassName("is-warning"))
  val isTab        = style(addClassName("is-tab"))
  val isSpaced     = style(addClassName("is-spaced"))
  val isVCentered  = style(addClassName("is-vcentered"))
  val isCentered   = style(addClassName("is-centered"))
  val isSuccess    = style(addClassName("is-success"))
  val isExpanded   = style(addClassName("is-expanded"))
  val isOneQuarter = style(addClassName("is-one-quarter"))
  val isMultiline  = style(addClassName("is-multiline"))
  val isFluid      = style(addClassName("is-fluid"))
  val isFourFifths = style(addClassName("is-four-fifths"))

  //Layout
  val container    = style(addClassNames("container"))
  val notification = style(addClassNames("notification"))

  //Elements
  val box          = style(addClassNames("box"))
  val mediaContent = style(addClassNames("media-content"))
  val content      = style(addClassNames("content"))

  //Typography
  val title = style(addClassName("title"))

  //Miscellaneous
  val help   = style(addClassName("help"))
  val delete = style(addClassName("delete"))
  val empty  = styleS()

  //Navigation
  val navbar      = style(addClassName("navbar"))
  val navbarMenu  = style(addClassName("navbar-menu"))
  val navbarBrand = style(addClassName("navbar-brand"))
  val navbarItem  = style(addClassName("navbar-item"))
  val navbarStart = style(addClassName("navbar-start"))

  //columns
  val columns = style(addClassName("columns"))
  val column  = style(addClassName("column"))

  //modal
  val modal           = style(addClassName("modal"))
  val modalBackground = style(addClassName("modal-background"))
  val modalContent    = style(addClassName("modal-content"))
  val modalClose      = style(addClassName("modal-close"))
  val modalCard       = style(addClassName("modal-card"))
  val modalCardBody   = style(addClassName("modal-card-body"))
  val modalCardHead   = style(addClassName("modal-card-head"))
  val modalCardTitle  = style(addClassName("modal-card-title"))
  val modalCardFoot   = style(addClassName("modal-card-foot"))

  // Conditional modifiers (defined at the bottom to avoid forward reference: it was causing performance issues)
  val isActive: Boolean => StyleA =
    styleF.bool(
      active => if (active) styleS(addClassName("is-active")) else empty
    )

  val isX: Int => StyleA = styleF(gridDomain)(index => styleS(addClassName(s"is-$index")))
}
