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
	public void execute(ManagedFunctionContext<InitialiseHttpRequestStateDependencies, None> context) throws Throwable {

		// Obtain the dependencies
		HttpArgument arguments = (HttpArgument) context
				.getObject(InitialiseHttpRequestStateDependencies.PATH_ARGUMENTS);
		HttpRequestState requestState = (HttpRequestState) context
				.getObject(InitialiseHttpRequestStateDependencies.HTTP_REQUEST_STATE);

		// Initialise the request state
		HttpRequestStateManagedObjectSource.initialiseHttpRequestState(arguments, requestState);
	}

}