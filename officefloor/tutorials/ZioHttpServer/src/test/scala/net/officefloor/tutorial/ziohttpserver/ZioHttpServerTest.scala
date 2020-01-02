package net.officefloor.tutorial.ziohttpserver

import net.officefloor.scalatest.WoofRules
import org.scalatest.FlatSpec

/**
 * Tests the ZIO HTTP Server.
 */
class ZioHttpServerTest extends FlatSpec with WoofRules {

  it should "get message" in {
    withMockWoofServer { server =>
      val request = mockRequest("/")
        .method(httpMethod("POST"))
        .header("Content-Type", "application/json")
        .entity(jsonEntity(new ZioRequest(1)))
      val response = server.send(request)
      response.assertResponse(200, jsonEntity(new ZioResponse("Hi via ZIO")))
    }
  }

}
