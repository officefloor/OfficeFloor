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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.Team;

/**
 * Builds a node of a {@link Flow} and provides linking to other {@link Flow}
 * instances.
 * 
 * @author Daniel
 */
public interface FlowNodeBuilder<F extends Enum<F>> {

	/**
	 * Specifies the {@link Team} that will carry out this node.
	 * 
	 * @param teamName
	 *            Name of the {@link Team} local to the {@link Office} that will
	 *            execute this node.
	 */
	void setTeam(String teamName);

	/**
	 * Specifies the next {@link Task} in the {@link Flow} ({@link Task} will
	 * reside on the same {@link Work}).
	 * 
	 * @param taskName
	 *            Name of the next {@link Task} in the {@link Flow}.
	 */
	void setNextTaskInFlow(String taskName);

	/**
	 * Specifies the next {@link Task} in the {@link Flow} ({@link Task} may
	 * reside on another {@link Work}).
	 * 
	 * @param workName
	 *            Name of {@link Work} containing the {@link Task}.
	 * @param taskName
	 *            Name of the next {@link Task} in the {@link Flow}.
	 */
	void setNextTaskInFlow(String workName, String taskName);

	/**
	 * Links in a {@link Flow} by specifying the first {@link Task} of the
	 * {@link Flow}.
	 * 
	 * @param key
	 *            Key identifying the {@link Flow}.
	 * @param taskName
	 *            Name of {@link Task} that resides on same {@link Work} as this
	 *            {@link Task}.
	 * @param strategy
	 *            Strategy to instigate the {@link Flow}.
	 * @throws BuildException
	 *             If fails to link in {@link Flow}.
	 */
	void linkFlow(F key, String taskName, FlowInstigationStrategyEnum strategy)
			throws BuildException;

	/**
	 * Links in a {@link Flow} by specifying the first {@link Task} of the
	 * {@link Flow}.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link Flow}.
	 * @param taskName
	 *            Name of {@link Task} that resides on same {@link Work} as this
	 *            {@link Task}.
	 * @param strategy
	 *            Strategy to instigate the {@link Flow}.
	 * @throws BuildException
	 *             If fails to link in {@link Flow}.
	 */
	void linkFlow(int flowIndex, String taskName,
			FlowInstigationStrategyEnum strategy) throws BuildException;

	/**
	 * Links in a {@link Flow} by specifying the first {@link Task} of the
	 * {@link Flow}.
	 * 
	 * @param key
	 *            Key identifying the {@link Flow}.
	 * @param workName
	 *            Name of the {@link Work} that the first {@link Task} of the
	 *            {@link Flow} resides on.
	 * @param taskName
	 *            Name of {@link Task} that resides on a different {@link Work}
	 *            as this {@link Task}.
	 * @param strategy
	 *            Strategy to instigate the {@link Flow}.
	 * @throws BuildException
	 *             If fails to link in {@link Flow}.
	 */
	void linkFlow(F key, String workName, String taskName,
			FlowInstigationStrategyEnum strategy) throws BuildException;

	/**
	 * Links in a {@link Flow} by specifying the first {@link Task} of the
	 * {@link Flow}.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link Flow}.
	 * @param workName
	 *            Name of the {@link Work} that the first {@link Task} of the
	 *            {@link Flow} resides on.
	 * @param taskName
	 *            Name of {@link Task} that resides on a different {@link Work}
	 *            as this {@link Task}.
	 * @param strategy
	 *            Strategy to instigate the {@link Flow}.
	 * @throws BuildException
	 *             If fails to link in {@link Flow}.
	 */
	void linkFlow(int flowIndex, String workName, String taskName,
			FlowInstigationStrategyEnum strategy) throws BuildException;

	/**
	 * <p>
	 * Adds an {@link Escalation} to the {@link EscalationProcedure} for the
	 * {@link Task}.
	 * <p>
	 * The order in which the {@link Escalation} instances are added is the
	 * order in which they are checked for handling escalation. Only one
	 * {@link Escalation} is used to handle escalation and the first one
	 * covering the cause will be used.
	 * 
	 * @param typeOfCause
	 *            Type of cause handled by this {@link Escalation}.
	 * @param isResetThreadState
	 *            Indicates the {@link ThreadState} is to be reset on following
	 *            the {@link Escalation}.
	 * @param taskName
	 *            Name of the {@link Task} that resides on the same {@link Work}
	 *            as this {@link Task}.
	 * @see #addEscalation(Class, boolean, String, String)
	 */
	void addEscalation(Class<? extends Throwable> typeOfCause,
			boolean isResetThreadState, String taskName);

	/**
	 * Adds an {@link Escalation} to the {@link EscalationProcedure} for the
	 * {@link Task}.
	 * 
	 * @param typeOfCause
	 *            Type of cause handled by this {@link Escalation}.
	 * @param isResetThreadState
	 *            Indicates the {@link ThreadState} is to be reset on following
	 *            the {@link Escalation}.
	 * @param workName
	 *            Name of the {@link Work} that the first {@link Task} of the
	 *            {@link Flow} resides on.
	 * @param taskName
	 *            Name of {@link Task} that resides on a different {@link Work}
	 *            as this {@link Task}.
	 * @see #addEscalation(Class, boolean, String)
	 */
	void addEscalation(Class<? extends Throwable> typeOfCause,
			boolean isResetThreadState, String workName, String taskName);

}
