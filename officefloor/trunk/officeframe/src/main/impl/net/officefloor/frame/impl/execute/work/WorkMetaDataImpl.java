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
package net.officefloor.frame.impl.execute.work;

import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.WorkMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Implementation of the {@link WorkMetaData}.
 * 
 * @author Daniel
 */
public class WorkMetaDataImpl<W extends Work> implements WorkMetaData<W> {

	/**
	 * Name of this {@link Work}.
	 */
	private final String workName;

	/**
	 * {@link WorkFactory}.
	 */
	private final WorkFactory<W> workFactory;

	/**
	 * {@link ManagedObjectIndex} instances in the order the {@link Task}
	 * instances of this {@link Work} expect.
	 */
	private final ManagedObjectIndex[] managedObjectIndexes;

	/**
	 * {@link ManagedObjectMetaData} of the {@link ManagedObject} instances
	 * bound to this {@link Work}.
	 */
	private final ManagedObjectMetaData<?>[] managedObjectMetaData;

	/**
	 * {@link AdministratorIndex} instances in the order the {@link Task}
	 * instances of this {@link Work} expect.
	 */
	private final AdministratorIndex[] administratorIndexes;

	/**
	 * {@link AdministratorMetaData} of the {@link Administrator} instances
	 * bound to this {@link Work}.
	 */
	private final AdministratorMetaData<?, ?>[] administratorMetaData;

	/**
	 * {@link FlowMetaData} for the initial {@link Flow} of the {@link Work} .
	 */
	private final FlowMetaData<W> initialFlowMetaData;

	/**
	 * Initiate.
	 * 
	 * @param workFactory
	 *            {@link WorkFactory}.
	 * @param moIndexes
	 *            {@link ManagedObjectIndex} instances in the order the
	 *            {@link Task} instances of this {@link Work} expect.
	 * @param moMetaData
	 *            {@link ManagedObjectMetaData} of the {@link ManagedObject}
	 *            instances bound to this {@link Work}.
	 * @param adminIndexes
	 *            {@link AdministratorIndex} instances in the order the
	 *            {@link Task} instances of this {@link Work} expect.
	 * @param adminMetaData
	 *            {@link AdministratorMetaData} of the {@link Administrator}
	 *            instances bound to this {@link Work}.
	 * @param initialFlowMetaData
	 *            {@link FlowMetaData} for the initial {@link Flow} of the
	 *            {@link Work}.
	 */
	public WorkMetaDataImpl(String workName, WorkFactory<W> workFactory,
			ManagedObjectIndex[] moIndexes,
			ManagedObjectMetaData<?>[] moMetaData,
			AdministratorIndex[] adminIndexes,
			AdministratorMetaData<?, ?>[] adminMetaData,
			FlowMetaData<W> initialFlowMetaData) {
		this.workName = workName;
		this.workFactory = workFactory;
		this.managedObjectIndexes = moIndexes;
		this.managedObjectMetaData = moMetaData;
		this.administratorIndexes = adminIndexes;
		this.administratorMetaData = adminMetaData;
		this.initialFlowMetaData = initialFlowMetaData;
	}

	/*
	 * ===================== WorkMetaData =================================
	 */

	@Override
	public int getWorkId() {
		// TODO remove
		throw new UnsupportedOperationException("getWorkId deprecated");
	}

	@Override
	public String getWorkName() {
		return this.workName;
	}

	@Override
	public WorkFactory<W> getWorkFactory() {
		return this.workFactory;
	}

	@Override
	public FlowMetaData<W> getInitialFlowMetaData() {
		return this.initialFlowMetaData;
	}

	@Override
	public ManagedObjectIndex[] getManagedObjectIndexes() {
		return this.managedObjectIndexes;
	}

	@Override
	public ManagedObjectMetaData<?>[] getManagedObjectMetaData() {
		return this.managedObjectMetaData;
	}

	@Override
	public AdministratorIndex[] getAdministratorIndexes() {
		return this.administratorIndexes;
	}

	@Override
	public AdministratorMetaData<?, ?>[] getAdministratorMetaData() {
		return this.administratorMetaData;
	}

}