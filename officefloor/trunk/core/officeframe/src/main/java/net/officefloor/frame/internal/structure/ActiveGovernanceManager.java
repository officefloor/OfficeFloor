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
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * Provides management functions to the {@link ActiveGovernance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ActiveGovernanceManager<I, F extends Enum<F>> {

	/**
	 * Obtains the {@link ActiveGovernance} being managed.
	 * 
	 * @return {@link ActiveGovernance} being managed.
	 */
	ActiveGovernance<I, F> getActiveGovernance();

	/**
	 * Indicates if the {@link ManagedObject} is ready.
	 * 
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode}.
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 * @param context
	 *            {@link ContainerContext}.
	 * @return <code>true</code> if the {@link ManagedObject} is ready.
	 */
	boolean isManagedObjectReady(JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, ContainerContext context);

	/**
	 * Unregisters the {@link ManagedObject} from {@link Governance}.
	 * 
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team}
	 *            unregistering the {@link ManagedObject}.
	 */
	void unregisterManagedObject(JobNodeActivateSet activateSet,
			TeamIdentifier currentTeam);

}