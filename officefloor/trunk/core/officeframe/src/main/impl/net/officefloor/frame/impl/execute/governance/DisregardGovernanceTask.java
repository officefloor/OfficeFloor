/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.internal.structure.GovernanceControl;
import net.officefloor.frame.spi.governance.Governance;

/**
 * {@link Task} to disregard the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class DisregardGovernanceTask<F extends Enum<F>> implements
		TaskFactory<GovernanceWork, GovernanceTaskDependency, F>,
		Task<GovernanceWork, GovernanceTaskDependency, F> {

	/*
	 * ==================== TaskFactory ==============================
	 */

	@Override
	public Task<GovernanceWork, GovernanceTaskDependency, F> createTask(
			GovernanceWork work) {
		return this;
	}

	/*
	 * ======================== Task =================================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public Object doTask(
			TaskContext<GovernanceWork, GovernanceTaskDependency, F> context)
			throws Throwable {

		// Obtain the governance control
		GovernanceControl<?, F> governanceControl = (GovernanceControl<?, F>) context
				.getObject(GovernanceTaskDependency.GOVERNANCE_CONTROL);

		// Disregard the governance
		governanceControl.disregardGovernance(context);

		// Should be no next task
		return null;
	}

}