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

import java.util.List;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpRouteSectionSource.RouteInput;
import net.officefloor.web.route.WebRouter;
import net.officefloor.web.state.HttpArgument;

/**
 * Routes the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRouter {

	/**
	 * {@link WebRouter}.
	 */
	private final WebRouter router;

	/**
	 * {@link Flow} index for non handled.
	 */
	private final int nonHandledFlowIndex;

	/**
	 * Instantiate.
	 * 
	 * @param router
	 *            {@link WebRouter}.
	 * @param nonHandledFlowIndex
	 *            {@link Flow} index for non handled.
	 */
	public HttpRouter(WebRouter router, int nonHandledFlowIndex) {
		this.router = router;
		this.nonHandledFlowIndex = nonHandledFlowIndex;
	}

	/**
	 * Configures the {@link ManagedFunctionTypeBuilder} with the {@link Flow}
	 * instances for routing.
	 * 
	 * @param routes
	 *            {@link List} of {@link RouteInput} instances for the application.
	 * @param function
	 *            {@link ManagedFunctionTypeBuilder} to be configured with the
	 *            {@link Flow} instances.
	 */
	public void configureRoutes(List<RouteInput> routes, ManagedFunctionTypeBuilder<?, Indexed> function) {

		// Configure the route functions
		for (RouteInput route : routes) {
			ManagedFunctionFlowTypeBuilder<Indexed> flow = function.addFlow();
			flow.setLabel(route.getOutputName());
			flow.setArgumentType(HttpArgument.class);
		}

		// Configure the unhandled flow
		function.addFlow().setLabel(HttpRouteSectionSource.UNHANDLED_OUTPUT_NAME);
	}

	/**
	 * Routes the {@link ServerHttpConnection}.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @param context
	 *            {@link ManagedFunctionContext}.
	 * @return Argument for the next {@link ManagedFunction}.
	 */
	public Object route(ServerHttpConnection connection, ManagedFunctionContext<?, Indexed> context) {

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