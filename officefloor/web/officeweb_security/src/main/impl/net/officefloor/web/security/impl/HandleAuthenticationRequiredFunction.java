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

import java.util.Map;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
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
		AUTHENTICATION_REQUIRED_EXCEPTION
	}

	/**
	 * {@link Map} of {@link HttpSecurity} name to {@link Flow} index.
	 */
	private final Map<String, Integer> httpSecurityNameToFlowIndex;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecurityNameToFlowIndex
	 *            {@link Map} of {@link HttpSecurity} name to {@link Flow}
	 *            index.
	 */
	public HandleAuthenticationRequiredFunction(Map<String, Integer> httpSecurityNameToFlowIndex) {
		this.httpSecurityNameToFlowIndex = httpSecurityNameToFlowIndex;
	}

	/*
	 * ================== ManagedFunction ====================
	 */

	@Override
	public Object execute(ManagedFunctionContext<Dependencies, Indexed> context) throws Throwable {

		// Obtain the authentication required exception
		AuthenticationRequiredException exception = (AuthenticationRequiredException) context
				.getObject(Dependencies.AUTHENTICATION_REQUIRED_EXCEPTION);

		// Determine if requiring specific security
		String requiredHttpSecurityName = exception.getRequiredHttpSecurityName();
		if (requiredHttpSecurityName != null) {

			// Obtain the handling flow
			Integer flowIndex = this.httpSecurityNameToFlowIndex.get(requiredHttpSecurityName);
			if (flowIndex == null) {
				throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, null,
						"No " + HttpSecurity.class.getSimpleName() + " configured for name '" + requiredHttpSecurityName
								+ "'");
			}

			// Trigger the flow to challenge
			context.doFlow(flowIndex, null, null);
		}

		// Determine if just the single security
		if (this.httpSecurityNameToFlowIndex.size() == 1) {
			// Trigger the only security
			context.doFlow(0, null, null);
			return null;
		}

		// Nothing further
		return null;
	}

}