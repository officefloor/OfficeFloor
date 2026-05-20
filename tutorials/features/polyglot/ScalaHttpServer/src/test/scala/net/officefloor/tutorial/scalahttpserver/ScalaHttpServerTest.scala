package net.officefloor.tutorial.scalahttpserver

import net.officefloor.scalatest.WoofRules
import org.scalatest.flatspec.AnyFlatSpec

class ScalaHttpServerTest extends AnyFlatSpec with WoofRules {

  "Call Server" should "get result" in {
    withMockWoofServer { server =>
      val request = mockRequest("/scala")
          .method(httpMethod("POST"))
          .header("Content-Type", "application/json")
          .entity(jsonEntity(new ScalaRequest("Daniel")))
      val response = server.send(request)
      response.assertResponse(200, jsonEntity(new ScalaResponse("Hello Daniel")))
    }
  }

}