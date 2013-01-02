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
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * Activity regarding the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceActivity<I, F extends Enum<F>> {

	/**
	 * Obtains the {@link GovernanceMetaData} for this
	 * {@link GovernanceActivity}.
	 * 
	 * @return {@link GovernanceMetaData} for this {@link GovernanceActivity}.
	 */
	GovernanceMetaData<I, F> getGovernanceMetaData();

	/**
	 * Undertakes an activity regarding the {@link Governance}.
	 * 
	 * @param governanceContext
	 *            {@link GovernanceContext}
	 * @param jobContext
	 *            {@link JobContext}.
	 * @param jobNode
	 *            {@link JobNode}.
	 * @param activateSet
	 *            {@link JobNodeActivateSet}.
	 * @param currentTeam
	 *            {@link TeamIdentifier} of the current {@link Team} undertaking
	 *            the activity.
	 * @param containerContext
	 *            {@link ContainerContext}.
	 * @return <code>true</code> should activity be successfully trigger. In
	 *         other words, does not need re-executing as waiting on
	 *         {@link ManagedObject}.
	 * @throws Throwable
	 *             If activity fails.
	 */
	boolean doActivity(GovernanceContext<F> governanceContext,
			JobContext jobContext, JobNode jobNode,
			JobNodeActivateSet activateSet, TeamIdentifier currentTeam,
			ContainerContext containerContext) throws Throwable;

}