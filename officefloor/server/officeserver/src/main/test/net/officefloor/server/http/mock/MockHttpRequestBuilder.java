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

import java.io.OutputStream;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpVersion;

/**
 * Builder for a mock {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MockHttpRequestBuilder {

	/**
	 * Flags with the {@link HttpRequest} is secure.
	 * 
	 * @param isSecure
	 *            <code>true</code> if secure {@link HttpRequest}.
	 * @return <code>this</code>.
	 */
	MockHttpRequestBuilder secure(boolean isSecure);

	/**
	 * Specifies the {@link HttpMethod}.
	 * 
	 * @param method
	 *            {@link HttpMethod}.
	 * @return <code>this</code>.
	 */
	MockHttpRequestBuilder method(HttpMethod method);

	/**
	 * Specifies the request URI.
	 * 
	 * @param requestUri
	 *            Request URI.
	 * @return <code>this</code>.
	 */
	MockHttpRequestBuilder uri(String requestUri);

	/**
	 * Specifies the {@link HttpVersion}.
	 * 
	 * @param version
	 *            {@link HttpVersion}.
	 * @return <code>this</code>.
	 */
	MockHttpRequestBuilder version(HttpVersion version);

	/**
	 * Adds a {@link HttpHeader}.
	 * 
	 * @param name
	 *            {@link HttpHeader} name.
	 * @param value
	 *            {@link HttpHeader} value.
	 * @return <code>this</code>.
	 */
	MockHttpRequestBuilder header(String name, String value);

	/**
	 * Sets the HTTP entity.
	 * 
	 * @param entity
	 *            Entity content.
	 * @return <code>this</code>.
	 */
	MockHttpRequestBuilder entity(String entity);

	/**
	 * Obtains the {@link OutputStream} to write the HTTP entity.
	 * 
	 * @return {@link OutputStream} to write the HTTP entity.
	 */
	OutputStream getHttpEntity();

	/**
	 * Flags to turn off checks for {@link HttpRequest} and provide efficient
	 * processing.
	 * 
	 * @param isStress
	 *            <code>true</code> to turn off checks and process more
	 *            efficiently.
	 * @return <code>this</code>.
	 */
	MockHttpRequestBuilder setEfficientForStressTests(boolean isStress);

	/**
	 * <p>
	 * Builds a mock {@link HttpRequest} from this
	 * {@link MockHttpRequestBuilder} configuration.
	 * <p>
	 * This is useful for testing to create a mock {@link HttpRequest}.
	 * 
	 * @return Mock {@link HttpRequest}.
	 */
	HttpRequest build();

}