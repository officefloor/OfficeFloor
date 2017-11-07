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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.http.HttpEscalationHandler;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.route.WebRouter;

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
	 * {@link WebRouter}.
	 */
	private final WebRouter router;

	/**
	 * {@link Flow} index for non handled.
	 */
	private final int nonHandledFlowIndex;

	/**
	 * {@link HttpEscalationHandler}. May be <code>null</code>.
	 */
	private final HttpEscalationHandler escalationHandler;

	/**
	 * Instantiate.
	 * 
	 * @param router
	 *            {@link WebRouter}.
	 * @param nonHandledFlowIndex
	 *            {@link Flow} index for non handled.
	 * @param escalationHandler
	 *            {@link HttpEscalationHandler}. May be <code>null</code>.
	 */
	public HttpRouteFunction(WebRouter router, int nonHandledFlowIndex, HttpEscalationHandler escalationHandler) {
		this.router = router;
		this.escalationHandler = escalationHandler;
		this.nonHandledFlowIndex = nonHandledFlowIndex;
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
	public Object execute(ManagedFunctionContext<HttpRouteDependencies, Indexed> context) {

		// Obtain the server HTTP connection
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(HttpRouteDependencies.SERVER_HTTP_CONNECTION);

		// Load the escalation handler
		if (this.escalationHandler != null) {
			connection.getResponse().setEscalationHandler(this.escalationHandler);
		}

		// Attempt to route the request
		if (this.router.service(connection, context)) {
			return null; // routed to servicing

		} else {
			// Not handled
			context.doFlow(this.nonHandledFlowIndex, null, null);
			return null; // flow invoked
		}
	}

}