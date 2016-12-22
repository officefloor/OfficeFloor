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

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.Flow;
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
	 * {@link Governance} {@link ManagedFunction} instances.
	 * 
	 * @param teamName
	 *            {@link Team} name.
	 */
	void setTeam(String teamName);

	/**
	 * Links in a {@link Flow} by specifying the first {@link ManagedFunction} of
	 * the {@link Flow}.
	 * 
	 * @param key
	 *            Key identifying the {@link Flow}.
	 * @param workName
	 *            Name of the {@link Work} that the first {@link ManagedFunction} of the
	 *            {@link Flow} resides on.
	 * @param taskName
	 *            Name of {@link ManagedFunction} that resides on a different {@link Work}
	 *            as this {@link ManagedFunction}.
	 * @param strategy
	 *            Strategy to instigate the {@link Flow}.
	 * @param argumentType
	 *            Type of argument passed to the instigated {@link Flow}.
	 *            May be <code>null</code> to indicate no argument.
	 */
	void linkFlow(F key, String workName, String taskName,
			FlowInstigationStrategyEnum strategy, Class<?> argumentType);

	/**
	 * Links in a {@link Flow} by specifying the first {@link ManagedFunction} of
	 * the {@link Flow}.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link Flow}.
	 * @param workName
	 *            Name of the {@link Work} that the first {@link ManagedFunction} of the
	 *            {@link Flow} resides on.
	 * @param taskName
	 *            Name of {@link ManagedFunction} that resides on a different {@link Work}
	 *            as this {@link ManagedFunction}.
	 * @param strategy
	 *            Strategy to instigate the {@link Flow}.
	 * @param argumentType
	 *            Type of argument passed to the instigated {@link Flow}.
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
	 *            Name of the {@link Work} that the first {@link ManagedFunction} of the
	 *            {@link Flow} resides on.
	 * @param taskName
	 *            Name of {@link ManagedFunction} on the {@link Work}.
	 */
	void addEscalation(Class<? extends Throwable> typeOfCause, String workName,
			String taskName);

}