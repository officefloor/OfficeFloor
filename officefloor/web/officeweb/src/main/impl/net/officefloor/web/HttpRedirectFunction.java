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

import java.io.Serializable;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.state.HttpCookie;
import net.officefloor.web.state.HttpRequestState;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource;

/**
 * {@link ManagedFunction} to send a redirect.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRedirectFunction
		implements ManagedFunctionFactory<HttpRedirectFunction.HttpRedirectDependencies, None>,
		ManagedFunction<HttpRedirectFunction.HttpRedirectDependencies, None> {

	/**
	 * Name of the {@link HttpSession} attribute containing the
	 * {@link HttpRequestState} momento.
	 */
	public static final String SESSION_ATTRIBUTE_REDIRECT_MOMENTO = "_redirect_";

	/**
	 * Dependency keys.
	 */
	public static enum HttpRedirectDependencies {
		SERVER_HTTP_CONNECTION, REQUEST_STATE, SESSION_STATE
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

		// Obtain the dependencies
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(HttpRedirectDependencies.SERVER_HTTP_CONNECTION);
		HttpRequestState requestState = (HttpRequestState) context.getObject(HttpRedirectDependencies.REQUEST_STATE);
		HttpSession session = (HttpSession) context.getObject(HttpRedirectDependencies.SESSION_STATE);

		// Obtain the redirect location
		String redirectLocation = connection.getServerLocation().createClientUrl(this.isSecure,
				this.applicationPath);

		// Send the redirect
		HttpResponse response = connection.getResponse();
		response.setStatus(HttpStatus.SEE_OTHER);
		response.getHeaders().addHeader(LOCATION, redirectLocation);

		// Export the request state
		Serializable momento = HttpRequestStateManagedObjectSource.exportHttpRequestState(requestState);

		// Store in session (to import on servicing redirect)
		session.setAttribute(SESSION_ATTRIBUTE_REDIRECT_MOMENTO, momento);

		// Load cookie indicating redirect
		HttpCookie.addHttpCookie(new HttpCookie("ofr", redirectLocation), connection.getResponse());

		// No further functions
		return null;
	}

}