/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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

import java.io.Serializable;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.AuthenticationContinuationError;
import net.officefloor.web.state.HttpRequestState;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource;

/**
 * {@link ManagedFunctionFactory} for completing authentication with application
 * specific credentials.
 * 
 * @author Daniel Sagenschneider
 */
public class CompleteApplicationHttpAuthenticateFunction<AC extends Serializable> extends
		StaticManagedFunction<CompleteApplicationHttpAuthenticateFunction.Dependencies, CompleteApplicationHttpAuthenticateFunction.Flows> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		ACCESS_CONTROL, HTTP_SESSION, REQUEST_STATE
	}

	/**
	 * Flow keys.
	 */
	public static enum Flows {
		FAILURE
	}

	/*
	 * ========================= ManagedFunction =============================
	 */

	@Override
	public Object execute(ManagedFunctionContext<Dependencies, Flows> context) throws Throwable {

		// Obtain the access control (handles not logged in)
		context.getObject(Dependencies.ACCESS_CONTROL);

		// Reinstate request for servicing prior to authentication required
		HttpSession session = (HttpSession) context.getObject(Dependencies.HTTP_SESSION);
		HttpRequestState requestState = (HttpRequestState) context.getObject(Dependencies.REQUEST_STATE);
		Serializable momento = session.getAttribute(HttpChallengeFunction.ATTRIBUTE_CHALLENGE_REQUEST_MOMENTO);
		if (momento != null) {
			// Reinstate the request
			HttpRequestStateManagedObjectSource.importHttpRequestState(momento, requestState);
		} else {
			// Failure as must reinstate request
			context.doFlow(Flows.FAILURE, new AuthenticationContinuationError(), null);
			return null; // continuation failure
		}

		// No further functions
		return null;
	}

}