resolvers := Seq(Resolver.jcenterRepo)
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.1.1")
addSbtPlugin("ch.epfl.scala"      % "sbt-scalajs-bundler"      % "0.18.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("ohnosequences"      % "sbt-s3-resolver"          % "0.17.0")
addSbtPlugin("com.mintbeans"      % "sbt-ecr"                  % "0.15.0")
addSbtPlugin("org.scalameta"      % "sbt-scalafmt"             % "2.3.2")
addSbtPlugin("com.typesafe.sbt"   % "sbt-native-packager"      % "1.6.1")
addSbtPlugin("io.spray"           % "sbt-revolver"             % "0.9.1")
