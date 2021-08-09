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
