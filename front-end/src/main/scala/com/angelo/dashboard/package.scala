package com.angelo

import scalacss.ScalaCssReactImplicits

//TODO move to styling package
package object dashboard extends ScalaCssReactImplicits {
  val CssSettings = scalacss.devOrProdDefaults
}
