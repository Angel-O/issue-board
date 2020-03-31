object Commands {

  import CommandHelpers._
  import ScopedCommand._

  lazy val BackendClean: ScopedCommand    = ScopedCommand("backEnd", "clean" :: Nil)
  lazy val RunDb: ScopedCommand           = ScopedCommand("backEnd", "runDb" :: Nil)
  lazy val ServerHotReload: ScopedCommand = BackendClean + RunDb + ScopedCommand("backEnd", "reStart" :: Nil).continuous
  lazy val ServerAndDbRun: ScopedCommand  = BackendClean + RunDb + ScopedCommand("backEnd", "run" :: Nil)
  lazy val UiHotReload: ScopedCommand     = (StopStartWDS + "fastOptJS::webpack").reloaded.continuous
  lazy val UiBuild: ScopedCommand         = ScopedCommand("frontEnd", "clean" :: "fastOptJS::webpack" :: Nil)
  lazy val DevMode: ScopedCommand         = ScopedCommand("devMode" :: Nil).continuous
  lazy val StartWDS: ScopedCommand        = ScopedCommand("frontEnd", "fastOptJS::startWebpackDevServer" :: Nil)
  lazy val StopWDS: ScopedCommand         = ScopedCommand("frontEnd", "fastOptJS::stopWebpackDevServer" :: Nil)
  lazy val StopStartWDS: ScopedCommand    = StopWDS + StartWDS
  lazy val TearDown: ScopedCommand        = StopWDS + ScopedCommand("backEnd", "killDb" :: "reStop" :: Nil)

  object CommandHelpers {

    object ScopedCommand {

      trait Concat[A] {
        def +(other: A)(implicit self: ScopedCommand): ScopedCommand
      }

      implicit class SCommandOps(scopedCommand: ScopedCommand) {

        def +[A: Concat](other: A): ScopedCommand =
          implicitly[Concat[A]].+(other)(scopedCommand)

        private[ScopedCommand] def concatValue: String = {
          val builder = (command: String) => scopedCommand.projectScope.map(s => s"$s/$command").getOrElse(command)
          scopedCommand.subCommands.map(builder).mkString("; ").trim
        }
      }

      implicit val scopedCommandConcat: Concat[ScopedCommand] =
        new Concat[ScopedCommand] {
          override def +(other: ScopedCommand)(implicit self: ScopedCommand): ScopedCommand =
            ScopedCommand(
              self.concatValue :: other.concatValue :: Nil,
              self.scopedCommandOptions + other.scopedCommandOptions
            )
        }

      implicit val stringCommandConcat: Concat[String] =
        new Concat[String] {
          override def +(other: String)(implicit self: ScopedCommand): ScopedCommand =
            ScopedCommand(
              self.concatValue :: other :: Nil,
              self.scopedCommandOptions
            )
        }

      def apply(
        subCommands: List[String]
      ): ScopedCommand =
        SimpleScopedCommand(None, subCommands, ScopedCommandOptions.default)

      def apply(
        projectScope: String,
        subCommands: List[String]
      ): ScopedCommand =
        SimpleScopedCommand(Some(projectScope), subCommands, ScopedCommandOptions.default)

      private[ScopedCommand] def apply(
        projectScope: Option[String],
        subCommands: List[String],
        scopedCommandOptions: ScopedCommandOptions
      ): ScopedCommand = SimpleScopedCommand(projectScope, subCommands, scopedCommandOptions)

      private[ScopedCommand] def apply(
        subCommands: List[String],
        scopedCommandOptions: ScopedCommandOptions
      ): ScopedCommand = SimpleScopedCommand(None, subCommands, scopedCommandOptions)

      case class ScopedCommandOptions private (reloaded: Boolean, continuous: Boolean) {

        def +(other: ScopedCommandOptions): ScopedCommandOptions =
          // [this] command takes precedence for reloaded, [other] command determines continuous
          ScopedCommandOptions(reloaded = reloaded || other.reloaded, continuous = other.continuous)
      }

      private object ScopedCommandOptions {
        private[ScopedCommand] def default: ScopedCommandOptions = new ScopedCommandOptions(false, false)
      }

      private[ScopedCommand] final case class SimpleScopedCommand private (
        scope: Option[String],
        commands: List[String],
        commandOptions: ScopedCommandOptions
      ) extends ScopedCommand(scope, SimpleScopedCommand.validateSubCommands(commands), commandOptions)

      private object SimpleScopedCommand {
        private def validateSubCommands(subCommands: List[String]): List[String] = {
          require(subCommands.nonEmpty, "command list cannot be empty")
          require(subCommands.size == subCommands.toSet.size, "command list cannot contain duplicates")
          subCommands
        }
      }
    }

    sealed abstract class ScopedCommand(
      private[ScopedCommand] val projectScope: Option[String],
      private[ScopedCommand] val subCommands: List[String],
      private[ScopedCommand] val scopedCommandOptions: ScopedCommandOptions
    ) {

      def value: String = {
        def builder(lastIndex: Int)(command: String, currentIndex: Int) =
          if (scopedCommandOptions.reloaded && currentIndex == 0)
            projectScope
              .map(s => s"reload; $s/$command")
              .getOrElse(s"reload; $command")
          else
            s"${if (scopedCommandOptions.continuous && currentIndex == lastIndex) "~" else ""} ${projectScope
              .map(s => s"$s/$command")
              .getOrElse(command)}"

        subCommands.zipWithIndex.map((builder(subCommands.size - 1) _).tupled).mkString("; ").trim
      }

      def continuous: ScopedCommand =
        ScopedCommand.apply(projectScope, subCommands, scopedCommandOptions.copy(continuous = true))

      def reloaded: ScopedCommand =
        ScopedCommand.apply(projectScope, subCommands, scopedCommandOptions.copy(reloaded = true))

      def +(other: ScopedCommand): ScopedCommand = SCommandOps(this) + other

    }
  }
}
