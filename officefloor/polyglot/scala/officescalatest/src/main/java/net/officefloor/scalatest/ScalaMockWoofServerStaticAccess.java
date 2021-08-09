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

package net.officefloor.scalatest;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Provides access to {@link MockWoofServer} static methods.
 * 
 * @author Daniel Sagenschneider
 */
public class ScalaMockWoofServerStaticAccess {

	/**
	 * {@link ObjectMapper}.
	 */
	private static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.registerModule(new DefaultScalaModule());
	}

	/**
	 * Obtains {@link MockHttpRequestBuilder} for '/'.
	 * 
	 * @return {@link MockHttpRequestBuilder}.
	 */
	public MockHttpRequestBuilder mockRequest() {
		return MockWoofServer.mockRequest();
	}

	/**
	 * Obtains {@link MockHttpRequestBuilder}.
	 * 
	 * @param requestUri Request URI for {@link MockHttpRequestBuilder}.
	 * @return {@link MockHttpRequestBuilder}.
	 */
	public MockHttpRequestBuilder mockRequest(String requestUri) {
		return MockWoofServer.mockRequest(requestUri);
	}

	/**
	 * Obtains the {@link HttpMethod}.
	 * 
	 * @param methodName {@link HttpMethod} name.
	 * @return {@link HttpMethod}.
	 */
	public HttpMethod httpMethod(String methodName) {
		return HttpMethod.getHttpMethod(methodName);
	}

	/**
	 * Translate entity object to JSON.
	 * 
	 * @param entity Entity.
	 * @return JSON for entity.
	 * @throws IOException If fails to write object to JSON.
	 */
	public String jsonEntity(Object entity) throws IOException {
		return mapper.writeValueAsString(entity);
	}

}
