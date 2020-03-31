package com.angelo.dashboard.utils

import scalajs.js.Dynamic.global

// CAN'T BE USED in the browser
object FileUtils {

  val fs = global.require("fs")

  def rscPath(path: String): String = "src/main/resources/" + path

  def rsc(path: String): String = {

    def readFile(name: String): String = fs.readFileSync(name).toString.trim

    readFile(rscPath(path))
  }
}
