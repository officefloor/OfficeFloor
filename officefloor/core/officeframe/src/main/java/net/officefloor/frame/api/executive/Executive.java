package net.officefloor.frame.api.executive;

import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.internal.structure.Execution;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Executive.
 * 
 * @author Daniel Sagenschneider
 */
public interface Executive {

	/**
	 * Creates a new {@link ProcessState} identifier.
	 * 
	 * @return New {@link ProcessState} identifier.
	 */
	default Object createProcessIdentifier() {
		return new Object();
	}

	/**
	 * <p>
	 * Manages the {@link Execution}.
	 * <p>
	 * The {@link Thread#currentThread()} will provide the inbound {@link Thread}.
	 * 
	 * @param           <T> Type of {@link Throwable} thrown.
	 * @param execution {@link Execution} to be undertaken.
	 * @return {@link ProcessManager} for the {@link ProcessState}.
	 * @throws T Propagation of failure from {@link Execution}.
	 */
	default <T extends Throwable> ProcessManager manageExecution(Execution<T> execution) throws T {
		return execution.execute();
	}

	/**
	 * Obtains the {@link ExecutionStrategy} strategies.
	 * 
	 * @return {@link ExecutionStrategy} instances.
	 */
	ExecutionStrategy[] getExcutionStrategies();

	/**
	 * Obtains the {@link TeamOversight} instances.
	 * 
	 * @return {@link TeamOversight} instances.
	 */
	default TeamOversight[] getTeamOversights() {
		return new TeamOversight[0]; // no oversight by default
	}

}