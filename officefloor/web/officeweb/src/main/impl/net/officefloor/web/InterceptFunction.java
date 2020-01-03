package net.officefloor.web;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;

/**
 * {@link ManagedFunction} to trigger the interception before routing.
 * 
 * @author Daniel Sagenschneider
 */
public class InterceptFunction implements ManagedFunctionFactory<None, None>, ManagedFunction<None, None> {

	/*
	 * ================ ManagedFunctionFactory =================
	 */

	@Override
	public ManagedFunction<None, None> createManagedFunction() throws Throwable {
		return this;
	}

	/*
	 * =================== ManagedFunction =====================
	 */

	@Override
	public void execute(ManagedFunctionContext<None, None> context) throws Throwable {
		// Do nothing, as just linked next to interception
	}

}