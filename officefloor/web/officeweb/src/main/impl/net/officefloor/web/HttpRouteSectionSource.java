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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
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
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.http.HttpEscalationHandler;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpHandleRedirectFunction.HttpHandleRedirectDependencies;
import net.officefloor.web.HttpRedirectFunction.HttpRedirectDependencies;
import net.officefloor.web.HttpRouteFunction.HttpRouteDependencies;
import net.officefloor.web.InitialiseHttpRequestStateFunction.InitialiseHttpRequestStateDependencies;
import net.officefloor.web.NotHandledFunction.NotHandledDependencies;
import net.officefloor.web.build.HttpPathFactory;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.escalation.NotFoundHttpException;
import net.officefloor.web.route.WebRouter;
import net.officefloor.web.route.WebRouterBuilder;
import net.officefloor.web.route.WebServicer;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.state.HttpArgument;
import net.officefloor.web.state.HttpRequestState;

/**
 * {@link SectionSource} to handle HTTP routing.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
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
	 * Name of the handle redirect {@link ManagedFunction}.
	 */
	private static final String HANDLE_REDIRECT_FUNCTION_NAME = "redirect";

	/**
	 * Name of {@link FunctionFlow} to handle redirect.
	 */
	private static final String HANDLE_REDIRECT_FLOW = "REDIRECT";

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
		 * @param outputName {@link SectionOutput} name to link the interception.
		 * @param inputName  {@link SectionInput} name to link the routing.
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
		 * {@link HttpInputPath}.
		 */
		private final HttpInputPath inputPath;

		/**
		 * Instantiate.
		 * 
		 * @param flowIndex {@link Flow} index of the route.
		 * @param method    {@link HttpMethod} for the route.
		 * @param path      Path for the route.
		 * @param inputPath {@link HttpInputPath}.
		 */
		private RouteInput(int flowIndex, HttpMethod method, String path, HttpInputPath inputPath) {
			this.flowIndex = flowIndex;
			this.method = method;
			this.path = path;
			this.inputPath = inputPath;
		}

		/**
		 * Obtains the {@link SectionOutput} name for this route.
		 * 
		 * @return {@link SectionOutput} name for this route.
		 */
		public String getOutputName() {
			return this.flowIndex + "_" + method.getName() + "_" + this.path;
		}

		/**
		 * Obtains the {@link HttpInputPath} for this route.
		 * 
		 * @return {@link HttpInputPath} for this route.
		 */
		public HttpInputPath getHttpInputPath() {
			return this.inputPath;
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
		 * {@link HttpPathFactory}.
		 */
		private final HttpPathFactory<?> httpPathFactory;

		/**
		 * Name of the {@link SectionInput} to handle the redirect.
		 */
		private final String inputName;

		/**
		 * Instantiate.
		 * 
		 * @param isSecure        Indicates if redirect to a secure port.
		 * @param httpPathFactory {@link HttpPathFactory} for this redirect.
		 * @param inputName       Name of the {@link SectionInput} to handle the
		 *                        redirect.
		 * @param parameterType   Type of parameter passed to the
		 *                        {@link ManagedFunction} to retrieve the values to
		 *                        construct the path.
		 */
		private Redirect(boolean isSecure, HttpPathFactory<?> httpPathFactory, String inputName) {
			this.isSecure = isSecure;
			this.httpPathFactory = httpPathFactory;
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
	private HttpEscalationHandler escalationHandler = null;

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
	 * @param contextPath Context path. May be <code>null</code>.
	 */
	public HttpRouteSectionSource(String contextPath) {
		this.builder = new WebRouterBuilder(contextPath);
	}

	/**
	 * Specifies the {@link HttpEscalationHandler}
	 * 
	 * @param escalationHandler {@link HttpEscalationHandler}. May be
	 *                          <code>null</code>.
	 */
	public void setHttpEscalationHandler(HttpEscalationHandler escalationHandler) {
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
	 * Indicates if the path contains parameters.
	 * 
	 * @param path Path.
	 * @return <code>true</code> should the path contain parameters.
	 */
	public boolean isPathParameters(String path) {
		return WebRouterBuilder.isPathParameters(path);
	}

	/**
	 * Adds a route.
	 * 
	 * @param isSecure Indicates if a secure connection is required for the route.
	 * @param method   {@link HttpMethod}.
	 * @param path     Route path.
	 * @return {@link RouteInput} for the route.
	 */
	public RouteInput addRoute(boolean isSecure, HttpMethod method, String path) {

		// Obtain the flow index for the route
		int flowIndex = this.routes.size();

		// Add the route
		HttpInputPath inputPath = this.builder.addRoute(method, path, new WebRouteHandlerImpl(isSecure, flowIndex));

		// Track route information for configuration
		RouteInput input = new RouteInput(flowIndex, method, path, inputPath);
		this.routes.add(input);

		// Return the route input
		return input;
	}

	/**
	 * Adds a {@link Redirect}.
	 * 
	 * @param isSecure      Indicates to redirect to a secure port.
	 * @param routeInput    {@link RouteInput} to redirect to.
	 * @param parameterType Type of parameter passed to the redirect
	 *                      {@link ManagedFunction} to source values for
	 *                      constructing the path. May be <code>null</code>.
	 * @return {@link Redirect}.
	 * @throws Exception If fails to add the redirect.
	 */
	public Redirect addRedirect(boolean isSecure, RouteInput routeInput, Class<?> parameterType) throws Exception {

		// Obtain the redirect index (to keep name unique)
		int redirectIndex = this.redirects.size();

		// Create the HTTP path factory (defaulting type to object)
		parameterType = (parameterType == null) ? Object.class : parameterType;
		HttpPathFactory<?> httpPathFactory = routeInput.inputPath.createHttpPathFactory(parameterType);

		// Create the redirect input name
		String inputName = "REDIRECT_" + redirectIndex + (isSecure ? "_SECURE_" : "_") + routeInput.path
				+ (parameterType == null ? "" : "_" + parameterType.getName());

		// Create and register the redirect
		Redirect redirect = new Redirect(isSecure, httpPathFactory, inputName);
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
		WebRouter webRouter = this.builder.build();
		int nonHandledFlowIndex = this.routes.size();
		HttpRouter httpRouter = new HttpRouter(webRouter, nonHandledFlowIndex);

		// Add the routing function
		SectionFunctionNamespace routeNamespace = designer.addSectionFunctionNamespace("ROUTE",
				new HttpRouteManagedFunctionSource(httpRouter));
		SectionFunction route = routeNamespace.addSectionFunction(WebArchitect.HANDLER_INPUT_NAME, ROUTE_FUNCTION_NAME);

		// Add the handle redirect function
		SectionFunctionNamespace handleRedirectNamespace = designer.addSectionFunctionNamespace("REDIRECT",
				new HttpHandleRedirectFunctionSource(httpRouter));
		SectionFunction handleRedirect = handleRedirectNamespace.addSectionFunction(HANDLE_REDIRECT_FUNCTION_NAME,
				HANDLE_REDIRECT_FUNCTION_NAME);

		// Link redirect flow to handle redirect
		designer.link(route.getFunctionFlow(HANDLE_REDIRECT_FLOW), handleRedirect, false);

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

		// Obtain the connection, request state, session and application state
		SectionObject serverHttpConnection = designer.addSectionObject(ServerHttpConnection.class.getSimpleName(),
				ServerHttpConnection.class.getName());
		SectionObject requestState = designer.addSectionObject(HttpRequestState.class.getSimpleName(),
				HttpRequestState.class.getName());
		SectionObject session = designer.addSectionObject(HttpSession.class.getSimpleName(),
				HttpSession.class.getName());

		// Link the routing to section outputs
		for (RouteInput routeConfig : this.routes) {

			// Obtain the route flow
			String outputName = routeConfig.getOutputName();
			SectionOutput output = designer.addSectionOutput(outputName, null, false);

			// Obtain the handle redirect flow (never initialises)
			FunctionFlow handleRedirectFlow = handleRedirect.getFunctionFlow(outputName);
			designer.link(handleRedirectFlow, output, false);

			// Determine if path parameters
			FunctionFlow routeFlow = route.getFunctionFlow(outputName);
			if (!routeConfig.inputPath.isPathParameters()) {
				// No path parameters, so link directly
				designer.link(routeFlow, output, false);

			} else {
				// Path parameters, so must load to request state
				SectionFunctionNamespace initialiseNamespace = designer.addSectionFunctionNamespace(outputName,
						new InitialiseHttpRequestStateManagedFunctionSource());
				SectionFunction initialise = initialiseNamespace.addSectionFunction(outputName,
						INITIALISE_REQUEST_STATE_FUNCTION_NAME);
				designer.link(routeFlow, initialise, false);

				// Configure HTTP path arguments parameter
				initialise.getFunctionObject(InitialiseHttpRequestStateDependencies.PATH_ARGUMENTS.name())
						.flagAsParameter();

				// Configure dependency on request state
				designer.link(
						initialise.getFunctionObject(InitialiseHttpRequestStateDependencies.HTTP_REQUEST_STATE.name()),
						requestState);

				// Link initialise to output
				designer.link(initialise, output);
			}
		}

		// Link non-handled to output
		SectionOutput unhandled = designer.addSectionOutput(UNHANDLED_OUTPUT_NAME, null, false);
		designer.link(route.getFunctionFlow(UNHANDLED_OUTPUT_NAME), unhandled, false);
		designer.link(handleRedirect.getFunctionFlow(UNHANDLED_OUTPUT_NAME), unhandled, false);

		// Configure the not found input
		SectionInput notHandledInput = designer.addSectionInput(NOT_FOUND_INPUT_NAME, null);
		SectionFunctionNamespace notHandledNamespace = designer.addSectionFunctionNamespace("NOT_HANDLED",
				new NotHandledManagedFunctionSource());
		SectionFunction notHandled = notHandledNamespace.addSectionFunction(NOT_FOUND_INPUT_NAME, NOT_FOUND_INPUT_NAME);
		designer.link(notHandledInput, notHandled);

		// Escalate not found to office
		SectionOutput notFoundEscalation = designer.addSectionOutput(NotFoundHttpException.class.getSimpleName(),
				NotFoundHttpException.class.getName(), true);
		designer.link(notHandled.getFunctionEscalation(NotFoundHttpException.class.getName()), notFoundEscalation,
				true);

		// Configure routing dependencies
		designer.link(route.getFunctionObject(HttpRouteDependencies.SERVER_HTTP_CONNECTION.name()),
				serverHttpConnection);

		// Configure handle redirect dependencies
		handleRedirect.getFunctionObject(HttpHandleRedirectDependencies.COOKIE.name()).flagAsParameter();
		designer.link(handleRedirect.getFunctionObject(HttpHandleRedirectDependencies.SERVER_HTTP_CONNECTION.name()),
				serverHttpConnection);
		designer.link(handleRedirect.getFunctionObject(HttpHandleRedirectDependencies.REQUEST_STATE.name()),
				requestState);
		designer.link(handleRedirect.getFunctionObject(HttpHandleRedirectDependencies.SESSION.name()), session);

		// Configure not handled dependencies
		designer.link(notHandled.getFunctionObject(NotHandledDependencies.SERVER_HTTP_CONNECTION.name()),
				serverHttpConnection);
		notHandled.getFunctionObject(NotHandledDependencies.WEB_SERVICER.name()).flagAsParameter();

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
			redirection.getFunctionObject(HttpRedirectDependencies.PATH_VALUES.name()).flagAsParameter();
			designer.link(redirection.getFunctionObject(HttpRedirectDependencies.SERVER_HTTP_CONNECTION.name()),
					serverHttpConnection);
			designer.link(redirection.getFunctionObject(HttpRedirectDependencies.REQUEST_STATE.name()), requestState);
			designer.link(redirection.getFunctionObject(HttpRedirectDependencies.SESSION_STATE.name()), session);
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the {@link InterceptFunction}.
	 */
	@PrivateSource
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
			functionNamespaceTypeBuilder.addManagedFunctionType(FUNCTION_NAME, None.class, None.class)
					.setFunctionFactory(new InterceptFunction());
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the {@link HttpRouteFunction}.
	 */
	@PrivateSource
	private class HttpRouteManagedFunctionSource extends AbstractManagedFunctionSource {

		/**
		 * {@link HttpRouter}.
		 */
		private final HttpRouter router;

		/**
		 * Instantiate.
		 * 
		 * @param router {@link HttpRouter}.
		 */
		private HttpRouteManagedFunctionSource(HttpRouter router) {
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

			// Obtain the routes
			List<RouteInput> routes = HttpRouteSectionSource.this.routes;

			// Obtain the handle redirect flow (size is non-handled flow)
			int handleRedirectFlowIndex = routes.size() + 1;

			// Build the function
			ManagedFunctionTypeBuilder<HttpRouteDependencies, Indexed> builder = functionNamespaceTypeBuilder
					.addManagedFunctionType(ROUTE_FUNCTION_NAME, HttpRouteDependencies.class, Indexed.class)
					.setFunctionFactory(new HttpRouteFunction(HttpRouteSectionSource.this.escalationHandler,
							handleRedirectFlowIndex, this.router));

			// Configure the flows for the routes
			this.router.configureRoutes(routes, builder);

			// Add handle redirect flow
			builder.addFlow().setLabel(HANDLE_REDIRECT_FLOW);

			// Configure dependency on server HTTP connection
			builder.addObject(ServerHttpConnection.class).setKey(HttpRouteDependencies.SERVER_HTTP_CONNECTION);
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the {@link HttpHandleRedirectFunction}.
	 */
	@PrivateSource
	private class HttpHandleRedirectFunctionSource extends AbstractManagedFunctionSource {

		/**
		 * {@link HttpRouter}.
		 */
		private final HttpRouter router;

		/**
		 * Instantiate.
		 * 
		 * @param router {@link HttpRouter}.
		 */
		private HttpHandleRedirectFunctionSource(HttpRouter router) {
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

			// Obtain the routes
			List<RouteInput> routes = HttpRouteSectionSource.this.routes;

			// Build the function
			ManagedFunctionTypeBuilder<HttpHandleRedirectDependencies, Indexed> builder = functionNamespaceTypeBuilder
					.addManagedFunctionType(HANDLE_REDIRECT_FUNCTION_NAME, HttpHandleRedirectDependencies.class,
							Indexed.class)
					.setFunctionFactory(new HttpHandleRedirectFunction(this.router));

			// Configure the flows for the routes
			this.router.configureRoutes(routes, builder);

			// Configure dependency on server HTTP connection
			builder.addObject(HttpRequestCookie.class).setKey(HttpHandleRedirectDependencies.COOKIE);
			builder.addObject(ServerHttpConnection.class).setKey(HttpHandleRedirectDependencies.SERVER_HTTP_CONNECTION);
			builder.addObject(HttpRequestState.class).setKey(HttpHandleRedirectDependencies.REQUEST_STATE);
			builder.addObject(HttpSession.class).setKey(HttpHandleRedirectDependencies.SESSION);
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the
	 * {@link InitialiseHttpRequestStateFunction}.
	 */
	@PrivateSource
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
							InitialiseHttpRequestStateDependencies.class, None.class)
					.setFunctionFactory(new InitialiseHttpRequestStateFunction());

			// Configure dependency on the request state and path arguments
			builder.addObject(HttpArgument.class).setKey(InitialiseHttpRequestStateDependencies.PATH_ARGUMENTS);
			builder.addObject(HttpRequestState.class).setKey(InitialiseHttpRequestStateDependencies.HTTP_REQUEST_STATE);
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the {@link HttpRedirectFunction}.
	 */
	@PrivateSource
	private class RedirectManagedFunctionSource extends AbstractManagedFunctionSource {

		/**
		 * {@link Redirect}.
		 */
		private final Redirect redirect;

		/**
		 * Instantiate.
		 * 
		 * @param redirect {@link Redirect}.
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
			HttpRedirectFunction<?> function = new HttpRedirectFunction<>(this.redirect.isSecure,
					this.redirect.httpPathFactory);

			// Add the redirect function
			ManagedFunctionTypeBuilder<HttpRedirectDependencies, None> builder = functionNamespaceTypeBuilder
					.addManagedFunctionType(functionName, HttpRedirectDependencies.class, None.class)
					.setFunctionFactory(function);

			// Configure dependencies
			builder.addObject(this.redirect.httpPathFactory.getValuesType())
					.setKey(HttpRedirectDependencies.PATH_VALUES);
			builder.addObject(ServerHttpConnection.class).setKey(HttpRedirectDependencies.SERVER_HTTP_CONNECTION);
			builder.addObject(HttpRequestState.class).setKey(HttpRedirectDependencies.REQUEST_STATE);
			builder.addObject(HttpSession.class).setKey(HttpRedirectDependencies.SESSION_STATE);
		}
	}

	/**
	 * {@link ManagedFunctionSource} for the {@link NotHandledFunction}.
	 */
	@PrivateSource
	private class NotHandledManagedFunctionSource extends AbstractManagedFunctionSource {

		/*
		 * ============ ManagedFunctionSource ==============
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Add the not handled function
			ManagedFunctionTypeBuilder<NotHandledDependencies, None> builder = functionNamespaceTypeBuilder
					.addManagedFunctionType(NOT_FOUND_INPUT_NAME, NotHandledDependencies.class, None.class)
					.setFunctionFactory(new NotHandledFunction());

			// Configure dependencies
			builder.addObject(ServerHttpConnection.class).setKey(NotHandledDependencies.SERVER_HTTP_CONNECTION);
			builder.addObject(WebServicer.class).setKey(NotHandledDependencies.WEB_SERVICER);

			// Configure not found escalation
			builder.addEscalation(NotFoundHttpException.class);
		}
	}

}
