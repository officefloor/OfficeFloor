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

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;

/**
 * Meta-data for a {@link Job}.
 * 
 * @author Daniel Sagenschneider
 */
public interface JobMetaData {

	/**
	 * Obtains the name of the {@link Job}.
	 * 
	 * @return Name of the {@link Job}.
	 */
	String getJobName();

	/**
	 * Creates the {@link JobNodeActivatableSet} for executing the {@link Job}.
	 * 
	 * @return {@link JobNodeActivatableSet}.
	 */
	@Deprecated // part of job loop
	JobNodeActivatableSet createJobActivableSet();

	/**
	 * Obtains the {@link TeamManagement} responsible for completion of the
	 * {@link Job}.
	 * 
	 * @return {@link TeamManagement} responsible for completion of the
	 *         {@link Job}. May be <code>null</code> to enable any {@link Team}
	 *         to execute the {@link JobNode}.
	 */
	TeamManagement getResponsibleTeam();

	/**
	 * Obtains the {@link JobNodeLoop}.
	 * 
	 * @return {@link JobNodeLoop}.
	 */
	JobNodeLoop getJobNodeDelegator();

	/**
	 * Obtains the {@link TaskMetaData} of the next {@link Task} within
	 * {@link Flow} that this {@link Task} is involved within.
	 * 
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