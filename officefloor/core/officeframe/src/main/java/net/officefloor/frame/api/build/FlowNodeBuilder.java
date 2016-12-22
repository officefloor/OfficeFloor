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
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.team.Team;

/**
 * Builds a node of a {@link Flow} and provides linking to other
 * {@link Flow} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface FlowNodeBuilder<F extends Enum<F>> {

	/**
	 * Specifies the {@link Team} by its {@link Office} registered name that
	 * that is responsible for this node.
	 * 
	 * @param officeTeamName
	 *            Name of the {@link Team} within the {@link Office}.
	 */
	void setTeam(String officeTeamName);

	/**
	 * Specifies the next {@link ManagedFunction} in the {@link Flow} ({@link ManagedFunction}
	 * will reside on the same {@link Work}).
	 * 
	 * @param taskName
	 *            Name of the next {@link ManagedFunction} in the {@link Flow}.
	 * @param argumentType
	 *            Type of argument passed to the next {@link ManagedFunction}. May be
	 *            <code>null</code> to indicate no argument.
	 */
	void setNextTaskInFlow(String taskName, Class<?> argumentType);

	/**
	 * Specifies the next {@link ManagedFunction} in the {@link Flow} ({@link ManagedFunction}
	 * may reside on another {@link Work}).
	 * 
	 * @param workName
	 *            Name of {@link Work} containing the {@link ManagedFunction}.
	 * @param taskName
	 *            Name of the next {@link ManagedFunction} in the {@link Flow}.
	 * @param argumentType
	 *            Type of argument passed to the next {@link ManagedFunction}. May be
	 *            <code>null</code> to indicate no argument.
	 */
	void setNextTaskInFlow(String workName, String taskName,
			Class<?> argumentType);

	/**
	 * Links in a {@link Flow} by specifying the first {@link ManagedFunction} of
	 * the {@link Flow}.
	 * 
	 * @param key
	 *            Key identifying the {@link Flow}.
	 * @param taskName
	 *            Name of {@link ManagedFunction} that resides on same {@link Work} as this
	 *            {@link ManagedFunction}.
	 * @param strategy
	 *            Strategy to instigate the {@link Flow}.
	 * @param argumentType
	 *            Type of argument passed to the instigated {@link Flow}.
	 *            May be <code>null</code> to indicate no argument.
	 */
	void linkFlow(F key, String taskName, FlowInstigationStrategyEnum strategy,
			Class<?> argumentType);

	/**
	 * Links in a {@link Flow} by specifying the first {@link ManagedFunction} of
	 * the {@link Flow}.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link Flow}.
	 * @param taskName
	 *            Name of {@link ManagedFunction} that resides on same {@link Work} as this
	 *            {@link ManagedFunction}.
	 * @param strategy
	 *            Strategy to instigate the {@link Flow}.
	 * @param argumentType
	 *            Type of argument passed to the instigated {@link Flow}.
	 *            May be <code>null</code> to indicate no argument.
	 */
	void linkFlow(int flowIndex, String taskName,
			FlowInstigationStrategyEnum strategy, Class<?> argumentType);

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
	 * <p>
	 * Adds an {@link EscalationFlow} to the {@link EscalationProcedure} for the
	 * {@link ManagedFunction}.
	 * <p>
	 * The order in which the {@link EscalationFlow} instances are added is the
	 * order in which they are checked for handling escalation. Only one
	 * {@link EscalationFlow} is used to handle escalation and the first one
	 * covering the cause will be used.
	 * 
	 * @param typeOfCause
	 *            Type of cause handled by this {@link EscalationFlow}.
	 * @param taskName
	 *            Name of the {@link ManagedFunction} that resides on the same {@link Work}
	 *            as this {@link ManagedFunction}.
	 * 
	 * @see #addEscalation(Class, String, String)
	 */
	void addEscalation(Class<? extends Throwable> typeOfCause, String taskName);

	/**
	 * Adds an {@link EscalationFlow} to the {@link EscalationProcedure} for the
	 * {@link ManagedFunction}.
	 * 
	 * @param typeOfCause
	 *            Type of cause handled by this {@link EscalationFlow}.
	 * @param workName
	 *            Name of the {@link Work} that the first {@link ManagedFunction} of the
	 *            {@link Flow} resides on.
	 * @param taskName
	 *            Name of {@link ManagedFunction} that resides on a different {@link Work}
	 *            as this {@link ManagedFunction}.
	 * 
	 * @see #addEscalation(Class, String)
	 */
	void addEscalation(Class<? extends Throwable> typeOfCause, String workName,
			String taskName);

}