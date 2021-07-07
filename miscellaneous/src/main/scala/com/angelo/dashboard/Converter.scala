package com.angelo.dashboard

import java.io.File

import scala.xml.{Elem, Node, Text, XML}

/**
 * HERO HERE ===> https://gist.github.com/nafg/112bf83e5676ed316f17cea505ea5d93
 *
 * Converts an html file into its equivalent scala-react-tags.
 * To run it from Intellij edit the run configuration pointing the working directory
 * to the directory this file is in (alternatively you can edit the file path specified in the run method
 * such that it is relative to the default working directory (which should be root of the multi-project))
 */
object Converter extends App {

  def quoteString(s: String) =
    s""""${s.replace("\n", "\\n").replace("\"", "\\\"")}""""

  def indentSpaces(n: Int) = " " * (n * 2)

  def toVDOM(node: Node, indentLevel: Int = 0): Option[String] =
    node match {
      case elem: Elem =>
        val attrArgs  =
          elem.attributes.asAttrMap.map {
            case (key, value) =>
              val unsupported = Set("label", "selectedindex", "tabindex", "onclick", "onchange", "style", "onkeyup")
              val attr        =
                key.toLowerCase match {
                  case s if s.contains("-") || unsupported.contains(s) => "VdomAttr(" + quoteString(s) + ")"
                  case "class"                                         => "^.cls"
                  case "for"                                           => "^.htmlFor"
                  case "type"                                          => "^.tpe"
                  case _                                               => "^." + key
                }
              attr + " := " + quoteString(value)
          }.toSeq
        val childArgs = elem.child.map(toVDOM(_, indentLevel + 1))
        val res       =
          "<." + elem.label +
            "(\n" + indentSpaces(indentLevel + 1) +
            (attrArgs ++ childArgs.flatten).mkString(",\n" + indentSpaces(indentLevel + 1)) +
            "\n" + indentSpaces(indentLevel) + ")"
        Some(res)

      case text: Text =>
        if ((text.data: String).trim.isEmpty) None
        else Some(quoteString((text.data: String).trim))
    }

  def run(path: String): Unit =
    Some(path).flatMap { path =>
      val file      = new File(path)
      val converted = toVDOM(XML.loadFile(file))
      converted
    }.foreach(println)

  run("miscellaneous/src/main/scala/com/angelo/dashboard/source.html")

}
