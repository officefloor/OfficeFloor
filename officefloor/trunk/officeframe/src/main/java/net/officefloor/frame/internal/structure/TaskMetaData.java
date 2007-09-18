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
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.team.Team;

/**
 * Meta-data for the {@link net.officefloor.frame.api.execute.Task}.
 * 
 * @author Daniel
 */
public interface TaskMetaData<P extends Object, W extends Work, M extends Enum<M>, F extends Enum<F>> {

	/**
	 * Obtains the {@link TaskFactory} to create the
	 * {@link net.officefloor.frame.api.execute.Task} for this
	 * {@link TaskMetaData}.
	 * 
	 * @return {@link TaskFactory}
	 */
	TaskFactory<P, W, M, F> getTaskFactory();

	/**
	 * Obtains the {@link Team} responsible for executing this
	 * {@link net.officefloor.frame.api.execute.Task}.
	 * 
	 * @return {@link Team} responsible for executing this
	 *         {@link net.officefloor.frame.api.execute.Task}.
	 */
	Team getTeam();

	/**
	 * Obtains the indexes to the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} instances
	 * that must be loaded before the
	 * {@link net.officefloor.frame.api.execute.Task} may be executed.
	 * 
	 * @return Listing of indexes of
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *         instances.
	 */
	int[] getRequiredManagedObjects();

	/**
	 * Obtains the indexes to the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} instances
	 * that are
	 * {@link net.officefloor.frame.spi.managedobject.AsynchronousManagedObject}
	 * and require checking to be ready before the
	 * {@link net.officefloor.frame.api.execute.Task} may be executed.
	 * 
	 * @return Listing of indexes of
	 *         {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *         instances.
	 */
	int[] getCheckManagedObjects();

	/**
	 * Translates the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} index of
	 * the {@link net.officefloor.frame.api.execute.Task} to that of the
	 * {@link Work}.
	 * 
	 * @param taskMoIndex
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            index of the {@link net.officefloor.frame.api.execute.Task}.
	 * @return {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *         index of the {@link Work}.
	 */
	int translateManagedObjectIndexForWork(int taskMoIndex);

	/**
	 * Obtains the {@link FlowMetaData} of the specified {@link Flow}.
	 * 
	 * @param flowIndex
	 *            Index of the {@link Flow}.
	 * @return {@link FlowMetaData} of the specified {@link Flow}.
	 */
	FlowMetaData<?> getFlow(int flowIndex);

	/**
	 * Obtains the {@link WorkMetaData} for this
	 * {@link net.officefloor.frame.api.execute.Task}.
	 * 
	 * @return {@link WorkMetaData} for this
	 *         {@link net.officefloor.frame.api.execute.Task}.
	 */
	WorkMetaData<W> getWorkMetaData();

	/**
	 * Obtains the {@link EscalationProcedure} for the
	 * {@link net.officefloor.frame.api.execute.Task} of this
	 * {@link TaskMetaData}.
	 * 
	 * @return {@link EscalationProcedure} for the
	 *         {@link net.officefloor.frame.api.execute.Task} of this
	 *         {@link TaskMetaData}.
	 */
	EscalationProcedure getEscalationProcedure();

	/**
	 * Obtains the {@link TaskMetaData} of the next
	 * {@link net.officefloor.frame.api.execute.Task} within {@link Flow} that
	 * this {@link net.officefloor.frame.api.execute.Task} is involved within.
	 * 
	 * @param key
	 *            Key of the {@link Flow}.
	 * @return {@link TaskMetaData} of the first
	 *         {@link net.officefloor.frame.api.execute.Task} within the
	 *         specified {@link Flow}.
	 */
	TaskMetaData<?, ?, ?, ?> getNextTaskInFlow();

	/**
	 * Meta-data of the {@link net.officefloor.frame.spi.administration.Duty} to
	 * undertake before executing the
	 * {@link net.officefloor.frame.api.execute.Task}.
	 * 
	 * @return Listing of the
	 *         {@link net.officefloor.frame.spi.administration.Duty} instances
	 *         to undertake before executing the
	 *         {@link net.officefloor.frame.api.execute.Task}.
	 */
	TaskDutyAssociation<?>[] getPreAdministrationMetaData();

	/**
	 * Meta-data of the
	 * {@link net.officefloor.frame.spi.administration.Administrator} to
	 * undertake after executing the
	 * {@link net.officefloor.frame.api.execute.Task}.
	 * 
	 * @return Listing the {@link net.officefloor.frame.spi.administration.Duty}
	 *         instances to undertake after executing the
	 *         {@link net.officefloor.frame.api.execute.Task}.
	 */
	TaskDutyAssociation<?>[] getPostAdministrationMetaData();

}
