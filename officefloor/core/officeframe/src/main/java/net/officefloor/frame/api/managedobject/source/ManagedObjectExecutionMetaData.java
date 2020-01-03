package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.executive.ExecutionStrategy;

/**
 * Describes an {@link ExecutionStrategy} required by the
 * {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExecutionMetaData {

	/**
	 * Provides a descriptive name for this {@link ExecutionStrategy}. This is
	 * useful to better describe the {@link ExecutionStrategy}.
	 * 
	 * @return Descriptive name for this {@link ExecutionStrategy}.
	 */
	String getLabel();

}
