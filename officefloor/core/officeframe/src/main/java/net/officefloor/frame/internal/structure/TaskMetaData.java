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

import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Meta-data for the {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskMetaData<W extends Work, D extends Enum<D>, F extends Enum<F>>
		extends JobMetaData {

	/**
	 * Obtains the name of this {@link Task}.
	 * 
	 * @return Name of this {@link Task}.
	 */
	String getTaskName();

	/**
	 * Obtains the {@link TaskFactory} to create the {@link Task} for this
	 * {@link TaskMetaData}.
	 * 
	 * @return {@link TaskFactory}
	 */
	TaskFactory<W, D, F> getTaskFactory();

	/**
	 * Obtains the differentiator for the {@link Task}.
	 * 
	 * @return Differentiator or <code>null</code> if no differentiator.
	 */
	Object getDifferentiator();

	/**
	 * Obtains the parameter type for the {@link Task}.
	 * 
	 * @return Parameter type for the {@link Task}. May be <code>null</code> to
	 *         indicate no parameter.
	 */
	Class<?> getParameterType();

	/**
	 * Obtains the {@link ManagedObjectIndex} instances identifying the
	 * {@link ManagedObject} instances that must be loaded before the
	 * {@link Task} may be executed.
	 * 
	 * @return Listing of {@link ManagedObjectIndex} instances.
	 */
	@Deprecated // move to JobMetaData
	ManagedObjectIndex[] getRequiredManagedObjects();

	/**
	 * Obtains the activation flags for the {@link Governance}. The index into
	 * the array identifies the {@link Governance} for the respective activation
	 * flag.
	 * 
	 * @return Activation flags for the {@link Governance}.
	 */
	@Deprecated // move to JobMetaData
	boolean[] getRequiredGovernance();

	/**
	 * Translates the {@link ManagedObject} index of the {@link Task} to that of
	 * the {@link Work} ({@link ManagedObjectIndex}).
	 * 
	 * @param taskMoIndex
	 *            {@link ManagedObject} index of the {@link Task}.
	 * @return {@link ManagedObjectIndex} identifying the {@link ManagedObject}
	 *         for the {@link Task} index.
	 */
	@Deprecated // work scope to become task scope (state managed within managed objects)
	ManagedObjectIndex translateManagedObjectIndexForWork(int taskMoIndex);

	/**
	 * Obtains the {@link FlowMetaData} of the specified {@link Flow}.
	 * 
	 * @param flowIndex
	 *            Index of the {@link Flow}.
	 * @return {@link FlowMetaData} of the specified {@link Flow}.
	 */
	FlowMetaData<?> getFlow(int flowIndex);

	/**
	 * Obtains the {@link WorkMetaData} for this {@link Task}.
	 * 
	 * @return {@link WorkMetaData} for this {@link Task}.
	 */
	WorkMetaData<W> getWorkMetaData();

	/**
	 * Meta-data of the {@link Duty} to undertake before executing the
	 * {@link Task}.
	 * 
	 * @return Listing of the {@link Duty} instances to undertake before
	 *         executing the {@link Task}.
	 */
	TaskDutyAssociation<?>[] getPreAdministrationMetaData();

	/**
	 * Meta-data of the {@link Administrator} to undertake after executing the
	 * {@link Task}.
	 * 
	 * @return Listing the {@link Duty} instances to undertake after executing
	 *         the {@link Task}.
	 */
	TaskDutyAssociation<?>[] getPostAdministrationMetaData();

	/**
	 * Creates the {@link JobNode} for the {@link Task}.
	 * 
	 * @param flow
	 *            {@link Flow} containing the {@link Task}.
	 * @param workContainer
	 *            {@link WorkContainer} for the {@link Work} for the
	 *            {@link Task}.
	 * @param parallelJobNodeOwner
	 *            Parallel {@link JobNode} owner.
	 * @param parameter
	 *            Parameter.
	 * @param governanceDeactivationStrategy
	 *            {@link GovernanceDeactivationStrategy}.
	 * @return {@link JobNode}.
	 */
	JobNode createTaskNode(Flow flow, WorkContainer<W> workContainer,
			JobNode parallelJobNodeOwner, Object parameter,
			GovernanceDeactivationStrategy governanceDeactivationStrategy);

}