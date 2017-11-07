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
import net.officefloor.server.http.HttpEscalationHandler;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpRedirectFunction.HttpRedirectDependencies;
import net.officefloor.web.HttpRouteFunction.HttpRouteDependencies;
import net.officefloor.web.InitialiseHttpRequestStateFunction.InitialiseHttpRequestStateDependencies;
import net.officefloor.web.NotFoundFunction.NotFoundDependencies;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.escalation.NotFoundHttpException;
import net.officefloor.web.route.WebRouter;
import net.officefloor.web.route.WebRouterBuilder;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.state.HttpArgument;
import net.officefloor.web.state.HttpRequestState;

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
	 * Name of the initialise {@link HttpRequestState} {@link ManagedFunction}.
	 */
	private static final String INITIALISE_REQUEST_STATE_FUNCTION_NAME = "initialise";

	/**
	 * Obtains the interception details.
	 */
	public static class Interception {

		/**
		 * {@link SectionOutput} name to link the interception.
		 */
		private final String outputName;

		/**
		 * {@link SectionInput} name to link the routing.
		 */
		private final String inputName;

		/**
		 * Instantiate.
		 * 
		 * @param outputName
		 *            {@link SectionOutput} name to link the interception.
		 * @param inputName
		 *            {@link SectionInput} name to link the routing.
		 */
		private Interception(String outputName, String inputName) {
			this.outputName = outputName;
			this.inputName = inputName;
		}

		/**
		 * Obtains the {@link SectionOutput} name to link the interception.
		 * 
		 * @return {@link SectionOutput} name to link the interception.
		 */
		public String getOutputName() {
			return this.outputName;
		}

		/**
		 * Obtains the {@link SectionInput} name to link the routing.
		 * 
		 * @return {@link SectionInput} name to link the routing.
		 */
		public String getInputName() {
			return this.inputName;
		}
	}

	/**
	 * Route input information.
	 */
	public static class RouteInput {

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
		 * Indicates if path parameters for the route.
		 */
		private final boolean isPathParameters;

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
		 * @param isPathParameters
		 *            Indicates if path parameters for the route.
		 */
		private RouteInput(int flowIndex, HttpMethod method, String path, boolean isPathParameters) {
			this.flowIndex = flowIndex;
			this.method = method;
			this.path = path;
			this.isPathParameters = isPathParameters;
		}

		/**
		 * Obtains the {@link SectionOutput} name for this route.
		 * 
		 * @return {@link SectionOutput} name for this route.
		 */
		public String getOutputName() {
			return this.flowIndex + "_" + method.getName() + "_" + this.path;
		}
	}

	/**
	 * Redirect.
	 */
	public static class Redirect {

		/**
		 * Indicates if redirect to a secure port.
		 */
		private final boolean isSecure;

		/**
		 * Application path for the redirect.
		 */
		private final String applicationPath;

		/**
		 * Name of the {@link SectionInput} to handle the redirect.
		 */
		private final String inputName;

		/**
		 * Instantiate.
		 * 
		 * @param isSecure
		 *            Indicates if redirect to a secure port.
		 * @param applicationPath
		 *            Application path for the redirect.
		 * @param inputName
		 *            Name of the {@link SectionInput} to handle the redirect.
		 */
		private Redirect(boolean isSecure, String applicationPath, String inputName) {
			this.isSecure = isSecure;
			this.applicationPath = applicationPath;
			this.inputName = inputName;
		}

		/**
		 * Obtains the name of the {@link SectionInput} to handle the redirect.
		 * 
		 * @return Name of the {@link SectionInput} to handle the redirect.
		 */
		public String getInputName() {
			return this.inputName;
		}
	}

	/**
	 * {@link WebRouterBuilder}.
	 */
	private final WebRouterBuilder builder;

	/**
	 * {@link HttpEscalationHandler}. May be <code>null</code>.
	 */
	private final HttpEscalationHandler escalationHandler;

	/**
	 * Optional {@link Interception}.
	 */
	private Interception interception = null;

	/**
	 * {@link RouteInput} instances.
	 */
	private final List<RouteInput> routes = new LinkedList<>();

	/**
	 * {@link Redirect} instances.
	 */
	private final List<Redirect> redirects = new LinkedList<>();

	/**
	 * Instantiate.
	 * 
	 * @param contextPath
	 *            Context path. May be <code>null</code>.
	 * @param escalationHandler
	 *            {@link HttpEscalationHandler}. May be <code>null</code>.
	 */
	public HttpRouteSectionSource(String contextPath, HttpEscalationHandler escalationHandler) {
		this.builder = new WebRouterBuilder(contextPath);
		this.escalationHandler = escalationHandler;
	}

	/**
	 * Obtains the {@link Interception}.
	 * 
	 * @return {@link Interception}.
	 */
	public Interception getInterception() {
		if (this.interception == null) {
			this.interception = new Interception("INTERCEPTION_OUT", "INTERCEPTION_IN");
		}
		return this.interception;
	}

	/**
	 * Adds a route.
	 * 
	 * @param method
	 *            {@link HttpMethod}.
	 * @param path
	 *            Route path.
	 * @return {@link RouteInput} for the route.
	 */
	public RouteInput addRoute(HttpMethod method, String path) {

		// Obtain the flow index for the route
		int flowIndex = this.routes.size();

		// Add the route
		boolean isPathParameters = this.builder.addRoute(method, path, new WebRouteHandlerImpl(flowIndex));

		// Track route information for configuration
		RouteInput input = new RouteInput(flowIndex, method, path, isPathParameters);
		this.routes.add(input);

		// Return the route input
		return input;
	}

	/**
	 * Adds a {@link Redirect}.
	 * 
	 * @param isSecure
	 *            Indicates to redirect to a secure port.
	 * @param applicationPath
	 *            Application path for the redirect.
	 * @return {@link Redirect}.
	 */
	public Redirect addRedirect(boolean isSecure, String applicationPath) {

		// Obtain the redirect index (to keep name unique)
		int redirectIndex = this.redirects.size();

		// Create the redirect input name
		String inputName = "REDIRECT_" + redirectIndex + (isSecure ? "_SECURE_" : "_") + applicationPath;

		// Create and register the redirect
		Redirect redirect = new Redirect(isSecure, applicationPath, inputName);
		this.redirects.add(redirect);

		// Return the redirect
		return redirect;
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

		// Determine if interception
		SectionInput handleHttpInput = designer.addSectionInput(WebArchitect.HANDLER_INPUT_NAME, null);
		if (this.interception == null) {
			// No interception, make routing function handle HTTP input
			designer.link(handleHttpInput, route);

		} else {
			// Have interception, so provide hooks to intercept
			SectionFunctionNamespace interceptNamespace = designer.addSectionFunctionNamespace(
					InterceptManagedFunctionSource.FUNCTION_NAME, new InterceptManagedFunctionSource());
			SectionFunction intercept = interceptNamespace.addSectionFunction(
					InterceptManagedFunctionSource.FUNCTION_NAME, InterceptManagedFunctionSource.FUNCTION_NAME);
			designer.link(handleHttpInput, intercept);

			// Trigger interception
			SectionOutput interceptOutput = designer.addSectionOutput(this.interception.outputName, null, false);
			designer.link(intercept, interceptOutput);

			// After interception, let routing service
			SectionInput routeInput = designer.addSectionInput(this.interception.inputName, null);
			designer.link(routeInput, route);
		}

		// Link the routing to section outputs
		SectionObject requestState = null;
		for (RouteInput routeConfig : this.routes) {

			// Obtain the route flow
			String outputName = routeConfig.getOutputName();
			SectionOutput output = designer.addSectionOutput(outputName, null, false);
			FunctionFlow flow = route.getFunctionFlow(outputName);

			// Determine if path parameters
			if (!routeConfig.isPathParameters) {
				// No path parameters, so link directly
				designer.link(flow, output, false);

			} else {
				// Path parameters, so must load to request state
				SectionFunctionNamespace initialiseNamespace = designer.addSectionFunctionNamespace(outputName,
						new InitialiseHttpRequestStateManagedFunctionSource());
				SectionFunction initialise = initialiseNamespace.addSectionFunction(outputName,
						INITIALISE_REQUEST_STATE_FUNCTION_NAME);
				designer.link(flow, initialise, false);

				// Configure HTTP path arguments parameter
				initialise.getFunctionObject(InitialiseHttpRequestStateDependencies.PATH_ARGUMENTS.name())
						.flagAsParameter();

				// Configure dependency on request state
				if (requestState == null) {
					requestState = designer.addSectionObject(HttpRequestState.class.getSimpleName(),
							HttpRequestState.class.getName());
				}
				designer.link(
						initialise.getFunctionObject(InitialiseHttpRequestStateDependencies.HTTP_REQUEST_STATE.name()),
						requestState);

				// Link initialise to output
				designer.link(initialise, output);
			}
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

		// Configure the redirects
		for (Redirect redirect : this.redirects) {

			// Obtain the function name
			String functionName = redirect.inputName;

			// Add the function to send the redirect
			SectionFunctionNamespace redirectNamespace = designer.addSectionFunctionNamespace(functionName,
					new RedirectManagedFunctionSource(redirect));
			SectionFunction redirection = redirectNamespace.addSectionFunction(functionName, functionName);

			// Link input to the function
			SectionInput redirectInput = designer.addSectionInput(functionName, null);
			designer.link(redirectInput, redirection);

			// Configure dependency on server HTTP connection
			designer.link(redirection.getFunctionObject(HttpRedirectDependencies.SERVER_HTTP_CONNECTION.name()),
					serverHttpConnection);
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the {@link InterceptFunction}.
	 */
	private static class InterceptManagedFunctionSource extends AbstractManagedFunctionSource {

		/**
		 * Name of the {@link ManagedFunction}.
		 */
		private static final String FUNCTION_NAME = "INTERCEPT";

		/*
		 * ============ ManagedFunctionSource ==============
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Add the intercept function
			functionNamespaceTypeBuilder.addManagedFunctionType(FUNCTION_NAME, new InterceptFunction(), None.class,
					None.class);
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
							new HttpRouteFunction(this.router, nonHandledFlowIndex,
									HttpRouteSectionSource.this.escalationHandler),
							HttpRouteDependencies.class, Indexed.class);

			// Configure dependency on server HTTP connection
			builder.addObject(ServerHttpConnection.class).setKey(HttpRouteDependencies.SERVER_HTTP_CONNECTION);

			// Configure the route functions
			for (RouteInput route : HttpRouteSectionSource.this.routes) {
				ManagedFunctionFlowTypeBuilder<Indexed> flow = builder.addFlow();
				flow.setLabel(route.getOutputName());
				flow.setArgumentType(HttpArgument.class);
			}

			// Configure the unhandled flow
			builder.addFlow().setLabel(UNHANDLED_OUTPUT_NAME);
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the
	 * {@link InitialiseHttpRequestStateFunction}.
	 */
	private class InitialiseHttpRequestStateManagedFunctionSource extends AbstractManagedFunctionSource {

		/*
		 * ============ ManagedFunctionSource ==============
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Add the initialise HTTP request state function
			ManagedFunctionTypeBuilder<InitialiseHttpRequestStateDependencies, None> builder = functionNamespaceTypeBuilder
					.addManagedFunctionType(INITIALISE_REQUEST_STATE_FUNCTION_NAME,
							new InitialiseHttpRequestStateFunction(), InitialiseHttpRequestStateDependencies.class,
							None.class);

			// Configure dependency on the request state and path arguments
			builder.addObject(HttpArgument.class).setKey(InitialiseHttpRequestStateDependencies.PATH_ARGUMENTS);
			builder.addObject(HttpRequestState.class).setKey(InitialiseHttpRequestStateDependencies.HTTP_REQUEST_STATE);
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the {@link HttpRedirectFunction}.
	 */
	private class RedirectManagedFunctionSource extends AbstractManagedFunctionSource {

		/**
		 * {@link Redirect}.
		 */
		private final Redirect redirect;

		/**
		 * Instantiate.
		 * 
		 * @param redirect
		 *            {@link Redirect}.
		 */
		private RedirectManagedFunctionSource(Redirect redirect) {
			this.redirect = redirect;
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

			// Create the function and obtain it's name
			final String functionName = this.redirect.inputName;
			HttpRedirectFunction function = new HttpRedirectFunction(this.redirect.isSecure,
					this.redirect.applicationPath);

			// Add the redirect function
			ManagedFunctionTypeBuilder<HttpRedirectDependencies, None> builder = functionNamespaceTypeBuilder
					.addManagedFunctionType(functionName, function, HttpRedirectDependencies.class, None.class);

			// Configure dependencies
			builder.addObject(ServerHttpConnection.class).setKey(HttpRedirectDependencies.SERVER_HTTP_CONNECTION);
			builder.addObject(HttpRequestState.class).setKey(HttpRedirectDependencies.REQUEST_STATE);
			builder.addObject(HttpSession.class).setKey(HttpRedirectDependencies.SESSION_STATE);
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