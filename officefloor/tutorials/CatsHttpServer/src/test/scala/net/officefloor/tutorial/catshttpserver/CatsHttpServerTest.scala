package net.officefloor.tutorial.catshttpserver

import cats.effect._
import cats.effect.unsafe.IORuntime
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import net.officefloor.scalatest.WoofRules
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Tests the Cats HTTP server.
 */
class CatsHttpServerTest extends AnyFlatSpec with WoofRules {

  // START SNIPPET: effect
  "MessageRepository" should "have data" in {
    implicit val runtime: IORuntime = setupIORuntime
    implicit val xa: Transactor[IO] = setupDatabase
    val message = MessageRepository.findById(1).unsafeRunSync
    assert(message.content == "Hi via doobie")
  }
  // END SNIPPET: effect

  // START SNIPPET: server
  "HTTP Server" should "get message" in {
    setupDatabase
    withMockWoofServer { server =>
      val request = mockRequest("/")
        .method(httpMethod("POST"))
        .header("Content-Type", "application/json")
        .entity(jsonEntity(new CatsRequest(1)))
      val response = server.send(request)
      response.assertResponse(200, jsonEntity(new CatsResponse("Hi via doobie and Cats")))
    }
  }
  // END SNIPPET: server

  def setupDatabase(): Transactor[IO] = {
    implicit val runtime: IORuntime = setupIORuntime
    val xa = Transactor.fromDriverManager[IO]("org.h2.Driver", "jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1", "SA", "password")
    val drop = sql"DROP TABLE IF EXISTS message".update.run
    val create = sql"CREATE TABLE message (id INT, content VARCHAR(50))".update.run
    val insert = sql"INSERT INTO message (id, content) VALUES (1, 'Hi via doobie')".update.run
    (drop, create, insert).mapN(_ + _ + _).transact(xa).unsafeRunSync
    xa
  }

  def setupIORuntime() : IORuntime = IORuntime.global

}