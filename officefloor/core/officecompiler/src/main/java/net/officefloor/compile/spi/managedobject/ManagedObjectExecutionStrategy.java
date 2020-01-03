package net.officefloor.compile.spi.managedobject;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link ExecutionStrategy} required by the {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExecutionStrategy {

	/**
	 * Obtains the name of the {@link ManagedObjectExecutionStrategy}.
	 * 
	 * @return Name of the {@link ManagedObjectExecutionStrategy}.
	 */
	String getManagedObjectExecutionStrategyName();

}