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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionFunctionNamespace;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpRouteFunction.HttpRouteDependencies;
import net.officefloor.web.NotFoundFunction.NotFoundDependencies;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.escalation.NotFoundHttpException;
import net.officefloor.web.route.WebRouter;
import net.officefloor.web.route.WebRouterBuilder;
import net.officefloor.web.state.HttpArgument;

/**
 * {@link SectionSource} to handle HTTP routing.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRouteSectionSource extends AbstractSectionSource {

	/**
	 * Name of {@link SectionOutput} for unhandled {@link HttpRequest}.
	 */
	public static final String UNHANDLED_OUTPUT_NAME = "UNHANDLED";

	/**
	 * Name of {@link SectionInput} for not found resource.
	 */
	public static final String NOT_FOUND_INPUT_NAME = "NOT_FOUND";

	/**
	 * Name of the route {@link ManagedFunction}.
	 */
	private static final String ROUTE_FUNCTION_NAME = "route";

	/**
	 * {@link WebRouterBuilder}.
	 */
	private final WebRouterBuilder builder;

	/**
	 * {@link RouteConfiguration} instances.
	 */
	private final List<RouteConfiguration> routes = new LinkedList<>();

	/**
	 * Instantiate.
	 * 
	 * @param contextPath
	 *            Context path. May be <code>null</code>.
	 */
	public HttpRouteSectionSource(String contextPath) {
		this.builder = new WebRouterBuilder(contextPath);
	}

	/**
	 * Adds a route.
	 * 
	 * @param method
	 *            {@link HttpMethod}.
	 * @param path
	 *            Route path.
	 * @return {@link OfficeSectionOutput} name for the route.
	 */
	public String addRoute(HttpMethod method, String path) {

		// Obtain the flow index for the route
		int flowIndex = this.routes.size();

		// Add the route
		this.builder.addRoute(method, path, new WebRouteHandlerImpl(flowIndex));

		// Track route information for configuration
		RouteConfiguration configuration = new RouteConfiguration(flowIndex, method, path);
		this.routes.add(configuration);

		// Return the route path
		return configuration.getOutputName();
	}

	/*
	 * ==================== SectionSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// All routes added via office, so now build web router
		WebRouter router = this.builder.build();

		// Add the routing function
		SectionFunctionNamespace routeNamespace = designer.addSectionFunctionNamespace("ROUTE",
				new HttpRouteManagedFunctionSource(router));
		SectionFunction route = routeNamespace.addSectionFunction(WebArchitect.HANDLER_INPUT_NAME, ROUTE_FUNCTION_NAME);

		// Make routing function handle routing
		SectionInput routeInput = designer.addSectionInput(WebArchitect.HANDLER_INPUT_NAME, null);
		designer.link(routeInput, route);

		// Link the routing to section outputs
		for (RouteConfiguration routeConfig : this.routes) {
			String outputName = routeConfig.getOutputName();
			SectionOutput output = designer.addSectionOutput(outputName, null, false);
			FunctionFlow flow = route.getFunctionFlow(outputName);
			designer.link(flow, output, false);
		}

		// Link non-handled to output
		FunctionFlow unhandledFlow = route.getFunctionFlow(UNHANDLED_OUTPUT_NAME);
		SectionOutput unhandled = designer.addSectionOutput(UNHANDLED_OUTPUT_NAME, null, false);
		designer.link(unhandledFlow, unhandled, false);

		// Configure the not found input
		SectionInput notFoundInput = designer.addSectionInput(NOT_FOUND_INPUT_NAME, null);
		SectionFunctionNamespace notFoundNamespace = designer.addSectionFunctionNamespace("NOT_FOUND",
				new NotFoundManagedFunctionSource());
		SectionFunction notFound = notFoundNamespace.addSectionFunction(NOT_FOUND_INPUT_NAME, NOT_FOUND_INPUT_NAME);
		designer.link(notFoundInput, notFound);

		// Escalate not found to office
		SectionOutput notFoundEscalation = designer.addSectionOutput(NotFoundHttpException.class.getSimpleName(),
				NotFoundHttpException.class.getName(), true);
		designer.link(notFound.getFunctionEscalation(NotFoundHttpException.class.getName()), notFoundEscalation, true);

		// Configure dependency on server HTTP connection
		SectionObject serverHttpConnection = designer.addSectionObject(ServerHttpConnection.class.getSimpleName(),
				ServerHttpConnection.class.getName());
		designer.link(route.getFunctionObject(HttpRouteDependencies.SERVER_HTTP_CONNECTION.name()),
				serverHttpConnection);
		designer.link(notFound.getFunctionObject(NotFoundDependencies.SERVER_HTTP_CONNECTION.name()),
				serverHttpConnection);
	}

	/**
	 * Information about the route.
	 */
	private static class RouteConfiguration {

		/**
		 * {@link Flow} index of the route.
		 */
		private final int flowIndex;

		/**
		 * {@link HttpMethod} for the route.
		 */
		private final HttpMethod method;

		/**
		 * Path for the route.
		 */
		private final String path;

		/**
		 * Instantiate.
		 * 
		 * @param flowIndex
		 *            {@link Flow} index of the route.
		 * @param method
		 *            {@link HttpMethod} for the route.
		 * @param path
		 *            Path for the route.
		 * @param routeBuilder
		 *            {@link WebRouterBuilder}.
		 */
		public RouteConfiguration(int flowIndex, HttpMethod method, String path) {
			this.flowIndex = flowIndex;
			this.method = method;
			this.path = path;
		}

		/**
		 * Obtains the {@link SectionOutput} name for this route.
		 * 
		 * @return {@link SectionOutput} name for this route.
		 */
		public String getOutputName() {
			return this.flowIndex + "-" + method.getName() + "-" + this.path;
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the {@link HttpRouteFunction}.
	 */
	private class HttpRouteManagedFunctionSource extends AbstractManagedFunctionSource {

		/**
		 * {@link WebRouter}.
		 */
		private final WebRouter router;

		/**
		 * Instantiate.
		 * 
		 * @param router
		 *            {@link WebRouter}.
		 */
		private HttpRouteManagedFunctionSource(WebRouter router) {
			this.router = router;
		}

		/*
		 * ============ ManagedFunctionSource ==============
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Obtain the non handled flow index
			int nonHandledFlowIndex = HttpRouteSectionSource.this.routes.size();

			// Add the route function
			ManagedFunctionTypeBuilder<HttpRouteDependencies, Indexed> builder = functionNamespaceTypeBuilder
					.addManagedFunctionType(ROUTE_FUNCTION_NAME,
							new HttpRouteFunction(this.router, nonHandledFlowIndex), HttpRouteDependencies.class,
							Indexed.class);

			// Configure dependency on server HTTP connection
			builder.addObject(ServerHttpConnection.class).setKey(HttpRouteDependencies.SERVER_HTTP_CONNECTION);

			// Configure the route functions
			for (RouteConfiguration route : HttpRouteSectionSource.this.routes) {
				ManagedFunctionFlowTypeBuilder<Indexed> flow = builder.addFlow();
				flow.setLabel(route.getOutputName());
				flow.setArgumentType(HttpArgument.class);
			}

			// Configure the unhandled flow
			builder.addFlow().setLabel(UNHANDLED_OUTPUT_NAME);
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the {@link NotFoundFunction}.
	 */
	private class NotFoundManagedFunctionSource extends AbstractManagedFunctionSource {

		/*
		 * ============ ManagedFunctionSource ==============
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Add the not found function
			ManagedFunctionTypeBuilder<NotFoundDependencies, None> builder = functionNamespaceTypeBuilder
					.addManagedFunctionType(NOT_FOUND_INPUT_NAME, new NotFoundFunction(), NotFoundDependencies.class,
							None.class);

			// Configure dependency on server HTTP connection
			builder.addObject(ServerHttpConnection.class).setKey(NotFoundDependencies.SERVER_HTTP_CONNECTION);

			// Configure not found escalation
			builder.addEscalation(NotFoundHttpException.class);
		}
	}

}