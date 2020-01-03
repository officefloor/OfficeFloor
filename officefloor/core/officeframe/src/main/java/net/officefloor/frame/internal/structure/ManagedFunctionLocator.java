package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;

/**
 * Locates a {@link ManagedFunctionMetaData} within the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionLocator {

	/**
	 * Obtains the {@link ManagedFunctionMetaData}.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @return {@link ManagedFunctionMetaData} or <code>null</code> if not
	 *         found.
	 */
	ManagedFunctionMetaData<?, ?> getManagedFunctionMetaData(String functionName);

}