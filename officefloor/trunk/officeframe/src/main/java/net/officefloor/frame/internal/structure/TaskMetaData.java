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

import net.officefloor.frame.api.build.TaskFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Meta-data for the {@link Task}.
 * 
 * @author Daniel
 */
public interface TaskMetaData<P, W extends Work, M extends Enum<M>, F extends Enum<F>>
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
	TaskFactory<P, W, M, F> getTaskFactory();

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
	ManagedObjectIndex[] getRequiredManagedObjects();

	/**
	 * Translates the {@link ManagedObject} index of the {@link Task} to that of
	 * the {@link Work} ({@link ManagedObjectIndex}).
	 * 
	 * @param taskMoIndex
	 *            {@link ManagedObject} index of the {@link Task}.
	 * @return {@link ManagedObjectIndex} identifying the {@link ManagedObject}
	 *         for the {@link Task} index.
	 */
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

}