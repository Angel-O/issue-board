import sbt._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

object Dependencies {

  object Shared {

    val circeVersion = "0.13.0"

    val deps = Def.setting(
      Seq(
        "io.circe" %% "circe-core"    % circeVersion,
        "io.circe" %% "circe-generic" % circeVersion,
        "io.circe" %% "circe-parser"  % circeVersion,
        "io.circe" %% "circe-literal" % circeVersion
      )
    )
  }

  object FrontEnd {

    val reactVersion         = "16.7.0"
    val scalaJsReactVersion  = "1.7.2"
    val scalaCssVersion      = "0.6.1"
    val scalaJavaTimeVersion = "2.0.0"

    val deps = Def.setting(
      Seq(
        "com.github.japgolly.scalajs-react" %%% "core"                 % scalaJsReactVersion,
        "com.github.japgolly.scalajs-react" %%% "extra"                % scalaJsReactVersion,
        "com.github.japgolly.scalacss"      %%% "core"                 % scalaCssVersion,
        "com.github.japgolly.scalacss"      %%% "ext-react"            % scalaCssVersion,
        "io.suzaku"                         %%% "diode-react"          % "1.1.11", //TODO watch out when upgrading to 1.1.14+
        "io.github.cquiroz"                 %%% "scala-java-time"      % scalaJavaTimeVersion,
        "io.github.cquiroz"                 %%% "scala-java-time-tzdb" % scalaJavaTimeVersion,
        "com.outr"                          %%% "profig"               % "2.3.8", //TODO watch out when upgrading to 3.x.x
        "org.scala-lang.modules"            %%% "scala-xml"            % "1.3.0"
      )
    )

    val npmDeps = Def.setting(Seq("react" -> reactVersion, "react-dom" -> reactVersion))

    val npmDevDeps = Def.setting(
      Seq(
        "html-webpack-plugin"           -> "4.3.0",
        "copy-webpack-plugin"           -> "6.0.2",
        "webpack-merge"                 -> "4.2.2",
        "css-loader"                    -> "3.5.3",
        "style-loader"                  -> "1.2.1",
        "file-loader"                   -> "6.0.0",
        "webfonts-loader"               -> "5.2.2",
        "dotenv-safe"                   -> "8.2.0",
        "bulma"                         -> "0.9.0",
        "@fortawesome/fontawesome-free" -> "5.13.1",
        "mini-css-extract-plugin"       -> "0.9.0",
        "node-sass"                     -> "4.14.1",
        "sass-loader"                   -> "8.0.2",
        "debug"                         -> "4.0.0" // TODO write facade
      )
    )
  }

  object Backend {

    val http4sVersion = "0.21.14"
    val zioVersion    = "1.0.3"

    val deps = Def.setting(
      Seq(
        "org.http4s"             %% "http4s-dsl"          % http4sVersion,
        "org.http4s"             %% "http4s-blaze-server" % http4sVersion,
        "org.http4s"             %% "http4s-blaze-client" % http4sVersion,
        "org.http4s"             %% "http4s-circe"        % http4sVersion,
        "org.typelevel"          %% "cats-core"           % "2.0.0",
        "dev.zio"                %% "zio"                 % zioVersion,
        "dev.zio"                %% "zio-logging-slf4j"   % "0.5.4",
        "dev.zio"                %% "zio-interop-cats"    % "2.2.0.1",
        "com.github.pureconfig"  %% "pureconfig"          % "0.14.0",
        "ch.qos.logback"         % "logback-classic"      % "1.2.3",
        "software.amazon.awssdk" % "dynamodb"             % "2.15.56"
      )
    )
  }
}
