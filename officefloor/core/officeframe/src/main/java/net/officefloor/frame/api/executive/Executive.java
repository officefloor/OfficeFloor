/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.api.executive;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.internal.structure.Execution;
import net.officefloor.frame.internal.structure.OfficeManager;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Executive.
 * 
 * @author Daniel Sagenschneider
 */
public interface Executive {

	/**
	 * Creates a new {@link ProcessIdentifier}.
	 * 
	 * @param officeContext {@link ExecutiveOfficeContext}.
	 * @return New {@link ProcessIdentifier}.
	 */
	default ProcessIdentifier createProcessIdentifier(ExecutiveOfficeContext officeContext) {
		return new ProcessIdentifier() {
		};
	}

	/**
	 * <p>
	 * Manages the {@link Execution}.
	 * <p>
	 * The {@link Thread#currentThread()} will provide the inbound {@link Thread}.
	 * 
	 * @param <T>       Type of {@link Throwable} thrown.
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

	/**
	 * Starts managing the {@link OfficeFloor}.
	 * 
	 * @throws Exception If fails to start managing.
	 */
	void startManaging() throws Exception;

	/**
	 * Obtains the {@link OfficeManager} for the {@link ProcessState}.
	 * 
	 * @param processIdentifier    {@link ProcessIdentifier} created by this
	 *                             {@link Executive} for the {@link ProcessState}.
	 * @param defaultOfficeManager Default {@link OfficeManager}.
	 * @return {@link OfficeManager} for the {@link ProcessState}.
	 */
	default OfficeManager getOfficeManager(ProcessIdentifier processIdentifier, OfficeManager defaultOfficeManager) {
		// By default, use default Office Manager
		return defaultOfficeManager;
	}

	/**
	 * Stops managing the {@link OfficeFloor}.
	 * 
	 * @throws Exception If fails to stop managing.
	 */
	void stopManaging() throws Exception;

}
