package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Configuration of an {@link ExecutionStrategy} utilised by a
 * {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExecutionConfiguration {

	/**
	 * Obtains the name to identify this {@link ExecutionStrategy}.
	 * 
	 * @return Name identifying this {@link ExecutionStrategy}.
	 */
	String getExecutionStrategyName();

}