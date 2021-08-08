/*-
 * #%L
 * Web Security
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
