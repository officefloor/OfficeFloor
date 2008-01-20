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
import net.officefloor.frame.internal.structure.Escalation;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Meta-data of the {@link net.officefloor.frame.api.execute.Task}.
 * 
 * @author Daniel
 */
public interface TaskBuilder<P extends Object, W extends Work, M extends Enum<M>, F extends Enum<F>> {

	/**
	 * Specifies the {@link TaskFactory}.
	 * 
	 * @param factory
	 *            {@link TaskFactory}.
	 */
	void setTaskFactory(TaskFactory<P, W, M, F> factory);

	/**
	 * Specifies the {@link net.officefloor.frame.spi.team.Team} that will carry
	 * out this {@link net.officefloor.frame.api.execute.Task}.
	 * 
	 * @param teamName
	 *            Name of the {@link net.officefloor.frame.spi.team.Team} local
	 *            to the {@link net.officefloor.frame.api.manage.Office} that
	 *            will execute this
	 *            {@link net.officefloor.frame.api.execute.Task}.
	 */
	void setTeam(String teamName);

	/**
	 * Specifies the next {@link net.officefloor.frame.api.execute.Task} in the
	 * {@link net.officefloor.frame.internal.structure.Flow} ({@link net.officefloor.frame.api.execute.Task}
	 * will reside on the same {@link Work}).
	 * 
	 * @param taskName
	 *            Name of the next
	 *            {@link net.officefloor.frame.api.execute.Task} in the
	 *            {@link net.officefloor.frame.internal.structure.Flow}.
	 */
	void setNextTaskInFlow(String taskName);

	/**
	 * Specifies the next {@link net.officefloor.frame.api.execute.Task} in the
	 * {@link net.officefloor.frame.internal.structure.Flow} ({@link net.officefloor.frame.api.execute.Task}
	 * may reside on another {@link Work}).
	 * 
	 * @param workName
	 *            Name of {@link Work} containing the
	 *            {@link net.officefloor.frame.api.execute.Task}.
	 * @param taskName
	 *            Name of the next
	 *            {@link net.officefloor.frame.api.execute.Task} in the
	 *            {@link net.officefloor.frame.internal.structure.Flow}.
	 */
	void setNextTaskInFlow(String workName, String taskName);

	/**
	 * Links in a {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 * to this {@link net.officefloor.frame.api.execute.Task}.
	 * 
	 * @param M
	 *            {@link Enum} type for the listing of
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            instances of {@link net.officefloor.frame.api.execute.Task}.
	 * @param key
	 *            Key identifying the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * @param workManagedObjectName
	 *            Name of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            local to this {@link Work}.
	 */
	void linkManagedObject(M key, String workManagedObjectName);

	/**
	 * Links in a {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 * to this {@link net.officefloor.frame.api.execute.Task}.
	 * 
	 * @param managedObjectIndex
	 *            Index of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}.
	 * @param workManagedObjectName
	 *            Name of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            local to this {@link Work}.
	 */
	void linkManagedObject(int managedObjectIndex, String workManagedObjectName);

	/**
	 * Links in a {@link net.officefloor.frame.spi.administration.Duty} to be
	 * executed before the {@link net.officefloor.frame.api.execute.Task}.
	 * 
	 * @param <A>
	 *            {@link Enum} of the duties.
	 * @param workAdministratorName
	 *            Name of the
	 *            {@link net.officefloor.frame.spi.administration.Administrator}
	 *            local to this {@link Work}.
	 * @param dutyKey
	 *            Key identifying the
	 *            {@link net.officefloor.frame.spi.administration.Duty}.
	 */
	<A extends Enum<A>> void linkPreTaskAdministration(
			String workAdministratorName, A dutyKey);

	/**
	 * Links in a {@link net.officefloor.frame.spi.administration.Duty} to be
	 * executed after the {@link net.officefloor.frame.api.execute.Task}.
	 * 
	 * @param <A>
	 *            {@link Enum} of the duties.
	 * @param workAdministratorName
	 *            Name of the
	 *            {@link net.officefloor.frame.spi.administration.Administrator}
	 *            local to this {@link Work}.
	 * @param dutyKey
	 *            Key identifying the
	 *            {@link net.officefloor.frame.spi.administration.Duty}.
	 */
	<A extends Enum<A>> void linkPostTaskAdministration(
			String workAdministratorName, A dutyKey);

