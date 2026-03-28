/*-
 * #%L
 * OfficeFloor SAM Maven Plugin
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.maven.test;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import net.officefloor.nosql.dynamodb.test.DynamoDbExtension;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.test.UsesDockerTest;
import net.officefloor.test.system.EnvironmentExtension;
import net.officefloor.woof.mock.MockWoofResponse;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the SAM application.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class SamTest extends AbstractSamTestCase {

	public static @Order(1) @RegisterExtension final DynamoDbExtension dynamo = new DynamoDbExtension();

	public static @Order(2) @RegisterExtension final MockWoofServerExtension server = new MockWoofServerExtension();

	public static @Order(3) @RegisterExtension final EnvironmentExtension env = new EnvironmentExtension(SamLogic.PROPERTY_ENV,
			"TEST_ENV");

	/*
	 * ================== AbstractSamTestCase =================
	 */

	@Override
	protected DynamoDBMapper getDynamoDbMapper() {
		return dynamo.getDynamoDbMapper();
	}

	@Override
	protected Response doRequest(HttpMethod method, String path, String entity, String... headerNameValues) {
		MockHttpRequestBuilder request = MockWoofServer.mockRequest(path).method(method).entity(entity);
		for (int i = 0; i < headerNameValues.length; i += 2) {
			String name = headerNameValues[i];
			String value = headerNameValues[i + 1];
			request.header(name, value);
		}
		return new MockResponse(server.send(request));
	}

	/**
	 * {@link Response} to wrap {@link MockWoofResponse}.
	 */
	private static class MockResponse extends Response {

		/**
		 * {@link MockWoofResponse}.
		 */
		private final MockWoofResponse response;

		/**
		 * Instantiate.
		 * 
		 * @param response {@link MockWoofResponse}.
		 */
		private MockResponse(MockWoofResponse response) {
			this.response = response;
		}

		/*
		 * =================== Response =======================
		 */

		@Override
		public void assertResponse(int statusCode, String entity, String... headerNameValues) {

			// Determine if cookie
			String cookieName = null;
			String cookieValue = null;
			if ((headerNameValues.length > 0) && ("set-cookie".equals(headerNameValues[0]))) {

				// Obtain the cookie values
				String[] cookieParts = headerNameValues[1].split("=");
				cookieName = cookieParts[0];
				cookieValue = cookieParts[1];

				// Clear cookies from headers
				headerNameValues = new String[0];
			}

			// Assert the response
			this.response.assertResponse(statusCode, entity == null ? "" : entity, headerNameValues);

			// Assert the cookie
			if (cookieName != null) {
				this.response.assertCookie(new WritableHttpCookie(cookieName, cookieValue, null));
			}
		}
	}

}
