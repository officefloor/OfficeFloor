package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.team.Team;

/**
 * Executes the {@link FunctionState} instances.
 *
 * @author Daniel Sagenschneider
 */
public interface FunctionLoop {

	/**
	 * Executes the {@link FunctionState} within the current {@link Thread}.
	 * 
	 * @param function
	 *            {@link FunctionState} to execute.
	 */
	void executeFunction(FunctionState function);

	/**
	 * Delegates the {@link FunctionState} to the appropriate {@link Team} to
	 * execute.
	 * 
	 * @param function
	 *            {@link FunctionState} to delegate to a {@link Team}.
	 */
	void delegateFunction(FunctionState function);

}