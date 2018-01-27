/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.server.http.mock;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

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
	 * @param name
	 *            Name of the {@link WritableHttpHeader}.
	 * @return First {@link WritableHttpHeader} by the name, or
	 *         <code>null</code> if no {@link WritableHttpHeader} by the name.
	 */
	WritableHttpHeader getHeader(String name);

	/**
	 * Obtains the response {@link WritableHttpHeader} instances.
	 * 
	 * @return {@link WritableHttpHeader} instances.
	 */
	List<WritableHttpHeader> getHeaders();

	/**
	 * Asserts the contents of the {@link HttpResponse}.
	 * 
	 * @param statusCode
	 *            Expected status code.
	 * @param entity
	 *            Expected entity.
	 * @param headerNameValuePairs
	 *            Expected {@link HttpHeader} name/value pairs. This only
	 *            confirms they exist on the {@link HttpResponse}. It is not
	 *            inclusive to check if these are the only {@link HttpHeader}
	 *            instances.
	 */
	void assertResponse(int statusCode, String entity, String... headerNameValuePairs);

	/**
	 * Asserts contains the {@link HttpHeader}.
	 * 
	 * @param name
	 *            Expected name.
	 * @param value
	 *            Expected value.
	 */
	void assertHeader(String name, String value);

	/**
	 * Obtains the {@link WritableHttpCookie} by the name.
	 * 
	 * @param name
	 *            Name of the {@link WritableHttpCookie}.
	 * @return {@link WritableHttpCookie} by the name, or <code>null</code> if
	 *         no {@link WritableHttpCookie} by the name.
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
	 * @param cookie
	 *            Expected {@link WritableHttpCookie}.
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
	 * @param charset
	 *            {@link Charset} for HTTP entity. May be <code>null</code> to
	 *            use default {@link Charset}.
	 * @return Text of the HTTP entity.
	 */
	String getEntity(Charset charset);

}