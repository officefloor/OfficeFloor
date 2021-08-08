/*-
 * #%L
 * Web on OfficeFloor Testing
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

package net.officefloor.woof.mock;

import com.fasterxml.jackson.databind.ObjectMapper;

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
	 * Obtains the JSON object from HTTP payload.
	 * 
	 * @param <T>        Type of object.
	 * @param statusCode {@link HttpStatus}.
	 * @param clazz      {@link Class} for the JSON object.
	 * @return JSON object.
	 */
	<T> T getJson(int statusCode, Class<T> clazz);

	/**
	 * Obtains the JSON object from HTTP payload using custom {@link ObjectMapper}.
	 * 
	 * @param <T>        Type of object.
	 * @param statusCode {@link HttpStatus}.
	 * @param clazz      {@link Class} for the JSON object.
	 * @param mapper     Custom {@link ObjectMapper}.
	 * @return JSON object.
	 */
	<T> T getJson(int statusCode, Class<T> clazz, ObjectMapper mapper);

	/**
	 * Asserts the JSON response.
	 * 
	 * @param statusCode           {@link HttpStatus}.
	 * @param entity               {@link Object} to be written as JSON.
	 * @param headerNameValuePairs Expected {@link HttpHeader} name/value pairs.
	 */
	void assertJson(int statusCode, Object entity, String... headerNameValuePairs);

	/**
	 * Asserts the JSON response providing custom {@link ObjectMapper}.
	 * 
	 * @param statusCode           {@link HttpStatus}.
	 * @param entity               {@link Object} to be written as JSON.
	 * @param mapper               Custom {@link ObjectMapper}.
	 * @param headerNameValuePairs Expected {@link HttpHeader} name/value pairs.
	 */
	void assertJson(int statusCode, Object entity, ObjectMapper mapper, String... headerNameValuePairs);

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
