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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.team.Team;

/**
 * Builds the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceBuilder<F extends Enum<F>> {

	/**
	 * Specifies the name of the {@link Team} responsible for executing the
	 * {@link Governance} {@link Task} instances.
	 * 
	 * @param teamName
	 *            {@link Team} name.
	 */
	void setTeam(String teamName);

	/**
	 * Links in a {@link JobSequence} by specifying the first {@link Task} of
	 * the {@link JobSequence}.
	 * 
	 * @param key
	 *            Key identifying the {@link JobSequence}.
	 * @param workName
	 *            Name of the {@link Work} that the first {@link Task} of the
	 *            {@link JobSequence} resides on.
	 * @param taskName
	 *            Name of {@link Task} that resides on a different {@link Work}
	 *            as this {@link Task}.
	 * @param strategy
	 *            Strategy to instigate the {@link JobSequence}.
	 * @param argumentType
	 *            Type of argument passed to the instigated {@link JobSequence}.
	 *            May be <code>null</code> to indicate no argument.
	 */
	void linkFlow(F key, String workName, String taskName,
			FlowInstigationStrategyEnum strategy, Class<?> argumentType);

	/**
	 * Links in a {@link JobSequence} by specifying the first {@link Task} of
	 * the {@link JobSequence}.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link JobSequence}.
	 * @param workName
	 *            Name of the {@link Work} that the first {@link Task} of the
	 *            {@link JobSequence} resides on.
	 * @param taskName
	 *            Name of {@link Task} that resides on a different {@link Work}
	 *            as this {@link Task}.
	 * @param strategy
	 *            Strategy to instigate the {@link JobSequence}.
	 * @param argumentType
	 *            Type of argument passed to the instigated {@link JobSequence}.
	 *            May be <code>null</code> to indicate no argument.
	 */
	void linkFlow(int flowIndex, String workName, String taskName,
			FlowInstigationStrategyEnum strategy, Class<?> argumentType);

	/**
	 * Adds an {@link EscalationFlow} to the {@link EscalationProcedure} for the
	 * {@link Governance}.
	 * 
	 * @param typeOfCause
	 *            Type of cause handled by this {@link EscalationFlow}.
	 * @param workName
	 *            Name of the {@link Work} that the first {@link Task} of the
	 *            {@link JobSequence} resides on.
	 * @param taskName
	 *            Name of {@link Task} on the {@link Work}.
	 */
	void addEscalation(Class<? extends Throwable> typeOfCause, String workName,
			String taskName);

}