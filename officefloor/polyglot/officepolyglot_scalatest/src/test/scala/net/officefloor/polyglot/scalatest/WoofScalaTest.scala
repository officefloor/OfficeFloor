package net.officefloor.polyglot.scalatest

import org.scalatest.FlatSpec

class WoofScalaTest extends FlatSpec with WoofRules {

  "JSON payload" should "provide attributes" in {
    assert("{\"message\":\"test\"}" == jsonEntity(new ScalaResponse("test")))
  }

  "Call Server" should "get successful result" in {
    withMockWoofServer { server => {
      val response = server.send(mockRequest().method(
        httpMethod("POST")).header("Content-Type", "application/json")
        .entity(jsonEntity(new ScalaRequest("test"))))

      print("Hello World")

      response.assertResponse(200, jsonEntity(new ScalaResponse("REQUEST = test")))
    }
    }
  }

}