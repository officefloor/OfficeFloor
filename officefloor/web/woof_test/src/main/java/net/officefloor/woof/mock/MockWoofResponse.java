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
package net.officefloor.woof.mock;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.mock.MockHttpResponse;

/**
 * {@link MockHttpResponse} with additional assertions.
 * 
 * @author Daniel Sagenschneider
 */
public interface MockWoofResponse extends MockHttpResponse {

	/**
	 * Asserts a JSON error.
	 * 
	 * @param failure              Cause.
	 * @param headerNameValuePairs Expected {@link HttpHeader} name/value pairs.
	 */
	void assertJsonError(Throwable failure, String... headerNameValuePairs);

	/**
	 * Asserts a JSON error.
	 * 
	 * @param httpStatus           Expected {@link HttpStatus}.
	 * @param failure              Cause.
	 * @param headerNameValuePairs Expected {@link HttpHeader} name/value pairs.
	 */
	void assertJsonError(int httpStatus, Throwable failure, String... headerNameValuePairs);

}