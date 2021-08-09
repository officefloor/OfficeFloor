/*-
 * #%L
 * Web Plug-in
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
import net.officefloor.web.route.WebServicer;
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
	 * @param router              {@link WebRouter}.
	 * @param nonHandledFlowIndex {@link Flow} index for non handled.
	 */
	public HttpRouter(WebRouter router, int nonHandledFlowIndex) {
		this.router = router;
		this.nonHandledFlowIndex = nonHandledFlowIndex;
	}

	/**
	 * Configures the {@link ManagedFunctionTypeBuilder} with the {@link Flow}
	 * instances for routing.
	 * 
	 * @param routes   {@link List} of {@link RouteInput} instances for the
	 *                 application.
	 * @param function {@link ManagedFunctionTypeBuilder} to be configured with the
	 *                 {@link Flow} instances.
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
	 * @param connection {@link ServerHttpConnection}.
	 * @param context    {@link ManagedFunctionContext}.
	 * @return Argument for the next {@link ManagedFunction}.
	 */
	public Object route(ServerHttpConnection connection, ManagedFunctionContext<?, Indexed> context) {

		// Obtain the service
		WebServicer servicer = this.router.getWebServicer(connection, context);
		switch (servicer.getMatchResult()) {
		case MATCH:
			// Service the request
			servicer.service(connection);
			break;

		default:
			// Not handled
			context.doFlow(this.nonHandledFlowIndex, servicer, null);
			break;
		}

		// Nothing further
		return null;
	}

}
