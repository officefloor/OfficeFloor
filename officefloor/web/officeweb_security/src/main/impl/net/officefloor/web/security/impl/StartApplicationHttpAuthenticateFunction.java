package net.officefloor.web.security.impl;

import java.io.Serializable;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.web.spi.security.AuthenticationContext;

/**
 * {@link ManagedFunction} and {@link ManagedFunctionFactory} for triggering
 * authentication with application specific credentials.
 * 
 * @author Daniel Sagenschneider
 */
public class StartApplicationHttpAuthenticateFunction<AC extends Serializable, C>
		extends StaticManagedFunction<StartApplicationHttpAuthenticateFunction.Dependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		AUTHENTICATION_CONTEXT, CREDENTIALS
	}

	/*
	 * ====================== ManagedFunction =============================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void execute(ManagedFunctionContext<Dependencies, None> context) throws Throwable {

		// Obtain the dependencies
		AuthenticationContext<AC, C> authenticationContext = (AuthenticationContext<AC, C>) context
				.getObject(Dependencies.AUTHENTICATION_CONTEXT);
		C credentials = (C) context.getObject(Dependencies.CREDENTIALS);

		// Trigger authentication
		authenticationContext.authenticate(credentials, null);
	}

}