	/**
	 * Links in a {@link net.officefloor.frame.internal.structure.Flow} by
	 * specifying the first {@link net.officefloor.frame.api.execute.Task} of
	 * the {@link net.officefloor.frame.internal.structure.Flow}.
	 * 
	 * @param F
	 *            {@link Enum} type for the listing of
	 *            {@link net.officefloor.frame.internal.structure.Flow}
	 *            instances to link to this
	 *            {@link net.officefloor.frame.api.execute.Task}.
	 * @param key
	 *            Key identifying the
	 *            {@link net.officefloor.frame.internal.structure.Flow}.
	 * @param taskName
	 *            Name of {@link net.officefloor.frame.api.execute.Task} that
	 *            resides on same {@link Work} as this
	 *            {@link net.officefloor.frame.api.execute.Task}.
	 * @param strategy
	 *            Strategy to instigate the
	 *            {@link net.officefloor.frame.internal.structure.Flow}.
	 */
	void linkFlow(F key, String taskName, FlowInstigationStrategyEnum strategy);

	/**
	 * Links in a {@link net.officefloor.frame.internal.structure.Flow} by
	 * specifying the first {@link net.officefloor.frame.api.execute.Task} of
	 * the {@link net.officefloor.frame.internal.structure.Flow}.
	 * 
	 * @param flowIndex
	 *            Index identifying the
	 *            {@link net.officefloor.frame.internal.structure.Flow}.
	 * @param taskName
	 *            Name of {@link net.officefloor.frame.api.execute.Task} that
	 *            resides on same {@link Work} as this
	 *            {@link net.officefloor.frame.api.execute.Task}.
	 * @param strategy
	 *            Strategy to instigate the
	 *            {@link net.officefloor.frame.internal.structure.Flow}.
	 */
	void linkFlow(int flowIndex, String taskName,
			FlowInstigationStrategyEnum strategy);

	/**
	 * Links in a {@link net.officefloor.frame.internal.structure.Flow} by
	 * specifying the first {@link net.officefloor.frame.api.execute.Task} of
	 * the {@link net.officefloor.frame.internal.structure.Flow}.
	 * 
	 * @param F
	 *            {@link Enum} type for the listing of
	 *            {@link net.officefloor.frame.internal.structure.Flow}
	 *            instances to link to this
	 *            {@link net.officefloor.frame.api.execute.Task}.
	 * @param key
	 *            Key identifying the
	 *            {@link net.officefloor.frame.internal.structure.Flow}.
	 * @param workName
	 *            Name of the {@link Work} that the first
	 *            {@link net.officefloor.frame.api.execute.Task} of the
	 *            {@link net.officefloor.frame.internal.structure.Flow} resides
	 *            on.
	 * @param taskName
	 *            Name of {@link net.officefloor.frame.api.execute.Task} that
	 *            resides on a different {@link Work} as this
	 *            {@link net.officefloor.frame.api.execute.Task}.
	 * @param strategy
	 *            Strategy to instigate the
	 *            {@link net.officefloor.frame.internal.structure.Flow}.
	 */
	void linkFlow(F key, String workName, String taskName,
			FlowInstigationStrategyEnum strategy);

	/**
	 * Links in a {@link net.officefloor.frame.internal.structure.Flow} by
	 * specifying the first {@link net.officefloor.frame.api.execute.Task} of
	 * the {@link net.officefloor.frame.internal.structure.Flow}.
	 * 
	 * @param flowIndex
	 *            Index identifying the
	 *            {@link net.officefloor.frame.internal.structure.Flow}.
	 * @param workName
	 *            Name of the {@link Work} that the first
	 *            {@link net.officefloor.frame.api.execute.Task} of the
	 *            {@link net.officefloor.frame.internal.structure.Flow} resides
	 *            on.
	 * @param taskName
	 *            Name of {@link net.officefloor.frame.api.execute.Task} that
	 *            resides on a different {@link Work} as this
	 *            {@link net.officefloor.frame.api.execute.Task}.
	 * @param strategy
	 *            Strategy to instigate the
	 *            {@link net.officefloor.frame.internal.structure.Flow}.
	 */
	void linkFlow(int flowIndex, String workName, String taskName,
			FlowInstigationStrategyEnum strategy);

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
