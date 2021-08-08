/*-
 * #%L
 * ScalaTest
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
