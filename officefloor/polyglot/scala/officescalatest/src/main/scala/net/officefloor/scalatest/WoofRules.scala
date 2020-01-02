package net.officefloor.scalatest

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import net.officefloor.woof.mock.MockWoofServerRule
import net.officefloor.server.http.mock.MockHttpRequestBuilder
import net.officefloor.server.http.HttpMethod

/**
 * Traits to assist testing with WoOF.
 */
trait WoofRules {

  /**
   * Allows running a Rule.
   */
  def withRule[T <: TestRule](rule: T)(testCode: T => Any): Unit = {
    rule(
      new Statement() {
        override def evaluate(): Unit = testCode(rule)
      },
      Description.createSuiteDescription("JUnit rule " + rule.getClass().getName())).evaluate()
  }

  /**
   * Loads the MockWoofServer for testing.
   */
  def withMockWoofServer(testCode: MockWoofServerRule => Any): Unit = {
    withRule(new MockWoofServerRule()) { server =>
      testCode(server)
    }
  }

  /**
   * Creates a MockHttpRequestBuilder.
   */
  def mockRequest(): MockHttpRequestBuilder = new ScalaMockWoofServerStaticAccess().mockRequest()

  /**
   * Creates a MockHttpRequestBuilder.
   */
  def mockRequest(requestUri: String): MockHttpRequestBuilder = new ScalaMockWoofServerStaticAccess().mockRequest(requestUri)

  /**
   * Obtains the HttpMethod for the name.
   */
  def httpMethod(methodName: String): HttpMethod = new ScalaMockWoofServerStaticAccess().httpMethod(methodName)
  
  /**
   * Translate entity object to JSON.
   */
  def jsonEntity(entity: Any): String = new ScalaMockWoofServerStaticAccess().jsonEntity(entity)
  
}