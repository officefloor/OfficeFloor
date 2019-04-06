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
package net.officefloor.polyglot.scalatest;

import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Provides access to {@link MockWoofServer} static methods.
 * 
 * @author Daniel Sagenschneider
 */
public class ScalaMockWoofServerStaticAccess {

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
}