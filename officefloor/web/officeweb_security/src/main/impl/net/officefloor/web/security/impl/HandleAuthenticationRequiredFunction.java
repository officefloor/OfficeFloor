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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.accept.AcceptNegotiator;
import net.officefloor.web.security.AuthenticationRequiredException;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.state.HttpRequestState;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource;

/**
 * {@link ManagedFunction} to handle the
 * {@link AuthenticationRequiredException}.
 * 
 * @author Daniel Sagenschneider
 */
public class HandleAuthenticationRequiredFunction
		extends StaticManagedFunction<HandleAuthenticationRequiredFunction.Dependencies, Indexed> {

	/**
	 * {@link HttpSession} attribute for the challenge request state.
	 */
	public static final String ATTRIBUTE_CHALLENGE_REQUEST_MOMENTO = "CHALLENGE_REQUEST_MOMENTO";

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		AUTHENTICATION_REQUIRED_EXCEPTION, SERVER_HTTP_CONNECTION, HTTP_SESSION, REQUEST_STATE
	}

	/**
	 * {@link HttpSecurity} name to {@link Flow} index (by array index).
	 */
	private final String[] httpSecurityNameToFlowIndex;

	/**
	 * Challenge {@link AcceptNegotiator}.
	 */
	private final AcceptNegotiator<int[]> challengeNegotiator;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecurityNameToFlowIndex {@link HttpSecurity} name to {@link Flow}
	 *                                    index (by array index).
	 * @param challengeNegotiator         Challenge {@link AcceptNegotiator}.
	 */
	public HandleAuthenticationRequiredFunction(String[] httpSecurityNameToFlowIndex,
			AcceptNegotiator<int[]> challengeNegotiator) {
		this.httpSecurityNameToFlowIndex = httpSecurityNameToFlowIndex;
		this.challengeNegotiator = challengeNegotiator;
	}

	/*
	 * ================== ManagedFunction ====================
	 */

	@Override
	public void execute(ManagedFunctionContext<Dependencies, Indexed> context) throws Throwable {

		// Obtain the dependencies
		AuthenticationRequiredException exception = (AuthenticationRequiredException) context
				.getObject(Dependencies.AUTHENTICATION_REQUIRED_EXCEPTION);
		ServerHttpConnection connection = (ServerHttpConnection) context.getObject(Dependencies.SERVER_HTTP_CONNECTION);
		HttpSession session = (HttpSession) context.getObject(Dependencies.HTTP_SESSION);
		HttpRequestState requestState = (HttpRequestState) context.getObject(Dependencies.REQUEST_STATE);

		// Save the connection and request state
		Serializable challengeMomento = session.getAttribute(ATTRIBUTE_CHALLENGE_REQUEST_MOMENTO);
		if (challengeMomento == null) {
			Serializable connectionMomento = connection.exportState();
			Serializable requestStateMomento = HttpRequestStateManagedObjectSource.exportHttpRequestState(requestState);
			session.setAttribute(ATTRIBUTE_CHALLENGE_REQUEST_MOMENTO,
					new ChallengeMomento(connectionMomento, requestStateMomento));
		}

		// Determine if requiring specific security
		String requiredHttpSecurityName = exception.getRequiredHttpSecurityName();
		if (requiredHttpSecurityName != null) {

			// Obtain the handling flow
			boolean isSecurityFound = false;
			for (int i = 0; i < this.httpSecurityNameToFlowIndex.length; i++) {
				String httpSecurityName = this.httpSecurityNameToFlowIndex[i];
				if (requiredHttpSecurityName.equals(httpSecurityName)) {
					context.doFlow(i, null, null);
					isSecurityFound = true;
				}
			}

			// Ensure a flow is invoked
			if (!isSecurityFound) {
				throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null,
						"No " + HttpSecurity.class.getSimpleName() + " configured for name '" + requiredHttpSecurityName
								+ "'");
			}

		} else if (this.httpSecurityNameToFlowIndex.length == 1) {
			// Trigger the only security
			context.doFlow(0, null, null);

		} else {
			// Obtain the negotiated challenges
			int[] flowIndexes = this.challengeNegotiator.getHandler(connection.getRequest());
			if (flowIndexes == null) {
				throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null,
						"No " + HttpSecurity.class.getSimpleName() + " available in accept negotiation");
			}

			// Trigger the challenge flows
			for (int i = 0; i < flowIndexes.length; i++) {
				int flowIndex = flowIndexes[i];
				context.doFlow(flowIndex, null, null);
			}
		}

		// Lastly, trigger sending challenge after challenges
		context.doFlow(HandleAuthenticationRequiredFunction.this.httpSecurityNameToFlowIndex.length, null, null);
	}

}
