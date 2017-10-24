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

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.web.state.HttpArgument;
import net.officefloor.web.state.HttpRequestState;
import net.officefloor.web.state.HttpRequestStateManagedObjectSource;

/**
 * {@link ManagedFunction} to initialise the {@link HttpRequestState}.
 * 
 * @author Daniel Sagenschneider
 */
public class InitialiseHttpRequestStateFunction implements
		ManagedFunctionFactory<InitialiseHttpRequestStateFunction.InitialiseHttpRequestStateDependencies, None>,
		ManagedFunction<InitialiseHttpRequestStateFunction.InitialiseHttpRequestStateDependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum InitialiseHttpRequestStateDependencies {
		PATH_ARGUMENTS, HTTP_REQUEST_STATE
	}

	/*
	 * ================ ManagedFunctionFactory ===============
	 */

	@Override
	public ManagedFunction<InitialiseHttpRequestStateDependencies, None> createManagedFunction() throws Throwable {
		return this;
	}

	/*
	 * ==================== ManagedFunction ==================
	 */

	@Override
	public Object execute(ManagedFunctionContext<InitialiseHttpRequestStateDependencies, None> context)
			throws Throwable {

		// Obtain the dependencies
		HttpArgument arguments = (HttpArgument) context
				.getObject(InitialiseHttpRequestStateDependencies.PATH_ARGUMENTS);
		HttpRequestState requestState = (HttpRequestState) context
				.getObject(InitialiseHttpRequestStateDependencies.HTTP_REQUEST_STATE);

		// Initialise the request state
		HttpRequestStateManagedObjectSource.initialiseHttpRequestState(arguments, requestState);

		// Carry on to service request
		return null;
	}

}