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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.state.HttpRequestState;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource;

/**
 * {@link ManagedFunction} to handle the redirect.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HttpHandleRedirectFunction
		implements ManagedFunctionFactory<HttpHandleRedirectFunction.HttpHandleRedirectDependencies, Indexed>,
		ManagedFunction<HttpHandleRedirectFunction.HttpHandleRedirectDependencies, Indexed> {

	/**
	 * Dependency keys.
	 */
	public static enum HttpHandleRedirectDependencies {
		COOKIE, SERVER_HTTP_CONNECTION, REQUEST_STATE, SESSION
	}

	/**
	 * {@link HttpRouter}.
	 */
	private final HttpRouter router;

	/**
	 * Instantiate.
	 * 
	 * @param router
	 *            {@link HttpRouter}.
	 */
	public HttpHandleRedirectFunction(HttpRouter router) {
		this.router = router;
	}

	/**
	 * ================= ManagedFunctionFactory ==================
	 */

	@Override
	public ManagedFunction<HttpHandleRedirectDependencies, Indexed> createManagedFunction() throws Throwable {
		return this;
	}

	/**
	 * ==================== ManagedFunction ======================
	 */

	@Override
	public Object execute(ManagedFunctionContext<HttpHandleRedirectDependencies, Indexed> context) throws Throwable {

		// Obtain the dependencies
		HttpRequestCookie cookie = (HttpRequestCookie) context.getObject(HttpHandleRedirectDependencies.COOKIE);
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(HttpHandleRedirectDependencies.SERVER_HTTP_CONNECTION);
		HttpRequestState requestState = (HttpRequestState) context
				.getObject(HttpHandleRedirectDependencies.REQUEST_STATE);
		HttpSession session = (HttpSession) context.getObject(HttpHandleRedirectDependencies.SESSION);

		// Obtain the serialised state (ensuring the intended state)
		SerialisedRequestState serialisable = (SerialisedRequestState) session
				.getAttribute(HttpRedirectFunction.SESSION_ATTRIBUTE_REDIRECT_MOMENTO);
		if (serialisable == null) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null, "No momento for redirection");
		}
		String serialisableIdentifier = String.valueOf(serialisable.identifier);
		if (!serialisableIdentifier.equals(cookie.getValue())) {
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null, "Redirect momento invalid version");
		}

		// Re-instate the request state
		HttpRequestStateManagedObjectSource.importHttpRequestState(serialisable.momento, requestState);

		// Undertake routing (with re-instated request state)
		return this.router.route(connection, context);
	}

}