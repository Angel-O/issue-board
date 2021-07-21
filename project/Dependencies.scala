import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt._

object Dependencies {

  object Shared {

    val circeVersion = "0.14.1"
    val catsVersion  = "2.6.1"

    val deps = Def.setting(
      Seq(
        "io.circe"      %%% "circe-core"           % circeVersion,
        "io.circe"      %%% "circe-generic"        % circeVersion,
        "io.circe"      %%% "circe-generic-extras" % circeVersion,
        "io.circe"      %%% "circe-parser"         % circeVersion,
        "io.circe"      %%% "circe-literal"        % circeVersion,
        "org.typelevel" %%% "cats-core"            % catsVersion
      )
    )
  }

  object FrontEnd {

    val reactVersion         = "17.0.2"
    val scalaJsReactVersion  = "1.7.7"
    val scalaCssVersion      = "0.7.0"
    val scalaJavaTimeVersion = "2.2.2"

    val deps = Def.setting(
      Seq(
        "com.github.japgolly.scalajs-react" %%% "core"                 % scalaJsReactVersion,
        "com.github.japgolly.scalajs-react" %%% "extra"                % scalaJsReactVersion,
        "com.github.japgolly.scalacss"      %%% "core"                 % scalaCssVersion,
        "com.github.japgolly.scalacss"      %%% "ext-react"            % scalaCssVersion,
        "io.suzaku"                         %%% "diode-react"          % "1.1.14", // has a dependency on `scalaJsReactVersion`
        "io.github.cquiroz"                 %%% "scala-java-time"      % scalaJavaTimeVersion,
        "io.github.cquiroz"                 %%% "scala-java-time-tzdb" % scalaJavaTimeVersion,
        "com.outr"                          %%% "profig"               % "2.3.8" // yaml no longer supported on version 3.x.x (js)
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

    val http4sVersion = "0.21.24"
    val zioVersion    = "1.0.9"

    val deps = Def.setting(
      Seq(
        "org.http4s"            %% "http4s-dsl"          % http4sVersion,
        "org.http4s"            %% "http4s-blaze-server" % http4sVersion,
        "org.http4s"            %% "http4s-blaze-client" % http4sVersion,
        "org.http4s"            %% "http4s-circe"        % http4sVersion,
        "dev.zio"               %% "zio"                 % zioVersion,
        "dev.zio"               %% "zio-logging-slf4j"   % "0.5.11",
        "dev.zio"               %% "zio-interop-cats"    % "2.5.1.0",
        "com.github.pureconfig" %% "pureconfig"          % "0.16.0",
        "ch.qos.logback"         % "logback-classic"     % "1.2.3",
        "software.amazon.awssdk" % "dynamodb"            % "2.17.1"
      )
    )
  }

  object Miscellaneous {

    val deps = Def.setting(Seq("org.scala-lang.modules" %%% "scala-xml" % "2.0.0"))
  }
}
