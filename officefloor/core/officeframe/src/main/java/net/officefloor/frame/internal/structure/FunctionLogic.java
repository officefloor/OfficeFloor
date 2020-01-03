package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.team.Team;

/**
 * Encapsulates simple logic for a {@link FunctionState}.
 *
 * @author Daniel Sagenschneider
 */
public interface FunctionLogic {

	/**
	 * Obtains the responsible {@link TeamManagement} for this
	 * {@link FunctionLogic}.
	 * 
	 * @return {@link TeamManagement} responsible for this
	 *         {@link FunctionLogic}. May be <code>null</code> to use any
	 *         {@link Team}.
	 */
	default TeamManagement getResponsibleTeam() {
		return null;
	}

	/**
	 * Indicates if the {@link FunctionLogic} requires {@link ThreadState}
	 * safety.
	 * 
	 * @return <code>true</code> should {@link FunctionLogic} require
	 *         {@link ThreadState} safety.
	 */
	default boolean isRequireThreadStateSafety() {
		return false;
	}

	/**
	 * Executes the logic.
	 * 
	 * @param flow
	 *            {@link Flow} that contains this {@link FunctionLogic}.
	 * @return Optional {@link FunctionState} to execute next.
	 * @throws Throwable
	 *             If logic fails.
	 */
	FunctionState execute(Flow flow) throws Throwable;

}