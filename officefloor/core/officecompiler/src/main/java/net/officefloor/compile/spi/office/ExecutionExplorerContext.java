package net.officefloor.compile.spi.office;

import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Context for the {@link ExecutionExplorer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutionExplorerContext {

	/**
	 * Obtains the initial {@link ExecutionManagedFunction} for the
	 * {@link OfficeSectionInput}.
	 * 
	 * @return Initial {@link ExecutionManagedFunction} for the
	 *         {@link OfficeSectionInput}.
	 */
	ExecutionManagedFunction getInitialManagedFunction();

	/**
	 * <p>
	 * Obtains the {@link ExecutionManagedFunction} by {@link ManagedFunction}
	 * name.
	 * <p>
	 * This enables obtaining dynamically invoked {@link ManagedFunction}
	 * instances via execution.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @return {@link ExecutionManagedFunction}.
	 */
	ExecutionManagedFunction getManagedFunction(String functionName);

}