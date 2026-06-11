package net.officefloor.tutorial.ziohttpserver

import net.officefloor.scalatest.WoofRules
import org.scalatest.flatspec.AnyFlatSpec

/**
 * Tests the ZIO HTTP Server.
 */
// START SNIPPET: tutorial
class ZioHttpServerTest extends AnyFlatSpec with WoofRules {

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
// END SNIPPET: tutorial