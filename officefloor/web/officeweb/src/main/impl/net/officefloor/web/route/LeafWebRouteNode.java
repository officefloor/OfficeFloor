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

package net.officefloor.web.route;

import java.util.Map;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpMethod.HttpMethodEnum;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.escalation.NotFoundHttpException;
import net.officefloor.web.state.HttpArgument;

/**
 * Leaf {@link WebRouteNode} that services the {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class LeafWebRouteNode implements WebRouteNode {

	/**
	 * <code>Allow</code> {@link HttpHeaderName}.
	 */
	private static final HttpHeaderName ALLOW = new HttpHeaderName("allow");

	/**
	 * {@link LeafWebRouteHandling} instances by their {@link HttpMethod}.
	 */
	private final Map<HttpMethodEnum, LeafWebRouteHandling> handlers;

	/**
	 * Not allowed {@link WebServicer}.
	 */
	private final WebServicer notAllowedServicer;

	/**
	 * Instantiate.
	 * 
	 * @param allowedMethods      Allowed {@link HttpMethod} names.
	 * @param handlers            {@link LeafWebRouteHandling} instances by their
	 *                            {@link HttpMethod}.
	 * @param isWildcardOnlyMatch Indicates matching all paths.
	 */
	public LeafWebRouteNode(String[] allowedMethods, Map<HttpMethodEnum, LeafWebRouteHandling> handlers,
			boolean isWildcardOnlyMatch) {
		this.handlers = handlers;

		// Create the allow value
		StringBuilder value = new StringBuilder();
		boolean isFirst = true;
		for (String allowedMethod : allowedMethods) {
			if (!isFirst) {
				value.append(", ");
			}
			isFirst = false;
			value.append(allowedMethod);
		}
		HttpHeaderValue allowedMethodsValue = new HttpHeaderValue(value.toString());

		// Create web servicer for not allowed method
		if (isWildcardOnlyMatch) {
			// On wild card only match, then not found
			this.notAllowedServicer = new WebServicer() {

				@Override
				public WebRouteMatchEnum getMatchResult() {
					return WebRouteMatchEnum.NO_MATCH;
				}

				@Override
				public void service(ServerHttpConnection connection) {

					// Not found
					String requestPath = connection.getRequest().getUri();
					throw new NotFoundHttpException(requestPath);
				}
			};

		} else {
			// Not full path, so indicate method not available
			this.notAllowedServicer = new WebServicer() {

				@Override
				public WebRouteMatchEnum getMatchResult() {
					return WebRouteMatchEnum.NOT_ALLOWED_METHOD;
				}

				@Override
				public void service(ServerHttpConnection connection) {
					// Method not allowed
					HttpResponse response = connection.getResponse();
					response.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
					response.getHeaders().addHeader(ALLOW, allowedMethodsValue);
				}
			};
		}
	}

	/*
	 * ================= WebRouteNode =================
	 */

	@Override
	public WebServicer handle(HttpMethod method, String path, int index, HttpArgument headPathArgument,
			ServerHttpConnection connection, ManagedFunctionContext<?, Indexed> context) {

		// Determine if end of path
		boolean isFurtherChecks;
		boolean isEnd = false;
		do {
			isFurtherChecks = false;
			if (path.length() == index) {
				// No further characters, so end of path
				isEnd = true;

			} else {
				// Determine if end of path
				char character = path.charAt(index);
				switch (character) {
				case '?':
				case '#':
					// Path end, as starting query string or fragment
					isEnd = true;
					break;

				case '/':
					// Determine if optional ending slash
					index++; // move past '/'
					isFurtherChecks = true;
					break;
				}
			}
		} while (isFurtherChecks);

		// Determine if end
		if (!isEnd) {
			return WebServicer.NO_MATCH; // not end, so not handled
		}

		// Obtain the handler
		WebRouteHandler handler = null;
		LeafWebRouteHandling handling = this.handlers.get(method.getEnum());
		if (handling != null) {
			handler = handling.handlerFactory.apply(method);
		}

		// Undertake handling
		if (handler != null) {

			// Obtain the named path arguments
			HttpArgument namedArguments = null;
			String[] parameterNames = handling.parameterNamesFactory.apply(method);
			if (parameterNames.length > 0) {

				// Load the last parameter
				String name = parameterNames[parameterNames.length - 1];
				namedArguments = new HttpArgument(name, headPathArgument.value, HttpValueLocation.PATH);
				headPathArgument = headPathArgument.next;

				// Load the remaining parameters
				for (int i = parameterNames.length - 2; i >= 0; i--) {
					name = parameterNames[i];

					// Add in the parameter
					HttpArgument nextArgument = new HttpArgument(name, headPathArgument.value, HttpValueLocation.PATH);
					nextArgument.next = namedArguments;
					namedArguments = nextArgument;

					// Move to next argument
					headPathArgument = headPathArgument.next;
				}
			}

			// Service the request
			WebRouteHandler finalHandler = handler;
			HttpArgument finalNamedArguments = namedArguments;
			return new WebServicer() {

				@Override
				public WebRouteMatchEnum getMatchResult() {
					return WebRouteMatchEnum.MATCH;
				}

				@Override
				public void service(ServerHttpConnection connection) {
					finalHandler.handle(finalNamedArguments, connection, context);
				}
			};

		} else {
			// Method not allowed
			return this.notAllowedServicer;
		}
	}

}
