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
package net.officefloor.web.security.impl;

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
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * {@link ManagedFunction} to handle the
 * {@link AuthenticationRequiredException}.
 * 
 * @author Daniel Sagenschneider
 */
public class HandleAuthenticationRequiredFunction
		extends StaticManagedFunction<HandleAuthenticationRequiredFunction.Dependencies, Indexed> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		AUTHENTICATION_REQUIRED_EXCEPTION, SERVER_HTTP_CONNECTION
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
	 * @param httpSecurityNameToFlowIndex
	 *            {@link HttpSecurity} name to {@link Flow} index (by array
	 *            index).
	 * @param challengeNegotiator
	 *            Challenge {@link AcceptNegotiator}.
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
	public Object execute(ManagedFunctionContext<Dependencies, Indexed> context) throws Throwable {

		// Obtain the dependencies
		AuthenticationRequiredException exception = (AuthenticationRequiredException) context
				.getObject(Dependencies.AUTHENTICATION_REQUIRED_EXCEPTION);
		ServerHttpConnection connection = (ServerHttpConnection) context.getObject(Dependencies.SERVER_HTTP_CONNECTION);

		// Determine if requiring specific security
		String requiredHttpSecurityName = exception.getRequiredHttpSecurityName();
		if (requiredHttpSecurityName != null) {

			// Obtain the handling flow
			for (int i = 0; i < this.httpSecurityNameToFlowIndex.length; i++) {
				if (requiredHttpSecurityName.equals(this.httpSecurityNameToFlowIndex[i])) {
					context.doFlow(i, null, null);
					return null;
				}
			}

			// As here, did not find security flow
			throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null, "No " + HttpSecurity.class.getSimpleName()
					+ " configured for name '" + requiredHttpSecurityName + "'");
		}

		// Determine if just the single security
		if (this.httpSecurityNameToFlowIndex.length == 1) {
			// Trigger the only security
			context.doFlow(0, null, null);
			return null;
		}

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

		// Nothing further
		return null;
	}

}