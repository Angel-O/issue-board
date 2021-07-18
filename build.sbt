import SettingsAndTasks._
import com.amazonaws.regions.{Region, Regions}

import scala.language.postfixOps
import scala.sys.process._

val baseVersion     = "0.0.1"
val scala213        = "2.13.6"
val projectName     = "issue-board"
val dbContainerName = s"$projectName-dev-db"
val dbScriptFolder  = "local-compose/db"
val dbScriptParams  = List(dbScriptFolder, dbContainerName, baseVersion)
val startDbProcess  = Process(s"sh $dbScriptFolder/start-db.sh ${dbScriptParams.mkString(" ")}")
val killDbProcess   = Process(s"docker container rm -f $dbContainerName")

Global / onChangedBuildSource := ReloadOnSourceChanges
//onLoad in Global := (Global / onLoad).value andThen { Command.process("project frontEnd", _) }
ThisBuild / name.withRank(KeyRanks.Invisible) := projectName
ThisBuild / version := baseVersion
ThisBuild / scalaVersion := scala213
ThisBuild / logLevel := Level.Info
ThisBuild / containerTag := println(baseVersion)
ThisBuild / scalacOptions := Seq("-language:postfixOps", "-deprecation", "-feature")

addCommandAlias("ui", Commands.UiHotReload.value)
addCommandAlias("ui-build", Commands.UiBuild.value)
addCommandAlias("server", Commands.ServerAndDbRun.value)
addCommandAlias("server-hot", Commands.ServerHotReload.value)
addCommandAlias("dev", (Commands.RunDb + Commands.StopStartWDS + Commands.DevMode).value)
addCommandAlias("down", Commands.TearDown.value)

lazy val root = (project in file("."))
  .aggregate(frontEnd, backEnd, shared.jvm, shared.js, misc)
  .disablePlugins(RevolverPlugin)
  .settings(
    name := projectName,
    devMode := {
      (backEnd / devMode).value
      (frontEnd / devMode).value
    }
  )

lazy val frontEnd = (project in file("front-end"))
  .dependsOn(shared.js)
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .disablePlugins(RevolverPlugin)
  .settings(
    useYarn := true,
    scalaJSUseMainModuleInitializer := true,
    fastOptJS / scalaJSLinkerConfig ~= { _.withOptimizer(false) },
    fastOptJS / webpackConfigFile := Some(webpackDir.value / "webpack-fastopt.config.js"),
    fastOptJS / webpackBundlingMode := BundlingMode.LibraryOnly(), //disable on prod
    fastOptJS / webpackDevServerExtraArgs := Seq("--inline", "--hot", "--env.ignoreEnvVarFile=false"),
    fastOptJS / webpackDevServerPort := 12345,
    Compile / npmDependencies ++= Dependencies.FrontEnd.npmDeps.value,
    Compile / npmDevDependencies ++= Dependencies.FrontEnd.npmDevDeps.value,
    libraryDependencies ++= Dependencies.FrontEnd.deps.value,
    startWds := (Compile / fastOptJS / startWebpackDevServer).value,
    stopWds := (Compile / fastOptJS / stopWebpackDevServer).value,
    devMode := (Compile / fastOptJS / webpack).value,
    startWebpackDevServer / version := "3.11.0",
    webpack / version := "4.43.0",
    webpackDir := baseDirectory.value / "webpack",
    webpackResources := webpackDir.value * "*",
    clean := clean.dependsOn(stopWds).value
  )

lazy val backEnd = (project in file("back-end"))
  .dependsOn(shared.jvm)
  .enablePlugins(JavaAppPackaging, EcrPlugin)
  .settings(
    Compile / mainClass := Some("com.angelo.dashboard.ZBoot"),
    reStart / javaOptions += "-Xmx2g",
    devMode := (Compile / reStart).toTask("").value,
    debugSettings := Revolver.enableDebugging(port = 5050, suspend = false).init.value,
    clean := clean.dependsOn(killDb).value,
    dockerSettings,
    dockerEnvVars := awsCreds,    // Docker / publishLocal / dockerEnvVars := awsCreds, not working...
    libraryDependencies ++= Dependencies.Backend.deps.value,
    runDb := startDbProcess run (thisProjectRef / streams).value.log,
    killDb := killDbProcess ! (thisProjectRef / streams).value.log,
    Global / cancelable := false, //https://github.com/sbt/sbt/issues/5226
    Global / onLoad := (Global / onLoad).value andThen { state =>
      val onExit = ExitHook(killDbProcess ! ProcessLogger(out => state.log.info(s"removed $out container"), _ => ()))
      state.copy(exitHooks = state.exitHooks + onExit)
    }
  )

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(libraryDependencies ++= Dependencies.Shared.deps.value)

lazy val misc = (project in file("miscellaneous"))
  .settings(libraryDependencies ++= Dependencies.Miscellaneous.deps.value)

lazy val dockerSettings = Seq(
  Docker / daemonUser := "daemon",
  Docker / packageName := s"$projectName-backend",
  Docker / version := baseVersion,
  Docker / dockerExposedPorts := Seq(8080),
  Docker / dockerLabels.withRank(KeyRanks.Invisible) := Map(baseVersion -> baseVersion),
  Docker / dockerBaseImage.withRank(KeyRanks.Invisible) := "openjdk:11.0.4-jdk",
  Docker / dockerRepository := Some("angeloop"),
  Ecr / region := Region.getRegion(Regions.EU_WEST_2),
  Ecr / repositoryName := (Docker / packageName).value,
  Ecr / localDockerImage := (Docker / dockerRepository).value.get + "/" + (Docker / packageName).value + ":" + (Docker / version).value,
  Ecr / login := ((Ecr / login) dependsOn (Ecr / createRepository)).value,
  Ecr / push := ((Ecr / push) dependsOn (Docker / publishLocal, Ecr / login)).value,
  Ecr / repositoryTags := Seq(baseVersion)
)
