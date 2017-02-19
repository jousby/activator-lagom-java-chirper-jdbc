import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}
import NativePackagerHelper._

organization in ThisBuild := "sample.chirper"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

// SCALA SUPPORT: Remove the line below
EclipseKeys.projectFlavor in Global := EclipseProjectFlavor.Java

lazy val consulServiceLocator = project("consul-service-locator")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      "com.ecwid.consul" % "consul-api" % "1.2.1"
    )
  )

lazy val friendApi = project("friend-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies += lagomJavadslApi
  )

lazy val friendImpl = project("friend-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceJdbc,
      lagomJavadslTestKit
    )
  )
  // docker build
  .settings(
    mappings in Universal ++= directory("friend-impl/src/main/docker/root"),
    dockerCommands := Seq(
      Cmd("FROM", "smebberson/alpine-consul-base:4.1.0"),

      Cmd("WORKDIR", "/opt/docker"),

      Cmd("ADD", "opt/docker/root /"),
      Cmd("ADD", "opt /opt"),
      Cmd("RUN", "rm -rf /opt/docker/root"),

      Cmd("RUN", "apk add --update curl bash"),

      Cmd("ENV", "LANG", "C.UTF-8"),
      Cmd("ENV", "JAVA_HOME", "/usr/lib/jvm/java-1.8-openjdk"),
      Cmd("ENV", "JAVA_VERSION", "8u111"),
      Cmd("ENV", "PATH", "$PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin"),

      Cmd("RUN", "apk add --update openjdk8=\"8.111.14-r0\""),
      Cmd("RUN", "rm -rf /var/cache/apk/*")
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(friendApi, consulServiceLocator)

lazy val chirpApi = project("chirp-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslJackson
    )
  )

lazy val chirpImpl = project("chirp-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceJdbc,
      lagomJavadslPubSub,
      lagomJavadslTestKit
    )
  )
  // docker build
  .settings(
    mappings in Universal ++= directory("chirp-impl/src/main/docker/root"),
    dockerCommands := Seq(
      Cmd("FROM", "smebberson/alpine-consul-base:4.1.0"),

      Cmd("WORKDIR", "/opt/docker"),

      Cmd("ADD", "opt/docker/root /"),
      Cmd("ADD", "opt /opt"),
      Cmd("RUN", "rm -rf /opt/docker/root"),

      Cmd("RUN", "apk add --update curl bash"),

      Cmd("ENV", "LANG", "C.UTF-8"),
      Cmd("ENV", "JAVA_HOME", "/usr/lib/jvm/java-1.8-openjdk"),
      Cmd("ENV", "JAVA_VERSION", "8u111"),
      Cmd("ENV", "PATH", "$PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin"),

      Cmd("RUN", "apk add --update openjdk8=\"8.111.14-r0\""),
      Cmd("RUN", "rm -rf /var/cache/apk/*")
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(chirpApi, consulServiceLocator)

lazy val activityStreamApi = project("activity-stream-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies += lagomJavadslApi
  )
  .dependsOn(chirpApi)

lazy val activityStreamImpl = project("activity-stream-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies += lagomJavadslTestKit
  )
  // docker build
  .settings(
    mappings in Universal ++= directory("activity-stream-impl/src/main/docker/root"),
    dockerCommands := Seq(
      Cmd("FROM", "smebberson/alpine-consul-base:4.1.0"),

      Cmd("WORKDIR", "/opt/docker"),

      Cmd("ADD", "opt/docker/root /"),
      Cmd("ADD", "opt /opt"),
      Cmd("RUN", "rm -rf /opt/docker/root"),

      Cmd("RUN", "apk add --update curl bash"),

      Cmd("ENV", "LANG", "C.UTF-8"),
      Cmd("ENV", "JAVA_HOME", "/usr/lib/jvm/java-1.8-openjdk"),
      Cmd("ENV", "JAVA_VERSION", "8u111"),
      Cmd("ENV", "PATH", "$PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin"),

      Cmd("RUN", "apk add --update openjdk8=\"8.111.14-r0\""),
      Cmd("RUN", "rm -rf /var/cache/apk/*")
    )
  )
  .dependsOn(activityStreamApi, chirpApi, friendApi, consulServiceLocator)

lazy val frontEnd = project("front-end")
  .enablePlugins(PlayJava, LagomPlay)
  .settings(
    version := "1.0-SNAPSHOT",
    routesGenerator := InjectedRoutesGenerator,
    libraryDependencies ++= Seq(
      "org.webjars" % "react" % "0.14.8",
      "org.webjars" % "react-router" % "1.0.3",
      "org.webjars" % "jquery" % "2.2.4",
      "org.webjars" % "foundation" % "5.5.2"
    ),
    ReactJsKeys.sourceMapInline := true,
    // Remove to use Scala IDE
    EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)
  )
  // docker build
  .settings(
    mappings in Universal ++= directory("front-end/docker/root"),
    dockerCommands := Seq(
      Cmd("FROM", "smebberson/alpine-consul-base:4.1.0"),

      Cmd("WORKDIR", "/opt/docker"),

      Cmd("ADD", "opt/docker/root /"),
      Cmd("ADD", "opt /opt"),
      Cmd("RUN", "rm -rf /opt/docker/root"),

      Cmd("RUN", "apk add --update curl bash"),

      Cmd("ENV", "LANG", "C.UTF-8"),
      Cmd("ENV", "JAVA_HOME", "/usr/lib/jvm/java-1.8-openjdk"),
      Cmd("ENV", "JAVA_VERSION", "8u111"),
      Cmd("ENV", "PATH", "$PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin"),

      Cmd("RUN", "apk add --update openjdk8=\"8.111.14-r0\""),
      Cmd("RUN", "rm -rf /var/cache/apk/*")
    )
  )

lazy val loadTestApi = project("load-test-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies += lagomJavadslApi
  )

lazy val loadTestImpl = project("load-test-impl")
  .enablePlugins(LagomJava)
  .settings(version := "1.0-SNAPSHOT")
  .dependsOn(loadTestApi, friendApi, activityStreamApi, chirpApi)

def project(id: String) = Project(id, base = file(id))
  .settings(javacOptions in compile ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"))
  .settings(jacksonParameterNamesJavacSettings: _*) // applying it to every project even if not strictly needed.


// See https://github.com/FasterXML/jackson-module-parameter-names
lazy val jacksonParameterNamesJavacSettings = Seq(
  javacOptions in compile += "-parameters"
)

// Using jdbc based persistent entities so don't need Cassandra in dev env
lagomCassandraEnabled in ThisBuild := false

// Kafka can be disabled until we need it
lagomKafkaEnabled in ThisBuild := false

licenses in ThisBuild := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

