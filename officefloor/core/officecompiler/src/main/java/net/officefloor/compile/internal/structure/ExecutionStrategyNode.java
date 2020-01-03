package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.officefloor.OfficeFloorExecutionStrategy;

/**
 * {@link OfficeFloorExecutionStrategy} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutionStrategyNode extends LinkExecutionStrategyNode, OfficeFloorExecutionStrategy {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Execution Strategy";

	/**
	 * Initialises the {@link ExecutionStrategyNode}.
	 */
	void initialise();

}