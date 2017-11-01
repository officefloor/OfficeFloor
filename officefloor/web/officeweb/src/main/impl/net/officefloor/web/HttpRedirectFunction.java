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
package net.officefloor.web;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * {@link ManagedFunction} to send a redirect.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRedirectFunction
		implements ManagedFunctionFactory<HttpRedirectFunction.HttpRedirectDependencies, None>,
		ManagedFunction<HttpRedirectFunction.HttpRedirectDependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum HttpRedirectDependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * <code>location</code> {@link HttpHeaderName}.
	 */
	private static final HttpHeaderName LOCATION = new HttpHeaderName("location");

	/**
	 * Indicates if redirect to secure port.
	 */
	private final boolean isSecure;

	/**
	 * Application path.
	 */
	private final String applicationPath;

	/**
	 * Instantiate.
	 * 
	 * @param isSecure
	 *            Indicates if redirect to secure port.
	 * @param applicationPath
	 *            Application path.
	 */
	public HttpRedirectFunction(boolean isSecure, String applicationPath) {
		this.isSecure = isSecure;
		this.applicationPath = applicationPath;
	}

	/*
	 * ============ ManagedFunctionFactory =============
	 */

	@Override
	public ManagedFunction<HttpRedirectDependencies, None> createManagedFunction() {
		return this;
	}

	/*
	 * =============== ManagedFunction =================
	 */

	@Override
	public Object execute(ManagedFunctionContext<HttpRedirectDependencies, None> context) {

		// Obtain the server HTTP connection
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(HttpRedirectDependencies.SERVER_HTTP_CONNECTION);

		// Obtain the redirect location
		String redirectLocation = connection.getHttpServerLocation().createClientUrl(this.isSecure,
				this.applicationPath);

		// Send the redirect
		HttpResponse response = connection.getHttpResponse();
		response.setHttpStatus(HttpStatus.SEE_OTHER);
		response.getHttpHeaders().addHeader(LOCATION, redirectLocation);

		// No further functions
		return null;
	}

}