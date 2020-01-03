package net.officefloor.compile.executive;

import net.officefloor.frame.api.executive.ExecutionStrategy;

/**
 * <code>Type definition</code> of an {@link ExecutionStrategy}.
 *
 * @author Daniel Sagenschneider
 */
public interface ExecutionStrategyType {

	/**
	 * Obtains the name of the {@link ExecutionStrategy}.
	 * 
	 * @return Name of the {@link ExecutionStrategy}.
	 */
	String getExecutionStrategyName();

}