package nus.edu.u.task.perf

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

/**
 * Gatling simulation for Task Service REST endpoints.
 *
 * Configuration can be injected via JVM system properties or environment variables:
 * - `taskService.baseUrl` / `TASK_SERVICE_BASE_URL`
 * - `taskService.loginPath` / `TASK_SERVICE_LOGIN_PATH`
 * - `taskService.username` / `TASK_SERVICE_USERNAME`
 * - `taskService.password` / `TASK_SERVICE_PASSWORD`
 * - `taskService.eventId` / `TASK_SERVICE_EVENT_ID`
 * - `taskService.taskId` / `TASK_SERVICE_TASK_ID`
 */
class TaskServiceSimulation extends Simulation {

  private def propOrEnv(propKey: String, envKey: String): Option[String] =
    sys.props.get(propKey).orElse(sys.env.get(envKey))

  private def extractCookie(rawCookies: Seq[String], name: String): Option[String] =
    rawCookies
      .flatMap(_.split(";"))
      .map(_.trim)
      .find(_.startsWith(s"$name="))
      .map(_.substring(name.length + 1))

  private val baseUrl =
    propOrEnv("taskService.baseUrl", "TASK_SERVICE_BASE_URL")
      .getOrElse("http://localhost:8080")

  private val loginPath =
    propOrEnv("taskService.loginPath", "TASK_SERVICE_LOGIN_PATH")
      .getOrElse("/users/auth/login")

  private val defaultUsername =
    propOrEnv("taskService.username", "TASK_SERVICE_USERNAME")
      .getOrElse("lushuwen1")

  private val defaultPassword =
    propOrEnv("taskService.password", "TASK_SERVICE_PASSWORD")
      .getOrElse("lushuwen1")

  private val defaultEventId =
    propOrEnv("taskService.eventId", "TASK_SERVICE_EVENT_ID")
      .getOrElse("1")

  private val defaultTaskId =
    propOrEnv("taskService.taskId", "TASK_SERVICE_TASK_ID")
      .getOrElse("1")

  private val credentialFeeder =
    Iterator.continually(
      Map(
        "username" -> defaultUsername,
        "password" -> defaultPassword,
        "eventId" -> defaultEventId,
        "taskId" -> defaultTaskId
      ))

  private val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  private val loginAction =
    exec(
      http("login")
        .post(loginPath)
        .body(
          StringBody(
            """{"username":"${username}","password":"${password}"}"""
          ))
        .asJson
        .check(status.is(200))
        .check(headerValues("Set-Cookie").saveAs("rawCookies"))
    ).exitHereIfFailed
      .exec { session =>
        val rawCookies = session("rawCookies").asOption[Seq[String]].getOrElse(Seq.empty)
        val authorization = extractCookie(rawCookies, "Authorization")
        val refreshToken = extractCookie(rawCookies, "refreshToken")
        (authorization, refreshToken) match {
          case (Some(auth), Some(refresh)) =>
            session
              .set("authorizationCookie", auth)
              .set("refreshTokenCookie", refresh)
          case _ =>
            session.markAsFailed
        }
      }

  private val getTaskScenario =
    scenario("Login and Get Task")
      .feed(credentialFeeder)
      .exec(loginAction)
      .pause(500.millis)
      .exec(
        http("get-task")
          .get("/tasks/${eventId}/${taskId}")
          .header(
            "Cookie",
            "Authorization=${authorizationCookie}; refreshToken=${refreshTokenCookie}"
          )
          .check(status.in(200, 404))
      )

  setUp(
    getTaskScenario.inject(
      rampUsers(20).during(30.seconds),
      constantUsersPerSec(20).during(60.seconds)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.percentile3.lte(60000),
      global.successfulRequests.percent.gte(80),
      forAll.failedRequests.percent.lte(30)
    )
}
