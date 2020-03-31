import SettingsAndTasks._
import com.amazonaws.regions.{Region, Regions}

import scala.language.postfixOps
import scala.sys.process._

val baseVersion     = "0.0.1"
val scala213        = "2.13.1"
val projectName     = "issue-board"
val dbContainerName = s"$projectName-dev-db"
val startDbProcess  = Process(s"sh local-compose/db/start-db.sh local-compose/db $dbContainerName")
val killDbProcess   = Process(s"docker container rm -f $dbContainerName")

Global / onChangedBuildSource := ReloadOnSourceChanges
//onLoad in Global := (Global / onLoad).value andThen { Command.process("project frontEnd", _) }
ThisBuild / name := projectName
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
  .aggregate(frontEnd, backEnd, shared.jvm, shared.js)
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
    scalaJSLinkerConfig in fastOptJS ~= { _.withOptimizer(false) },
    libraryDependencies ++= Dependencies.FrontEnd.deps.value,
    npmDependencies in Compile ++= Dependencies.FrontEnd.npmDeps.value,
    npmDevDependencies in Compile ++= Dependencies.FrontEnd.npmDevDeps.value,
    startWds := (Compile / fastOptJS / startWebpackDevServer).value,
    stopWds := (Compile / fastOptJS / stopWebpackDevServer).value,
    devMode := (Compile / fastOptJS / webpack).value,
    version in webpack := "4.43.0",
    version in startWebpackDevServer := "3.11.0",
    webpackDir := baseDirectory.value / "webpack",
    webpackResources := webpackDir.value * "*",
    webpackConfigFile in fastOptJS := Some(webpackDir.value / "webpack-fastopt.config.js"),
    webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(), //disable on prod
    webpackDevServerExtraArgs in fastOptJS := Seq("--inline", "--hot", "--env.ignoreEnvVarFile=false"),
    webpackDevServerPort in fastOptJS := 12345,
    clean := clean.dependsOn(stopWds).value
  )

lazy val backEnd = (project in file("back-end"))
  .dependsOn(shared.jvm)
  .enablePlugins(JavaAppPackaging, EcrPlugin)
  .settings(
    mainClass in Compile := Some("com.angelo.dashboard.ZBoot"),
    javaOptions in reStart += "-Xmx2g",
    devMode := (Compile / reStart).toTask("").value,
    clean := clean.dependsOn(killDb).value,
    dockerSettings,
    logLevel in Docker := Level.Info,
    dockerEnvVars := awsCreds,
    libraryDependencies ++= Dependencies.Backend.deps.value,
    runDb := { startDbProcess run (thisProjectRef / streams).value.log },
    killDb := { killDbProcess ! (thisProjectRef / streams).value.log },
    Global / cancelable := false, //https://github.com/sbt/sbt/issues/5226
    Global / onLoad := (onLoad in Global).value andThen { state =>
      val onExit = ExitHook(killDbProcess ! ProcessLogger(out => state.log.info(s"removed $out container"), _ => ()))
      state.copy(exitHooks = state.exitHooks + onExit)
    }
  )

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(libraryDependencies ++= Dependencies.Shared.deps.value)

lazy val dockerSettings = Seq(
  dockerLabels := Map(baseVersion -> baseVersion),
  dockerExposedPorts in Docker := Seq(8080),
  daemonUser in Docker := "daemon",
  dockerBaseImage := "openjdk:11.0.4-jdk",
  dockerRepository := Some("angeloop"),
  (packageName in Docker) := s"$projectName-backend",
  region in Ecr := Region.getRegion(Regions.EU_WEST_2),
  repositoryName in Ecr := (packageName in Docker).value,
  version in Docker := baseVersion,
  localDockerImage in Ecr := (dockerRepository in Docker).value.get + "/" + (packageName in Docker).value + ":" + (version in Docker).value,
  login in Ecr := ((login in Ecr) dependsOn (createRepository in Ecr)).value,
  push in Ecr := ((push in Ecr) dependsOn (publishLocal in Docker, login in Ecr)).value,
  repositoryTags in Ecr := Seq(baseVersion)
)
