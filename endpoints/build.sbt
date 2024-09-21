import Dependencies.*

name := "endpoints"

lazy val `endpoints-domain` = project
  .in(file("00-domain"))
  .settings(
    libraryDependencies ++= Seq(
      org.typelevel.squants
    )
  )
  .dependsOn(
    LocalProject("common"),
    LocalProject("support_services"),
  )

lazy val `endpoints-repos` =
  project
    .in(file("01-repos"))
    .settings(
      libraryDependencies ++= uz.scala.all
    )
    .dependsOn(
      `endpoints-domain`
    )

lazy val `endpoints-core` =
  project
    .in(file("02-core"))
    .settings(
      libraryDependencies ++=
        Seq(
          dev.profunktor.`http4s-jwt-auth`
        )
    )
    .dependsOn(
      `endpoints-repos`,
      LocalProject("support_redis"),
    )

lazy val `endpoints-api` =
  project
    .in(file("03-api"))
    .settings(
      libraryDependencies ++= com.github.caliban.all
    )
    .dependsOn(
      `endpoints-core`
    )

lazy val `endpoints-server` =
  project
    .in(file("04-server"))
    .dependsOn(`endpoints-api`)

lazy val `endpoints-runner` =
  project
    .in(file("05-runner"))
    .dependsOn(
      `endpoints-server`,
      LocalProject("support_database"),
    )
    .settings(
      libraryDependencies ++= Seq()
    )
    .settings(DockerImagePlugin.serviceSetting("endpoints"))
    .enablePlugins(DockerImagePlugin, JavaAppPackaging, DockerPlugin)

aggregateProjects(
  `endpoints-core`,
  `endpoints-runner`,
)
