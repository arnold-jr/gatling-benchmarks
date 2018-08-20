name := "query-benchmark"

version := "0.1"

scalaVersion := "2.12.6"

enablePlugins(GatlingPlugin)

libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.3.0" % "test",
  "io.gatling"            % "gatling-test-framework"    % "2.3.0" % "test"
)