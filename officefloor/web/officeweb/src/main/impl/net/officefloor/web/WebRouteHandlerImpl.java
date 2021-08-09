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
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.route.WebRouteHandler;
import net.officefloor.web.state.HttpArgument;

/**
 * {@link WebRouteHandler} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WebRouteHandlerImpl implements WebRouteHandler {

	/**
	 * <code>Location</code> {@link HttpHeaderName}.
	 */
	private static final HttpHeaderName LOCATION = new HttpHeaderName("location");

	/**
	 * Indicates if a secure connection is required.
	 */
	private final boolean isRequireSecure;

	/**
	 * {@link Flow} index for handling.
	 */
	private final int flowIndex;

	/**
	 * Instantiate.
	 * 
	 * @param isRequireSecure
	 *            Indicates if a secure connection is required.
	 * @param flowIndex
	 *            {@link Flow} index for handling.
	 */
	public WebRouteHandlerImpl(boolean isRequireSecure, int flowIndex) {
		this.isRequireSecure = isRequireSecure;
		this.flowIndex = flowIndex;
	}

	/*
	 * ================== WebRouteHandler ===================
	 */

	@Override
	public void handle(HttpArgument pathArguments, ServerHttpConnection connection,
			ManagedFunctionContext<?, Indexed> context) {

		// Determine if secure connection is required
		if (this.isRequireSecure && (!connection.isSecure())) {
			// Non-secure, requiring secure - so redirect
			String path = connection.getRequest().getUri();
			String securePath = connection.getServerLocation().createClientUrl(this.isRequireSecure, path);

			// Send redirect response to secure path
			HttpResponse response = connection.getResponse();
			response.setStatus(HttpStatus.TEMPORARY_REDIRECT);
			response.getHeaders().addHeader(LOCATION, securePath);
			return;
		}

		// Undertake flow to service route
		context.doFlow(this.flowIndex, pathArguments, null);
	}

}
