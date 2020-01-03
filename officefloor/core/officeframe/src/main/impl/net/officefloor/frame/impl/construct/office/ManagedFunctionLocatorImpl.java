package net.officefloor.frame.impl.construct.office;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;

/**
 * {@link ManagedFunctionLocator} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionLocatorImpl implements ManagedFunctionLocator {

	/**
	 * {@link ManagedFunctionMetaData} instances of the {@link Office}.
	 */
	private final Map<String, ManagedFunctionMetaData<?, ?>> officeFunctions = new HashMap<String, ManagedFunctionMetaData<?, ?>>();

	/**
	 * Initiate.
	 * 
	 * @param allFunctionMetaData
	 *            Listing of all {@link ManagedFunctionMetaData} within the
	 *            {@link Office}.
	 */
	public ManagedFunctionLocatorImpl(ManagedFunctionMetaData<?, ?>[] allFunctionMetaData) {

		// Create the mapping of functions for easier access
		for (ManagedFunctionMetaData<?, ?> function : allFunctionMetaData) {
			this.officeFunctions.put(function.getFunctionName(), function);
		}
	}

	/*
	 * ================== ManagedFunctionLocator ===============================
	 */

	@Override
	public ManagedFunctionMetaData<?, ?> getManagedFunctionMetaData(String functionName) {
		return this.officeFunctions.get(functionName);
	}

}