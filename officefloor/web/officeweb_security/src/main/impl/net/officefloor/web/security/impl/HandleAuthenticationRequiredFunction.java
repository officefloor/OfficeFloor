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
