package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.team.Team;

/**
 * Provides details to avoid execution with a particular {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AvoidTeam {

	/**
	 * Obtains the {@link FunctionState} to continue execution avoiding the
	 * specified {@link Team}.
	 * 
	 * @return {@link FunctionState} to continue execution avoiding the specified
	 *         {@link Team}.
	 */
	FunctionState getFunctionState();

	/**
	 * Flags to stop avoiding the {@link Team}.
	 */
	void stopAvoidingTeam();

}