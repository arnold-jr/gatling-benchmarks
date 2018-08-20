package gov.ornl.vaosl.querybenchmark

import com.typesafe.config.{Config, ConfigFactory}
import io.gatling.commons.validation.Validation
import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._


object TweetConf {
  val conf: Config = ConfigFactory.load().getConfig("tweeter")
  val endpoint: String = conf.getString("endpoint")
}


object PostTweet {

  val template: Session => Validation[Map[String,String]]  = (session: Session) =>
    for {
      handle <- session("name").validate[String]
      tweet <- session("word").validate[String]
    } yield Map("tweet[handle]" -> handle, "tweet[content]" -> tweet)

  val nameFeeder  = tsv("names.txt").random
  val wordFeeder  = tsv("words.txt").random

  val post: ChainBuilder =
    exec(
      http("Search")
        .post("")
        .formParamMap(template)
    )
}


class PostTweetSimulation extends Simulation {
  import TweetConf._

  val httpConf: HttpProtocolBuilder = http
    .baseURL(endpoint) // Here is the root for all relative URLs
    .contentTypeHeader("application/x-www-form-urlencoded")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val scn: ScenarioBuilder = scenario("Random Tweet Scenario")
    .feed(PostTweet.nameFeeder)
    .feed(PostTweet.wordFeeder)
    .exec(PostTweet.post)
    .pause(7) // Note that Gatling has recorder real time pauses

  setUp(scn.inject(rampUsers(10) over 1.minutes).protocols(httpConf))


}
