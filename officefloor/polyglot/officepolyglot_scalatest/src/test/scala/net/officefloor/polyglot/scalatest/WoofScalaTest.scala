package net.officefloor.polyglot.scalatest

import org.scalatest.FlatSpec

class WoofScalaTest extends FlatSpec with WoofRules {

  "Call Server" should "get successful result" in {
    withMockWoofServer { server =>
      {
        server.send(mockRequest()).assertResponse(200, "successful")
      }
    }
  }

}