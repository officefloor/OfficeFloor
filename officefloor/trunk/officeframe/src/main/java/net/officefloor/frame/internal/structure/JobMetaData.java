/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Team;

/**
 * Meta-data for a job.
 * 
 * @author Daniel
 */
public interface JobMetaData {

	/**
	 * Obtains the {@link Team} responsible for completing the job.
	 * 
	 * @return {@link Team} responsible for completing the job.
	 */
	Team getTeam();

	/**
	 * Obtains the indexes to the {@link ManagedObject} instances that must be
	 * loaded before the {@link Task} may be executed.
	 * 
	 * @return Listing of indexes of {@link ManagedObject} instances.
	 */
	int[] getRequiredManagedObjects();

	/**
	 * Obtains the {@link EscalationProcedure} for the {@link Task} of this
	 * {@link TaskMetaData}.
	 * 
	 * @return {@link EscalationProcedure} for the {@link Task} of this
	 *         {@link TaskMetaData}.
	 */
	EscalationProcedure getEscalationProcedure();

	/**
	 * Obtains the {@link TaskMetaData} of the next {@link Task} within
	 * {@link Flow} that this {@link Task} is involved within.
	 * 
	 * @param key
	 *            Key of the {@link Flow}.
	 * @return {@link TaskMetaData} of the first {@link Task} within the
	 *         specified {@link Flow}.
	 */
	TaskMetaData<?, ?, ?, ?> getNextTaskInFlow();

}
