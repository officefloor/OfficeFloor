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
package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.team.Team;

/**
 * Configuration of a {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskConfiguration<W extends Work, D extends Enum<D>, F extends Enum<F>> {

	/**
	 * Obtains the name of this {@link Task} local to its {@link Work}.
	 * 
	 * @return Name of this {@link Task} local to its {@link Work}.
	 */
	String getTaskName();

	/**
	 * Obtains the {@link TaskFactory} for the {@link Task}.
	 * 
	 * @return {@link TaskFactory}.
	 */
	TaskFactory<W, D, F> getTaskFactory();

	/**
	 * Obtains the differentiator for the {@link Task}.
	 * 
	 * @return Differentiator or <code>null</code> if no differentiator for the
	 *         {@link Task}.
	 */
	Object getDifferentiator();

	/**
	 * Obtains the name of {@link Office} registered {@link Team} responsible
	 * for completing this {@link Task}.
	 * 
	 * @return Name of the {@link Office} registered {@link Team}.
	 */
	String getOfficeTeamName();

	/**
	 * Obtains the reference to the next {@link Task} within the
	 * {@link JobSequence}.
	 * 
	 * @return Reference to the next {@link Task} within the {@link JobSequence}
	 *         .
	 */
	TaskNodeReference getNextTaskInFlow();

	/**
	 * Obtains the configuration of the {@link JobSequence} instances for this
	 * {@link Task}.
	 * 
	 * @return Configuration of {@link JobSequence} instances for this
	 *         {@link Task}.
	 */
	TaskFlowConfiguration<F>[] getFlowConfiguration();

	/**
	 * Obtains the configuration of the dependent {@link Object} instances for
	 * this {@link Task}.
	 * 
	 * @return Configuration of the dependent {@link Object} instances for this
	 *         {@link Task}.
	 */
	TaskObjectConfiguration<D>[] getObjectConfiguration();

	/**
	 * Obtains the configuration of the {@link Governance} instances for this
	 * {@link Task}.
	 * 
	 * @return Configuration of the {@link Governance} for this {@link Task}.
	 */
	TaskGovernanceConfiguration[] getGovernanceConfiguration();

	/**
	 * Obtains the listing of the {@link TaskDutyConfiguration} for the
	 * administration to be done before the {@link Task} is executed.
	 * 
	 * @return Listing of the {@link TaskDutyConfiguration} for the
	 *         administration to be done before the {@link Task} is executed.
	 */
	TaskDutyConfiguration<?>[] getPreTaskAdministratorDutyConfiguration();

	/**
	 * Obtains the listing of the {@link TaskDutyConfiguration} for the
	 * administration to be done after the {@link Task} is executed.
	 * 
	 * @return Listing of the {@link TaskDutyConfiguration} for the
	 *         administration to be done after the {@link Task} is executed.
	 */
	TaskDutyConfiguration<?>[] getPostTaskAdministratorDutyConfiguration();

	/**
	 * Obtains the {@link TaskEscalationConfiguration} instances in escalation
	 * order. Index 0 being first, index 1 second and so forth.
	 * 
	 * @return {@link TaskEscalationConfiguration} instances.
	 */
	TaskEscalationConfiguration[] getEscalations();

}