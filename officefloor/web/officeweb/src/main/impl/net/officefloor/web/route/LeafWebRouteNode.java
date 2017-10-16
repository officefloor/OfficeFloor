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
package net.officefloor.web.route;

import java.util.Map;
import java.util.function.Function;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpMethod.HttpMethodEnum;
import net.officefloor.web.state.HttpArgument;
import net.officefloor.server.http.HttpRequest;

/**
 * Leaf {@link WebRouteNode} that services the {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class LeafWebRouteNode implements WebRouteNode {

	/**
	 * {@link WebRouteHandler} instances by their {@link HttpMethod}.
	 */
	private final Map<HttpMethodEnum, Function<HttpMethod, WebRouteHandler>> handlers;

	/**
	 * Instantiate.
	 * 
	 * @param handlers
	 *            {@link WebRouteHandler} instances by their {@link HttpMethod}.
	 */
	public LeafWebRouteNode(Map<HttpMethodEnum, Function<HttpMethod, WebRouteHandler>> handlers) {
		this.handlers = handlers;
	}

	/*
	 * ================= WebRouteNode =================
	 */

	@Override
	public boolean handle(HttpMethod method, String path, int index, HttpArgument headPathArgument,
			ManagedFunctionContext<?, Indexed> context) {

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
			return false; // not end, so not handled
		}

		// Obtain the handler
		WebRouteHandler handler = null;
		Function<HttpMethod, WebRouteHandler> handlerFactory = this.handlers.get(method.getEnum());
		if (handlerFactory != null) {
			handler = handlerFactory.apply(method);
		}

		// Undertake handling
		if (handler != null) {
			handler.handle(headPathArgument, context);
		}

		// Handled
		return true;
	}

}