package net.officefloor.compile.managedobject;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <code>Type definition</code> of a {@link ExecutionStrategy} required by the
 * {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExecutionStrategyType {

	/**
	 * Obtains the name to identify requirement of a {@link ExecutionStrategy}.
	 * 
	 * @return Name to identify requirement of a {@link ExecutionStrategy}.
	 */
	String getExecutionStrategyName();

}