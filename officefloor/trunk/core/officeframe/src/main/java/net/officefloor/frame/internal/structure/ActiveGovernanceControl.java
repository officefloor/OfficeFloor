/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.GovernanceContext;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.JobContext;

/**
 * Provides control over the {@link ActiveGovernance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ActiveGovernanceControl<F extends Enum<F>> {

	/**
	 * Provides the {@link Governance} over the {@link ManagedObject}.
	 *
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode}.
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 * @param context
	 *            {@link ContainerContext}.
	 * @return <code>true</code> if complete.
	 * @throws Throwable
	 *             If fails to provide {@link Governance} over the
	 *             {@link ManagedObject}.
	 */
	boolean governManagedObject(GovernanceContext<F> governanceContext,
			JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, ContainerContext context)
			throws Throwable;

}