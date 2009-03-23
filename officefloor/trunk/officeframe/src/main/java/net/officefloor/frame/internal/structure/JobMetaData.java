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
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;

/**
 * Meta-data for a {@link Job}.
 * 
 * @author Daniel
 */
public interface JobMetaData {

	/**
	 * Creates the {@link JobActivatableSet} for executing the {@link Job}.
	 * 
	 * @return {@link JobActivatableSet}.
	 */
	JobActivatableSet createJobActivableSet();

	/**
	 * Obtains the {@link Team} responsible for completing the {@link Job}.
	 * 
	 * @return {@link Team} responsible for completing the {@link Job}.
	 */
	Team getTeam();

	/**
	 * Obtains the {@link TaskMetaData} of the next {@link Task} within
	 * {@link Flow} that this {@link Task} is involved within.
	 * 
	 * @param key
	 *            Key of the {@link Flow}.
	 * @return {@link TaskMetaData} of the first {@link Task} within the
	 *         specified {@link Flow}.
	 */
	TaskMetaData<?, ?, ?> getNextTaskInFlow();

	/**
	 * Obtains the {@link EscalationProcedure} for the {@link Job} of this
	 * {@link JobMetaData}.
	 * 
	 * @return {@link EscalationProcedure}.
	 */
	EscalationProcedure getEscalationProcedure();

}