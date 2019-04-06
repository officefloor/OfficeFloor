package net.officefloor.tutorial.scalahttpserver

import org.scalatest.FlatSpec
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import net.officefloor.woof.mock.MockWoofServerRule
import net.officefloor.woof.mock.MockWoofServer
import net.officefloor.polyglot.scalatest.WoofRules

class ScalaHttpServerTest extends FlatSpec with WoofRules {

  "Call Server" should "get result" in {
    withMockWoofServer { server =>
      server.send(mockRequest()).assertResponse(200, "successful")
    }
  }

}