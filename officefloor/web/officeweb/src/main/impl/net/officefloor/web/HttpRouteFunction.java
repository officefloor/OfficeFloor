/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.web;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.http.HttpEscalationHandler;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * {@link ManagedFunction} to route the {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRouteFunction implements ManagedFunctionFactory<HttpRouteFunction.HttpRouteDependencies, Indexed>,
		ManagedFunction<HttpRouteFunction.HttpRouteDependencies, Indexed> {

	/**
	 * Dependency keys.
	 */
	public static enum HttpRouteDependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * {@link HttpEscalationHandler}. May be <code>null</code>.
	 */
	private final HttpEscalationHandler escalationHandler;

	/**
	 * {@link Flow} index to handle a redirect.
	 */
	private final int handleRedirectFlowIndex;

	/**
	 * {@link HttpRouter}.
	 */
	private final HttpRouter router;

	/**
	 * Instantiate.
	 * 
	 * @param escalationHandler       {@link HttpEscalationHandler}. May be
	 *                                <code>null</code>.
	 * @param handleRedirectFlowIndex {@link Flow} index to handle a redirect.
	 * @param router                  {@link HttpRouter}.
	 */
	public HttpRouteFunction(HttpEscalationHandler escalationHandler, int handleRedirectFlowIndex, HttpRouter router) {
		this.escalationHandler = escalationHandler;
		this.handleRedirectFlowIndex = handleRedirectFlowIndex;
		this.router = router;
	}

	/*
	 * ============ ManagedFunctionFactory =============
	 */

	@Override
	public ManagedFunction<HttpRouteDependencies, Indexed> createManagedFunction() {
		return this;
	}

	/*
	 * =============== ManagedFunction =================
	 */

	@Override
	public void execute(ManagedFunctionContext<HttpRouteDependencies, Indexed> context) throws Exception {

		// Obtain the server HTTP connection
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(HttpRouteDependencies.SERVER_HTTP_CONNECTION);

		// Load the escalation handler
		if (this.escalationHandler != null) {
			connection.getResponse().setEscalationHandler(this.escalationHandler);
		}

		// Determine if potentially redirect
		HttpRequestCookie cookie = connection.getRequest().getCookies()
				.getCookie(HttpRedirectFunction.REDIRECT_COOKIE_NAME);
		if (cookie != null) {
			// Redirect, so trigger flow to import previous state
			context.doFlow(this.handleRedirectFlowIndex, cookie, null);
			return; // serviced by redirect
		}

		// No redirect, so route the request
		context.setNextFunctionArgument(this.router.route(connection, context));
	}

}