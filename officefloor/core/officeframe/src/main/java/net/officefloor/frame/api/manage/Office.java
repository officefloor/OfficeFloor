package net.officefloor.frame.api.manage;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Asset;

/**
 * Office within the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Office {

	/**
	 * Manually runs the {@link Asset} checks for the {@link Office}.
	 */
	void runAssetChecks();

	/**
	 * <p>
	 * Obtains the names of the {@link FunctionManager} instances within this
	 * {@link Office}.
	 * <p>
	 * This allows to dynamically manage this {@link Office}.
	 * 
	 * @return Names of the {@link FunctionManager} instances within this
	 *         {@link Office}.
	 */
	String[] getFunctionNames();

	/**
	 * Obtains the {@link FunctionManager} for the named
	 * {@link ManagedFunction}.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @return {@link FunctionManager} for the named {@link ManagedFunction}.
	 * @throws UnknownFunctionException
	 *             If unknown {@link ManagedFunction} name.
	 */
	FunctionManager getFunctionManager(String functionName) throws UnknownFunctionException;

}