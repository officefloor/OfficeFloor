package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectExecutionStrategy;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectExecutionStrategy;

/**
 * {@link OfficeFloorManagedObjectExecutionStrategy} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExecutionStrategyNode extends LinkExecutionStrategyNode,
		AugmentedManagedObjectExecutionStrategy, OfficeFloorManagedObjectExecutionStrategy {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Managed Object Source Execution Strategy";

	/**
	 * Initialises the {@link ManagedObjectExecutionStrategyNode}.
	 */
	void initialise();

}