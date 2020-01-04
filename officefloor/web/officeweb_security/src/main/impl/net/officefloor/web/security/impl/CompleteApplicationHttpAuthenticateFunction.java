/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.security.impl;

import java.io.Serializable;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.AuthenticationContinuationException;
import net.officefloor.web.state.HttpRequestState;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource;

/**
 * {@link ManagedFunctionFactory} for completing authentication with application
 * specific credentials.
 * 
 * @author Daniel Sagenschneider
 */
public class CompleteApplicationHttpAuthenticateFunction<AC extends Serializable>
		extends StaticManagedFunction<CompleteApplicationHttpAuthenticateFunction.Dependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		ACCESS_CONTROL, SERVER_HTTP_CONNECTION, HTTP_SESSION, REQUEST_STATE
	}

	/*
	 * ========================= ManagedFunction =============================
	 */

	@Override
	public void execute(ManagedFunctionContext<Dependencies, None> context) throws Throwable {

		// Obtain the access control (handles not logged in)
		context.getObject(Dependencies.ACCESS_CONTROL);

		// Reinstate request for servicing prior to authentication required
		ServerHttpConnection connection = (ServerHttpConnection) context.getObject(Dependencies.SERVER_HTTP_CONNECTION);
		HttpSession session = (HttpSession) context.getObject(Dependencies.HTTP_SESSION);
		HttpRequestState requestState = (HttpRequestState) context.getObject(Dependencies.REQUEST_STATE);
		Serializable momento = session
				.getAttribute(HandleAuthenticationRequiredFunction.ATTRIBUTE_CHALLENGE_REQUEST_MOMENTO);
		if (momento != null) {
			// Reinstate the connection and request state
			ChallengeMomento challenge = (ChallengeMomento) momento;
			connection.importState(challenge.getServerHttpConnectionMomento());
			HttpRequestStateManagedObjectSource.importHttpRequestState(challenge.getHttpRequestStateMomento(),
					requestState);

			// Clear the moment
			session.removeAttribute(HandleAuthenticationRequiredFunction.ATTRIBUTE_CHALLENGE_REQUEST_MOMENTO);

		} else {
			// Failure as must reinstate request
			throw new AuthenticationContinuationException("No previous request state to continue after login");
		}
	}

}
