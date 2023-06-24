/*-
 * #%L
 * Testing of HTTP Server
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

package net.officefloor.server.http.mock;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpResponseCookie;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.WritableHttpHeader;

/**
 * Mock {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MockHttpResponse {

	/**
	 * Obtains the {@link HttpVersion}.
	 * 
	 * @return {@link HttpVersion}.
	 */
	HttpVersion getVersion();

	/**
	 * Obtains the {@link HttpStatus}.
	 * 
	 * @return {@link HttpStatus}.
	 */
	HttpStatus getStatus();

	/**
	 * Obtains the first {@link WritableHttpHeader} by the name.
	 * 
	 * @param name Name of the {@link WritableHttpHeader}.
	 * @return First {@link WritableHttpHeader} by the name, or <code>null</code> if
	 *         no {@link WritableHttpHeader} by the name.
	 */
	WritableHttpHeader getHeader(String name);

	/**
	 * Assets the status of the {@link HttpResponse}.
	 * 
	 * @param statusCode Status code.
	 */
	void assertStatus(int statusCode);

	/**
	 * Assets the {@link HttpStatus} of the {@link HttpResponse}.
	 * 
	 * @param status {@link HttpStatus}.
	 */
	void assertStatus(HttpStatus status);

	/**
	 * Obtains the response {@link WritableHttpHeader} instances.
	 * 
	 * @return {@link WritableHttpHeader} instances.
	 */
	List<WritableHttpHeader> getHeaders();

	/**
	 * Asserts the contents of the {@link HttpResponse}.
	 * 
	 * @param statusCode           Expected status code.
	 * @param entity               Expected entity.
	 * @param headerNameValuePairs Expected {@link HttpHeader} name/value pairs.
	 *                             This only confirms they exist on the
	 *                             {@link HttpResponse}. It is not inclusive to
	 *                             check if these are the only {@link HttpHeader}
	 *                             instances.
	 */
	void assertResponse(int statusCode, String entity, String... headerNameValuePairs);

	/**
	 * Asserts contains the {@link HttpHeader}.
	 * 
	 * @param name  Expected name.
	 * @param value Expected value.
	 */
	void assertHeader(String name, String value);

	/**
	 * Obtains the {@link WritableHttpCookie} by the name.
	 * 
	 * @param name Name of the {@link WritableHttpCookie}.
	 * @return {@link WritableHttpCookie} by the name, or <code>null</code> if no
	 *         {@link WritableHttpCookie} by the name.
	 */
	WritableHttpCookie getCookie(String name);

	/**
	 * Obtains the response {@link WritableHttpCookie} instances.
	 * 
	 * @return {@link WritableHttpCookie} instances.
	 */
	List<WritableHttpCookie> getCookies();

	/**
	 * Asserts contains the {@link WritableHttpCookie}.
	 * 
	 * @param cookie Expected {@link WritableHttpCookie}.
	 * 
	 * @see MockHttpServer#mockResponseCookie(String, String)
	 */
	void assertCookie(HttpResponseCookie cookie);

	/**
	 * Obtains {@link InputStream} to the response HTTP entity.
	 * 
	 * @return {@link InputStream} to the response HTTP entity.
	 */
	InputStream getEntity();

	/**
	 * Obtains the HTTP entity as text.
	 * 
	 * @param charset {@link Charset} for HTTP entity. May be <code>null</code> to
	 *                use default {@link Charset}.
	 * @return Text of the HTTP entity.
	 */
	String getEntity(Charset charset);

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

}
