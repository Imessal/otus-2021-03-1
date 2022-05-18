lazy val root = project.in(file("."))
  .settings(
    name := "otusfp",
    version := "0.1.0-rc1",
    organization := "me.chuwy",
    scalaVersion := "2.13.4",
    initialCommands := "import me.chuwy.otusfp._"
  )
  .settings(BuildSettings.assemblySettings)
  .settings(BuildSettings.buildSettings)
  .settings(BuildSettings.scalifySettings)
  .settings(libraryDependencies ++= Dependencies.all)
  .settings(BuildSettings.helpersSettings)


// SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder"
// workaround for missing static SLF4J binder for logback
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"