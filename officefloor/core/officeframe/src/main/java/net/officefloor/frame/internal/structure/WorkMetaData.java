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

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Meta-data for a {@link Work} instance.
 * 
 * @author Daniel Sagenschneider
 */
public interface WorkMetaData<W extends Work> {

	/**
	 * Obtains the name of this {@link Work}.
	 * 
	 * @return Name of this {@link Work}.
	 */
	String getWorkName();

	/**
	 * Creates a {@link WorkContainer} from this {@link WorkMetaData}.
	 * 
	 * @param processState
	 *            {@link ProcessState} that the {@link WorkContainer} is being
	 *            created within.
	 * @return {@link WorkContainer}.
	 */
	WorkContainer<W> createWorkContainer(ProcessState processState);

	/**
	 * Obtain the {@link WorkFactory}.
	 * 
	 * @return {@link WorkFactory} of the {@link Work}.
	 */
	WorkFactory<W> getWorkFactory();

	/**
	 * Obtain the {@link FlowMetaData} for the initial {@link Flow} of
	 * the {@link Work}.
	 * 
	 * @return {@link FlowMetaData} for the initial {@link Flow} of the
	 *         {@link Work} or <code>null</code> if no initial
	 *         {@link Flow} for the {@link Work}.
	 */
	FlowMetaData<W> getInitialFlowMetaData();

	/**
	 * Obtains the meta-data of the {@link ManagedObject} instances bound to the
	 * {@link Work}.
	 * 
	 * @return Meta-data of the {@link ManagedObject} instances bound to the
	 *         {@link Work}.
	 */
	ManagedObjectMetaData<?>[] getManagedObjectMetaData();

	/**
	 * Obtains the meta-data of the {@link Administrator} instances for the
	 * {@link Work}.
	 * 
	 * @return Meta-data of the {@link Administrator} instances for the
	 *         {@link Work}.
	 */
	AdministratorMetaData<?, ?>[] getAdministratorMetaData();

	/**
	 * Obtains the {@link TaskMetaData} for the {@link Task} instances of this
	 * {@link Work}.
	 * 
	 * @return {@link TaskMetaData} for the {@link Task} instances of this
	 *         {@link Work}.
	 */
	TaskMetaData<W, ?, ?>[] getTaskMetaData();

}