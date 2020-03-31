package com.angelo.dashboard

object EnvVars {

  import scala.scalajs.js
  import scala.scalajs.js.annotation.JSImport

  @js.native
  @JSImport("js/process.js", JSImport.Namespace)
  object process extends js.Object {
    def env: js.Dynamic = js.native
  }

  trait TreatAsMissing[A] {
    def when(a: A): Boolean
  }

  trait EnvVarConverter[B] {
    def convert(a: String): B
  }

  implicit val stringEnvVarTreatAsMissing: TreatAsMissing[String] = _.isEmpty
  implicit val stringEnvVarConverter: EnvVarConverter[String]     = identity _

  implicit val booleanEnvVarTreatAsMissing: TreatAsMissing[Boolean] = _ => false
  implicit val booleanEnvVarConverter: EnvVarConverter[Boolean]     = _.toBoolean

  def getOrDefault[T: TreatAsMissing: EnvVarConverter](envVar: js.Dynamic, default: T): T =
    envVar.asInstanceOf[js.UndefOr[String]].map(implicitly[EnvVarConverter[T]].convert).toOption match {
      case Some(v) if implicitly[TreatAsMissing[T]].when(v) => default
      case Some(v)                                          => v
      case None                                             => default
    }
}
