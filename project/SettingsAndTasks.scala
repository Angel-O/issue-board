import sbt.{settingKey, taskKey, File}

object SettingsAndTasks {

  lazy val containerTag = taskKey[Unit]("Prints the container tag")
  lazy val runDb        = taskKey[Unit]("Runs dynamo db via docker")
  lazy val killDb       = taskKey[Unit]("Kills dynamo db docker container")
  lazy val devMode      = taskKey[Unit]("Runs a service in development mode")
  lazy val stopWds      = taskKey[Unit]("Stops webpack devServer")
  lazy val startWds     = taskKey[Unit]("Starts webpack devServer")
  lazy val webpackDir   = settingKey[File]("Webpack files directory")

  //TODO pass these as args
  //TODO remove these once deployment script is ready
  lazy val awsCreds: Map[String, String] =
    List(
      "AWS_ACCESS_KEY_ID",
      "AWS_SECRET_ACCESS_KEY",
      "AWS_SESSION_TOKEN"
    ).map(envVarKey => envVarKey -> sys.env.getOrElse(envVarKey, "dummy creds")).toMap
}
