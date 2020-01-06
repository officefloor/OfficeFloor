package net.officefloor.tutorial.catshttpserver

import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import net.officefloor.scalatest.WoofRules
import org.scalatest.FlatSpec

/**
 * Tests the Cats HTTP server.
 */
class CatsHttpServerTest extends FlatSpec with WoofRules {

  "MessageRepository" should "have data" in {
    val xa = setupDatabase
    val message = MessageRepository.findById(1).transact(xa).unsafeRunSync
    assert(message.content == "Hi via Cats")
  }

  "HTTP Server" should "get message" in {
    setupDatabase
    withMockWoofServer { server =>
      val request = mockRequest("/")
        .method(httpMethod("POST"))
        .header("Content-Type", "application/json")
        .entity(jsonEntity(new CatsRequest(1)))
      val response = server.send(request)
      response.assertResponse(200, jsonEntity(new CatsResponse("Hi via Cats")))
    }
  }

  def setupDatabase(): Transactor[IO] = {
    implicit val cs = IO.contextShift(ExecutionContexts.synchronous)
    val xa = Transactor.fromDriverManager[IO]("org.h2.Driver", "jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1", "SA", "password",
      Blocker.liftExecutionContext(ExecutionContexts.synchronous))
    val drop = sql"DROP TABLE IF EXISTS message".update.run
    val create = sql"CREATE TABLE message (id INT, content VARCHAR(50))".update.run
    val insert = sql"INSERT INTO message (id, content) VALUES (1, 'Hi via Cats')".update.run
    (drop, create, insert).mapN(_ + _ + _).transact(xa).unsafeRunSync
    xa
  }

}