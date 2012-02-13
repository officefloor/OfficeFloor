/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.ActiveGovernanceControl;
import net.officefloor.frame.internal.structure.ContainerContext;
import net.officefloor.frame.internal.structure.GovernanceControl;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.JobNodeActivateSet;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.JobContext;

/**
 * {@link Task} to provide {@link Governance} for a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernGovernanceActivity<I, F extends Enum<F>> extends
		AbstractGovernanceActivity<I, F, ActiveGovernanceControl<F>> {

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link GovernanceMetaData}.
	 * @param governanceControl
	 *            {@link GovernanceControl}.
	 */
	public GovernGovernanceActivity(GovernanceMetaData<I, F> metaData,
			ActiveGovernanceControl<F> governanceControl) {
		super(metaData, governanceControl);
	}

	/*
	 * ==================== GovernanceActivity ==============================
	 */

	@Override
	public boolean doActivity(GovernanceContext<F> governanceContext,
			JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, ContainerContext containerContext)
			throws Throwable {

		// Govern the managed object
		return this.governanceControl.governManagedObject(governanceContext,
				jobContext, jobNode, activateSet, containerContext);
	}

}