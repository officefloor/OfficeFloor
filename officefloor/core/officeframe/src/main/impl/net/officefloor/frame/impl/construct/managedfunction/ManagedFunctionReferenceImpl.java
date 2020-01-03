package net.officefloor.frame.impl.construct.managedfunction;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;

/**
 * {@link ManagedFunctionReference} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionReferenceImpl implements ManagedFunctionReference {

	/**
	 * Name of the {@link ManagedFunction}.
	 */
	private final String functionName;

	/**
	 * Type of argument to be passed to the referenced {@link ManagedFunction}.
	 */
	private final Class<?> argumentType;

	/**
	 * Initiate.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @param argumentType
	 *            Type of argument to be passed to the referenced
	 *            {@link ManagedFunction}.
	 */
	public ManagedFunctionReferenceImpl(String functionName, Class<?> argumentType) {
		this.functionName = functionName;
		this.argumentType = argumentType;
	}

	/*
	 * =================== ManagedFunctionReference ===================
	 */

	@Override
	public String getFunctionName() {
		return this.functionName;
	}

	@Override
	public Class<?> getArgumentType() {
		return this.argumentType;
	}

}