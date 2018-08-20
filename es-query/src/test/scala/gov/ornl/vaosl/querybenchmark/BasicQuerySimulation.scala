package gov.ornl.vaosl.querybenchmark

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.core.Predef._
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.core.session.Expression
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._
import scala.util.Random

object ESConf {
  val conf: Config = ConfigFactory.load().getConfig("es")
  val host: String = conf.getString("host")
  val index: String = conf.getString("index")
}


object Search {
  import ESConf._

  val template: Expression[String] = (session: Session) =>
    for {
      foo <- session("searchTerm").validate[String]
    } yield s"""{ "query": { "match": { "note": $foo } } }"""

  val feeder: RecordSeqFeederBuilder[String] = Array(
    """{ "query": "spouse death", "operator": "and" }""",
    "\"divorce\"",
    "\"separation\"",
    "\"prison\"",
    """{ "query": "child death", "operator": "and" }""",
    """{ "query": "injury illness", "operator": "or" }""",
    "\"marriage\"",
    "\"fired\"",
    """{ "query": "marriage counsel", "operator": "and" }""",
    "\"retirement\""
  ).map(s => Map("searchTerm" -> s)).random

  val search: ChainBuilder =
    exec(
      http("Search")
        .post(s"/$index/_search?pretty")
        .body(StringBody(template)).asJSON
    )
}

class BasicQuerySimulation extends Simulation {
  import ESConf._

  val httpConf: HttpProtocolBuilder = http
    .baseURL(s"http://$host:9200") // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val scn: ScenarioBuilder = scenario("Query Scenario")
    .feed(Search.feeder)
    .exec(Search.search)
    .pause(7) // Note that Gatling has recorder real time pauses

  setUp(scn.inject(rampUsers(1000) over 1.minutes).protocols(httpConf))


}
