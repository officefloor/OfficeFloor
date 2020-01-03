package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;

/**
 * Startup task for an {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeStartupFunction {

	/**
	 * Obtains the {@link FlowMetaData} for the startup task.
	 * 
	 * @return {@link FlowMetaData} for the startup task.
	 */
	FlowMetaData getFlowMetaData();

	/**
	 * Obtains the parameter to invoke the startup {@link ManagedFunction} with.
	 * 
	 * @return Parameter for the startup {@link ManagedFunction}.
	 */
	Object getParameter();

}