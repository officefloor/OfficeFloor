/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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