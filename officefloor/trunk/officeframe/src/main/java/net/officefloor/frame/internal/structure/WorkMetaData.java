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

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.spi.administration.Administrator;

/**
 * Meta-data for a {@link Work} instance.
 * 
 * @author Daniel
 */
public interface WorkMetaData<W extends Work> {

	/**
	 * Obtains the ID distinguishing this {@link Work} from the other
	 * {@link Work}.
	 * 
	 * @return ID distinguishing this {@link Work} from the other {@link Work}.
	 */
	// TODO remove work ID in favour of bounding to a scope
	@Deprecated
	int getWorkId();

	/**
	 * Obtain the {@link WorkFactory}.
	 * 
	 * @return {@link WorkFactory} of the {@link Work}.
	 */
	WorkFactory<W> getWorkFactory();

	/**
	 * Obtain the {@link FlowMetaData} for the initial {@link Flow} of the
	 * {@link Work}.
	 * 
	 * @return {@link FlowMetaData} for the initial {@link Flow} of the
	 *         {@link Work}.
	 */
	FlowMetaData<W> getInitialFlowMetaData();

	/**
	 * Obtains the {@link ManagedObjectIndex} instances to look up the
	 * {@link ManagedObjectContainer} instances for the {@link Work}.
	 * 
	 * @return {@link ManagedObjectIndex} instances to look up the
	 *         {@link ManagedObjectContainer} instances for the {@link Work}.
	 */
	ManagedObjectIndex[] getManagedObjectIndexes();

	/**
	 * Obtains the meta-data of the {@link ManagedObject} instances bound to the
	 * {@link Work}.
	 * 
	 * @return Meta-data of the {@link ManagedObject} instances bound to the
	 *         {@link Work}.
	 */
	ManagedObjectMetaData<?>[] getManagedObjectMetaData();

	/**
	 * Obtains the {@link AdministratorIndex} instances to look up the
	 * {@link AdministratorContainer} instances for the {@link Work}.
	 * 
	 * @return {@link AdministratorIndex} instances to look up the
	 *         {@link AdministratorContainer} instances for the {@link Work}.
	 */
	AdministratorIndex[] getAdministratorIndexes();

	/**
	 * Obtains the meta-data of the {@link Administrator} instances for the
	 * {@link Work}.
	 * 
	 * @return Meta-data of the {@link Administrator} instances for the
	 *         {@link Work}.
	 */
	AdministratorMetaData<?, ?>[] getAdministratorMetaData();

}
