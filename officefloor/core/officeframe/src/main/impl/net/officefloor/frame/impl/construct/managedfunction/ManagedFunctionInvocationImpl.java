package net.officefloor.frame.impl.construct.managedfunction;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.configuration.ManagedFunctionInvocation;

/**
 * {@link ManagedFunctionInvocation} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionInvocationImpl implements ManagedFunctionInvocation {

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private final String functionName;

	/**
	 * Argument to the {@link ManagedFunction}. May be <code>null</code>.
	 */
	private final Object argument;

	/**
	 * Instantiate.
	 * 
	 * @param functionName Name of the {@link ManagedFunction}.
	 * @param argument     Argument to the {@link ManagedFunction}. May be
	 *                     <code>null</code>.
	 */
	public ManagedFunctionInvocationImpl(String functionName, Object argument) {
		this.functionName = functionName;
		this.argument = argument;
	}

	/*
	 * ===================== ManagedFunctionInvocation ======================
	 */

	@Override
	public String getFunctionName() {
		return this.functionName;
	}

	@Override
	public Object getArgument() {
		return this.argument;
	}

}