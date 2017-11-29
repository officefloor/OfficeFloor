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